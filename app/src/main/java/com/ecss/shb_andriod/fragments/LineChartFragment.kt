package com.ecss.shb_andriod.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ecss.shb_andriod.R
import com.ecss.shb_andriod.model.ChartDataViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class LineChartFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_line_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lineChart = view.findViewById<LineChart>(R.id.lineChart)
        val chartDataViewModel = ViewModelProvider(requireActivity())[ChartDataViewModel::class.java]
        // Move chart down with margin
        (lineChart.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 32
        chartDataViewModel.surveys.observe(viewLifecycleOwner) { surveys ->
            if (surveys.isNullOrEmpty()) return@observe
            // Use the new function that includes total in the map
            val grouped = chartDataViewModel.countObservationsByMonthYearWithSeenHeardAndTotal(surveys)
            val seenHeardTypes = listOf("Total", "Seen", "Heard", "Not found")
            val monthYears = grouped.keys.sortedBy { key ->
                java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.UK).parse(key)
            }
            // Color palette (same as report)
            val colorMap = mapOf(
                "Seen" to Color.BLUE, // Seen: blue
                "Heard" to Color.GREEN, // Heard: green
                "Not found" to Color.RED, // Not found: red
                "Total" to Color.WHITE
            )
            val dataSets = seenHeardTypes.map { seenHeard ->
                val entries = monthYears.mapIndexed { idx, monthYear ->
                    val breakdown = grouped[monthYear]?.second ?: emptyMap()
                    val entry = Entry(idx.toFloat(), breakdown[seenHeard]?.toFloat() ?: 0f)
                    // Attach breakdown and label for marker
                    entry.data = mapOf(
                        "label" to monthYear,
                        "breakdowns" to breakdown
                    )
                    entry
                }
                LineDataSet(entries, seenHeard).apply {
                    lineWidth = 2f
                    setDrawValues(false)
                    color = colorMap[seenHeard] ?: Color.MAGENTA
                    setCircleColor(colorMap[seenHeard] ?: Color.MAGENTA)
                }
            }
            // Put Total as the first dataset
            val allDataSets = dataSets.sortedBy { if (it.label == "Total") 0 else 1 }
            val lineData = LineData(allDataSets as List<com.github.mikephil.charting.interfaces.datasets.ILineDataSet>)
            lineChart.data = lineData
            lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(monthYears)
            lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            lineChart.xAxis.granularity = 1f
            lineChart.xAxis.labelCount = 6 // Show fewer x labels to avoid overlap
            lineChart.xAxis.setLabelRotationAngle(30f) // Rotate labels for readability
            lineChart.xAxis.axisMinimum = 0f // First tick touches y-axis
            lineChart.xAxis.axisMaximum = (monthYears.size - 1).toFloat()
            lineChart.xAxis.spaceMin = 0.2f
            lineChart.xAxis.spaceMax = 0.2f
            lineChart.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT // Use full width of parent
            lineChart.requestLayout()
            lineChart.setVisibleXRangeMaximum(6f) // Show 6 points at a time, rest scrollable
            // Fix: always show the first x-axis value by moving to the start
            lineChart.moveViewToX(0f)
            lineChart.axisRight.isEnabled = false
            lineChart.description.isEnabled = false
            // Make x-axis tick labels smaller
            lineChart.xAxis.textSize = 8f
            // Center and space out legend vertically
            lineChart.legend.isEnabled = true
            lineChart.legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM // Move legend to bottom
            lineChart.legend.yOffset = 20f // Increase gap between chart and legend
            lineChart.legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            lineChart.legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            lineChart.legend.yEntrySpace = 20f // Reduce vertical gap between legend entries
            lineChart.legend.xEntrySpace = 32f // Reduce horizontal gap between legend entries
            // X-axis line color light grey
            lineChart.xAxis.axisLineColor = android.graphics.Color.LTGRAY
            // Set x-axis and y-axis label values and legend to LTGRAY
            lineChart.xAxis.textColor = android.graphics.Color.LTGRAY
            lineChart.axisLeft.textColor = android.graphics.Color.LTGRAY
            lineChart.legend.textColor = android.graphics.Color.LTGRAY
            // Disable zoom but allow horizontal scroll
            lineChart.setScaleEnabled(false)
            lineChart.setPinchZoom(false)
            lineChart.setDragEnabled(true)
            lineChart.setScaleXEnabled(false)
            lineChart.setScaleYEnabled(false)
            lineChart.setVisibleXRangeMaximum(6f)
            lineChart.setVisibleXRangeMinimum(1f)
            val allEntries = allDataSets.flatMap { it.values as List<Entry> }
            val maxY = allEntries.maxOfOrNull { entry -> entry.y } ?: 1f
            lineChart.axisLeft.axisMaximum = (maxY + 2).coerceAtLeast(maxY + 1.1f)
            lineChart.axisLeft.granularity = 1f
            lineChart.axisLeft.setDrawGridLines(true)
            // Set up the custom marker for the line chart
            val marker = com.ecss.shb_andriod.view.CustomTooltipMarkerView(requireContext(), R.layout.custom_marker_view)
            lineChart.marker = marker
            lineChart.invalidate()
        }
    }
}