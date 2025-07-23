package com.ecss.shb_andriod.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecss.shb_andriod.adapter.PieChartAdapter

class DataVisualizationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val recyclerView: RecyclerView = RecyclerView(context)

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        recyclerView.layoutParams = params
        addView(recyclerView)
    }
}
