package com.ecss.shb_andriod.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.ecss.shb_andriod.R
import kotlin.math.roundToInt

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val lineChart: LineChart

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.item_linechart, this, true)
        lineChart = view.findViewById(R.id.lineChart)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(false)
        lineChart.isDoubleTapToZoomEnabled = false
    }

    fun setLineChartData(
        monthYearLabels: List<String>,
        totalCounts: List<Float>,
        breakdowns: Map<String, List<Float>>,
        color: Int = android.graphics.Color.BLUE
    ) {
        Log.d("LineChartView", "Setting line chart data: total=$totalCounts breakdowns=$breakdowns")
        if (monthYearLabels.isEmpty() || totalCounts.isEmpty()) {
            lineChart.clear()
            lineChart.invalidate()
            return
        }
        val dataSets = mutableListOf<LineDataSet>()
        // Total line
        val totalEntries = totalCounts.mapIndexed { idx, value ->
            val breakdownMap = breakdowns.mapValues { it.value[idx].toInt() }
            Entry(idx.toFloat(), value).apply {
                data = mapOf(
                    "label" to monthYearLabels.getOrNull(idx),
                    "breakdowns" to breakdownMap,
                    "lineLabel" to "Total"
                )
            }
        }
        val totalDataSet = LineDataSet(totalEntries, "Total")
        totalDataSet.color = color
        totalDataSet.setDrawValues(false)
        totalDataSet.setDrawCircles(true)
        totalDataSet.setCircleColor(color)
        totalDataSet.lineWidth = 2f
        totalDataSet.circleRadius = 4f
        totalDataSet.setDrawFilled(false)
        dataSets.add(totalDataSet)
        // Breakdown lines
        val breakdownColors = listOf(
            android.graphics.Color.parseColor("#00FF00"), // Seen
            android.graphics.Color.parseColor("#2196F3"), // Heard
            android.graphics.Color.parseColor("#FF0000"), // Not found
            android.graphics.Color.parseColor("#FFA500")  // Other
        )
        breakdowns.entries.forEachIndexed { idx, (key, values) ->
            val entries = values.mapIndexed { i, v ->
                val breakdownMap = breakdowns.mapValues { it.value[i].toInt() }
                Entry(i.toFloat(), v).apply {
                    data = mapOf(
                        "label" to monthYearLabels.getOrNull(i),
                        "breakdowns" to breakdownMap,
                        "lineLabel" to key
                    )
                }
            }
            val dataSet = LineDataSet(entries, key)
            dataSet.color = breakdownColors.getOrNull(idx) ?: android.graphics.Color.LTGRAY
            dataSet.setDrawValues(false)
            dataSet.setDrawCircles(true)
            dataSet.setCircleColor(dataSet.color)
            dataSet.lineWidth = 2f
            dataSet.circleRadius = 4f
            dataSet.setDrawFilled(false)
            dataSets.add(dataSet)
        }
        val lineData = LineData(dataSets as List<com.github.mikephil.charting.interfaces.datasets.ILineDataSet>)
        lineChart.data = lineData
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
       // lineChart.legend.yOffset = 20f // Increase offset to move legend below x-axis label
        lineChart.legend.textColor = android.graphics.Color.WHITE // Set legend label color to white
        lineChart.setViewPortOffsets(100f, 80f, 100f, 200f) // Decrease top offset for more visible top of graph
        setChartTitle("Observations by Month-Year")
        // Show every month label in order

        // --- Dynamic Y-Axis Calculation ---
        // Gather all values
        val allValues = totalCounts + breakdowns.values.flatten()
        val maxValue = allValues.maxOrNull() ?: 0f
        // Find a nice interval (4, 8, 12, etc.)
        fun nextMultipleOfFourOrAbove(value: Float): Float {
            return if (value <= 4f) 4f else (Math.ceil(value / 4.0) * 4).toFloat()
        }
        val interval = nextMultipleOfFourOrAbove((maxValue / 7).coerceAtLeast(4f))
        val axisMax = nextMultipleOfFourOrAbove(maxValue)
        lineChart.axisLeft.axisMinimum = 0f
        lineChart.axisLeft.axisMaximum = axisMax
        lineChart.axisLeft.granularity = interval
        lineChart.axisLeft.labelCount = ((axisMax / interval).toInt() + 1)
        lineChart.axisLeft.textColor = android.graphics.Color.WHITE
        lineChart.axisRight.isEnabled = false

        // Dynamically set axisMaximum to the next number divisible by 4 above the max value
        /*val maxValue = (totalCounts.maxOrNull() ?: 0f)
        val interval = 5f
        val labelCount = 5 // 0, 5, 10, 15, 20 (adjust as needed)
        val dynamicMax = if (maxValue % interval == 0f) maxValue else ((maxValue / interval).toInt() + 1) * interval
        lineChart.axisLeft.valueFormatter = null // Use default formatter
        lineChart.axisLeft.granularity = interval
        lineChart.axisLeft.labelCount = labelCount
        lineChart.axisLeft.axisMinimum = 0f
        lineChart.axisLeft.axisMaximum = dynamicMax*/
        lineChart.axisRight.setDrawLabels(false)
        lineChart.axisRight.setDrawAxisLine(false)
        lineChart.axisRight.setDrawGridLines(false)
        lineChart.xAxis.setDrawLabels(true)
        lineChart.xAxis.setDrawAxisLine(true)
        lineChart.xAxis.setDrawGridLines(true)
        lineChart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.textColor = android.graphics.Color.WHITE
        lineChart.xAxis.textSize = 8f // Make x-axis label text smaller
        lineChart.xAxis.setAvoidFirstLastClipping(false)

        // Add marker view for tooltip
        val marker = CustomPieMarkerView(context, R.layout.custom_marker_view)
        lineChart.marker = marker

        lineChart.invalidate()

        // Show marker and highlight lines when a value is selected
        lineChart.setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                // Show marker and highlight lines
                lineChart.highlightValue(h)
            }
            override fun onNothingSelected() {
                val lineData = lineChart.data
                lineData?.dataSets?.forEach { dataSet ->
                    if (dataSet.label == "Other") {
                        dataSet.isVisible = true
                    }
                }
                lineChart.invalidate()
            }
        })

        // Set extra offsets for the chart as requested
        lineChart.setExtraOffsets(400f, 30f, 100f, 200f)
    }

    fun setChartTitle(title: String) {
        val titleView = findViewById<TextView>(R.id.lineChartTitle)
        titleView?.text = title
          titleView?.setTextColor(android.graphics.Color.WHITE)
        titleView?.setTypeface(titleView.typeface, android.graphics.Typeface.BOLD)
        // Reduce top margin and set vertical bias to 0 for better layout
        val params = titleView?.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        params?.topMargin = 32 // dp, adjust as needed
        params?.verticalBias = 0f
        titleView?.layoutParams = params
    }

    class ChartMarkerView(context: Context, layoutRes: Int, private val labels: List<String>) : MarkerView(context, layoutRes) {
        private val tvLabel: TextView = findViewById(R.id.tvLabel)
        private val tvValue: TextView = findViewById(R.id.tvValue)
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            val idx = e?.x?.toInt() ?: 0
            tvLabel.text = labels.getOrNull(idx) ?: ""
            tvValue.text = e?.y?.toInt()?.toString() ?: ""
            super.refreshContent(e, highlight)
        }
        override fun getOffset(): MPPointF {
            return MPPointF(-(width / 2).toFloat(), -height.toFloat())
        }
    }
}