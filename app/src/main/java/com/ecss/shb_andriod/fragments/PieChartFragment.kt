package com.ecss.shb_andriod.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ecss.shb_andriod.R
import com.ecss.shb_andriod.model.ChartDataViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import androidx.core.graphics.toColorInt
import android.util.Log

class PieChartFragment : Fragment() {
    private lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pie_chart, container, false)
        pieChart = view.findViewById(R.id.pieChart)
        pieChart.isDrawHoleEnabled = false // Remove inner circle
        pieChart.holeRadius = 0f // Ensure no hole is drawn
        pieChart.isRotationEnabled = false // Disable rotation by touch
        // Attach custom marker
        val marker = com.ecss.shb_andriod.view.CustomTooltipMarkerView(requireContext(), R.layout.custom_marker_view)
        pieChart.marker = marker
        // Optionally, set breakdown map if needed:
        // marker.setBreakdownMap(breakdownMap)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val chartDataViewModel = ViewModelProvider(requireActivity())[ChartDataViewModel::class.java]
        chartDataViewModel.surveys.observe(viewLifecycleOwner) { surveys ->
            val pieEntries = chartDataViewModel.getPieEntriesByLocation()
            val dataSet = PieDataSet(pieEntries, "")
            dataSet.setDrawValues(false)
            // Set custom colors for Seen, Heard, Not Found (green, blue, red)
            // Pie by seenHeard (for colors), fallback to location if not available
            val pieEntriesSeenHeard = chartDataViewModel.surveys.value
                ?.groupingBy { it.seenHeard?.trim() ?: "Unknown" }
                ?.eachCount()
                ?.map { (label, count) ->
                    com.github.mikephil.charting.data.PieEntry(count.toFloat(), label)
                } ?: emptyList()
            val colorMap = mapOf(
                "Seen" to "#388E3C".toColorInt(), // green
                "Heard" to "#1976D2".toColorInt(), // blue
                "Not Found" to "#D32F2F".toColorInt()
            )
            // Log entry labels for debugging
            pieEntries.forEach { Log.d("PieChartFragment", "PieEntry label: '${it.label}' value: ${it.value}") }
            val colorsSeenHeard = pieEntriesSeenHeard.map { entry ->
                colorMap[entry.label.trim()] ?: android.graphics.Color.LTGRAY
            }
            val dataSetSeenHeard = PieDataSet(pieEntriesSeenHeard, "")
            dataSetSeenHeard.setDrawValues(false)
            dataSetSeenHeard.colors = colorsSeenHeard
            val pieDataSeenHeard = PieData(dataSetSeenHeard)
            pieChart.data = pieDataSeenHeard
            pieChart.legend.isEnabled = false
            pieChart.description.isEnabled = false
            pieChart.setDrawEntryLabels(false)
            pieChart.setEntryLabelColor(android.graphics.Color.TRANSPARENT)
            pieChart.setEntryLabelTextSize(0f)
            pieChart.setExtraOffsets(0f, 0f, 0f, 0f)
            pieChart.invalidate()
            // Pie by location: assign a color from a palette for each location
            val colorPalette = listOf(
                android.graphics.Color.CYAN,
                android.graphics.Color.DKGRAY,
                android.graphics.Color.GRAY,
                android.graphics.Color.LTGRAY,
                android.graphics.Color.MAGENTA,
                android.graphics.Color.YELLOW
            )
            val locationColors = pieEntries.mapIndexed { idx, _ ->
                colorPalette[idx % colorPalette.size]
            }
            dataSet.colors = locationColors
            val pieData = PieData(dataSet)
            pieChart.data = pieData
            pieChart.legend.isEnabled = false
            pieChart.description.isEnabled = false
            pieChart.setDrawEntryLabels(false)
            pieChart.setEntryLabelColor(android.graphics.Color.TRANSPARENT)
            pieChart.setEntryLabelTextSize(0f)
            pieChart.setExtraOffsets(0f, 0f, 0f, 0f)
            pieChart.invalidate()
        }
    }
}