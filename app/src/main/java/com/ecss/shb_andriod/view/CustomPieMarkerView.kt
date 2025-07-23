package com.ecss.shb_andriod.view

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.ecss.shb_andriod.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class CustomPieMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvLabel: TextView = findViewById(R.id.tvLabel)
    private val tvValue: TextView = findViewById(R.id.tvValue)
    private var columnEntries: List<Entry>? = null

    fun setColumnEntries(entries: List<Entry>?) {
        columnEntries = entries
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e is PieEntry) {
            val dataMap = e.data as? Map<String, Any>
            val location = e.label
            val total = e.value.toInt()
            val breakdown = dataMap?.get("breakdown") as? Map<String, Int>
            val sb = StringBuilder()
            sb.append(location)
            sb.append("<br>Total: $total")
            if (breakdown != null && breakdown.isNotEmpty()) {
                val order = listOf("Seen", "Heard", "Not found")
                order.forEach { key ->
                    breakdown[key]?.let { value ->
                        val color = when (key) {
                            "Seen" -> android.graphics.Color.parseColor("#00FF00")
                            "Heard" -> android.graphics.Color.parseColor("#2196F3")
                            "Not found" -> android.graphics.Color.parseColor("#FF0000")
                            else -> android.graphics.Color.WHITE
                        }
                        sb.append("<br>")
                        sb.append("<font color='${String.format("#%06X", 0xFFFFFF and color)}' size='2'>$key: $value</font>")
                    }
                }
                breakdown.keys.filter { it !in order }.forEach { key ->
                    breakdown[key]?.let { value ->
                        sb.append("<br><font color='#FFFFFF' size='2'>$key: $value</font>")
                    }
                }
            }
            tvLabel.text = android.text.Html.fromHtml(sb.toString(), android.text.Html.FROM_HTML_MODE_LEGACY)
            tvLabel.textSize = 12f // Make font smaller
            tvValue.text = ""
        } else if (e != null && e.data is Map<*, *>) {
            val dataMap = e.data as? Map<String, Any>
            if (dataMap != null && dataMap.containsKey("index")) {
                // TreeChartView marker format
                val index = dataMap["index"] ?: "-"
                val treeHeight = dataMap["treeHeight"] ?: "-"
                val birdHeight = dataMap["birdHeight"] ?: "-"
                val activityType = dataMap["activityType"] ?: "-"
                val seenHeard = dataMap["seenHeard"] ?: "-"
                val date = dataMap["date"] ?: "-"
                val time = dataMap["time"] ?: "-"
                val location = dataMap["location"] ?: "-"
                val numberOfBirds = dataMap["numberOfBirds"] ?: "-"

                val sb = StringBuilder()
                sb.append("Tree $index\n")
                sb.append("Tree Height: $treeHeight\n")
                sb.append("Bird Height: $birdHeight\n")
                sb.append("Activity Type: $activityType\n")
                sb.append("Seen/Heard: $seenHeard\n")
                sb.append("Date: $date\n")
                sb.append("Time: $time\n")
                sb.append("Location: $location\n")
                sb.append("Number of Birds: $numberOfBirds")

                tvLabel.text = sb.toString()
                tvLabel.textSize = 12f
                tvValue.text = ""
            } else if (dataMap != null && dataMap.containsKey("percentage")) {
                // TreePercentageChartView marker format
                val index = dataMap["index"] ?: "-"
                val percentage = dataMap["percentage"] ?: "-"
                val treeHeight = dataMap["treeHeight"] ?: "-"
                val birdHeight = dataMap["birdHeight"] ?: "-"
                val location = dataMap["location"] ?: "-"
                val date = dataMap["date"] ?: "-"
                val time = dataMap["time"] ?: "-"
                val sb = StringBuilder()
                sb.append("Tree $index\n")
                sb.append("Percentage: $percentage%\n")
                sb.append("Tree Height: $treeHeight\n")
                sb.append("Bird Height: $birdHeight\n")
                sb.append("Location: $location\n")
                sb.append("Date: $date\n")
                sb.append("Time: $time")
                tvLabel.text = sb.toString()
                tvLabel.textSize = 14f // Slightly bigger font for percentage marker
                tvLabel.setPadding(32, 32, 32, 32) // Add padding for readability
                tvValue.text = ""
            } else {
                // Fallback: Only show marker for line chart
                val label = dataMap?.get("label") as? String ?: ""
                val total = e.y.toInt()
                val breakdowns = dataMap?.get("breakdowns") as? Map<String, Int>
                val sb = StringBuilder()
                sb.append(label)
                sb.append("\nTotal: $total")
                breakdowns?.forEach { (k, v) -> sb.append("\n$k: $v") }
                tvLabel.text = sb.toString()
                tvLabel.textSize = 12f
                tvValue.text = ""
            }
        } else {
            tvLabel.text = ""
            tvValue.text = ""
        }
        super.refreshContent(e, highlight)
    }

    private fun isMarkerActive(): Boolean {
        // Implement logic to check if the marker should be active for the current chart
        // For now, always return true. You can set a flag from your chart switching logic.
        return true
    }

    override fun getOffset(): MPPointF {
        // Shift the tooltip a little more to the left
        val offsetX = -(width * 0.5f) // Make area smaller by reducing offset
        val chartWidth = chartView?.width ?: 0
        val chartHeight = chartView?.height ?: 0
        val markerWidth = width
        val markerHeight = height

        // Calculate Y offset: show below if it would be cut off at the top, otherwise above
        var offsetY = if (markerHeight + 32 > chartHeight / 2) {
            4f // show below the slice with less padding (higher)
        } else {
            -markerHeight.toFloat() - 10f // show above the slice with less padding (higher)
        }
        // Clamp Y so marker stays within chart vertically
        offsetY = offsetY.coerceAtLeast(-markerHeight.toFloat()).coerceAtMost(chartHeight - markerHeight.toFloat())

        // Clamp X so marker stays within chart horizontally
        val safeOffsetX = offsetX.coerceAtLeast(-markerWidth.toFloat()).coerceAtMost(chartWidth - markerWidth.toFloat())
        return MPPointF(safeOffsetX, offsetY)
    }
}