package com.ecss.shb_andriod.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ecss.shb_andriod.R
import com.ecss.shb_andriod.view.TreeChartView

class TreeChartFragment : Fragment() {
    lateinit var treeChartView: TreeChartView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tree_chart, container, false)
        treeChartView = view.findViewById(R.id.treeChartView)
        return view
    }
}