package com.ecss.shb_andriod.model

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

object DataVisualization {
    fun setupPieChart(pieChart: PieChart, data: List<Pair<String, Float>>) {
        val entries = data.map { PieEntry(it.second, it.first) }
        val dataSet = PieDataSet(entries, "Observations")
        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.invalidate()
    }
}