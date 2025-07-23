package com.ecss.shb_andriod.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.ecss.shb_andriod.R

class PieChartAdapter(private val data: List<Pair<String, Float>>) : RecyclerView.Adapter<PieChartAdapter.PieChartViewHolder>() {
    class PieChartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pieChart: PieChart = view.findViewById(R.id.pieChart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PieChartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_piechart, parent, false)
        return PieChartViewHolder(view)
    }

    override fun onBindViewHolder(holder: PieChartViewHolder, position: Int) {
        val entries = data.map { PieEntry(it.second, it.first) }
        val dataSet = PieDataSet(entries, "Observations")
        val pieData = PieData(dataSet)
        holder.pieChart.data = pieData
        holder.pieChart.description.isEnabled = false
        holder.pieChart.setUsePercentValues(true)
        holder.pieChart.invalidate()
    }

    override fun getItemCount(): Int = 1 // Only one pie chart per adapter
}