package com.ecss.shb_andriod.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import android.view.View
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.ecss.shb_andriod.R

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val pieChart: PieChart

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.item_piechart, this, true)
        pieChart = view.findViewById(R.id.pieChart)
        pieChart.isRotationEnabled = false // Disable rotation
        pieChart.isDrawHoleEnabled = false // Remove inner circle
        pieChart.holeRadius = 0f // Ensure no hole is drawn
        // Make the pie chart bigger by scaling it up
        pieChart.scaleX = 1.15f
        pieChart.scaleY = 1.15f
    }

    fun setPieChartData(entries: List<PieEntry>) {
        Log.d("PieChartView", "Setting pie chart data with $entries")
        if (entries.isEmpty()) {
            pieChart.clear()
            pieChart.invalidate()
            return
        }
        val dataSet = PieDataSet(entries, "")
        Log.d("PieChartView", "Creating PieDataSet with ${entries.size} entries")
        val colorConstants = listOf(
            android.graphics.Color.DKGRAY,
            android.graphics.Color.RED,
            android.graphics.Color.BLUE,
            android.graphics.Color.GRAY,
            android.graphics.Color.GREEN,
            android.graphics.Color.MAGENTA,
            android.graphics.Color.LTGRAY
        )
        val colors = entries.indices.map { i ->
            colorConstants[i % colorConstants.size]
        }
        dataSet.colors = colors
        Log.d("PieChartView", "Colors set for PieDataSet: $colors")
        // Ensure values, labels, legend, and description are hidden
        dataSet.setDrawValues(false)
        dataSet.valueTextColor = android.graphics.Color.TRANSPARENT
        dataSet.valueTextSize = 0f
        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.legend.isEnabled = false
        pieChart.description.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.setEntryLabelColor(android.graphics.Color.TRANSPARENT)
        pieChart.setEntryLabelTextSize(0f)
        // Remove extra offsets for maximum chart area
        pieChart.setExtraOffsets(100f, 80f, 100f, 200f) // Use correct method for PieChart
        pieChart.invalidate() // Force redraw
    }

    fun setChartTitle(title: String) {
        val titleView = findViewById<TextView>(R.id.pieChartTitle)
        titleView?.text = title
        titleView?.setTextColor(android.graphics.Color.WHITE)
        titleView?.setTypeface(titleView.typeface, android.graphics.Typeface.BOLD)
    }

    fun attachCustomMarker(context: Context) {
        val markerView = CustomPieMarkerView(context, R.layout.custom_marker_view)
        pieChart.marker = markerView
        // Remove offsets to maximize pie chart area, but ensure marker is not cut off
        pieChart.setExtraOffsets(0f, 0f, 0f, 0f)
    }

    fun detachCustomMarker() {
        pieChart.marker = null
        // Restore original offsets for better chart visibility
        pieChart.setExtraOffsets(100f, 30f, 100f, 200f)
    }

    fun showPieChart(entries: List<PieEntry>, title: String = "Observations by Location") {
        this.visibility = View.VISIBLE
        setPieChartData(entries)
        setChartTitle(title)
    }
}
