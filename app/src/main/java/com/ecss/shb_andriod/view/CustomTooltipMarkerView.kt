package com.ecss.shb_andriod.view

import android.content.Context

import android.widget.TextView
import com.ecss.shb_andriod.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class CustomTooltipMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvLabel: TextView = findViewById(R.id.tvLabel)
    private val tvValue: TextView = findViewById(R.id.tvValue)
    private var columnEntries: List<Entry>? = null
    private var breakdownMapByLabel: Map<String, Map<String, Int>> = emptyMap()

    fun setColumnEntries(entries: List<Entry>?) {
        columnEntries = entries
    }

    fun setBreakdownMap(map: Map<String, Map<String, Int>>) {
        breakdownMapByLabel = map
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e is PieEntry) {
            val location = e.label
            val total = e.value.toInt()
            val breakdown = breakdownMapByLabel[location]
            val order = listOf("Seen", "Heard", "Not found")
            tvLabel.text = location
            tvLabel.textSize = 12f
            // Show breakdown in tvValue with colored spans
            val spannable = android.text.SpannableStringBuilder()
            // Total (white, always from breakdown sum if available)
            val totalFromBreakdown = breakdown?.values?.sum() ?: total
            val totalText = "Total: $totalFromBreakdown\n"
            spannable.append(totalText)
            spannable.setSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.WHITE), 0, totalText.length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            val colorMap = mapOf(
                "Seen" to android.graphics.Color.BLUE, // Seen: blue
                "Heard" to android.graphics.Color.GREEN, // Heard: green
                "Not found" to android.graphics.Color.RED // Not found: red
            )
            order.forEach { key ->
                val value = breakdown?.get(key) ?: 0
                val line = "$key: $value\n"
                val start = spannable.length
                spannable.append(line)
                val end = spannable.length
                val color = colorMap[key] ?: android.graphics.Color.WHITE
                spannable.setSpan(android.text.style.ForegroundColorSpan(color), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            tvValue.text = spannable
        } else if (e != null && e.data is Map<*, *>) {
            val dataMap = e.data as? Map<String, Any>
            val label = dataMap?.get("label") as? String ?: ""
            val breakdowns = dataMap?.get("breakdowns") as? Map<String, Int>
            val order = listOf("Seen", "Heard", "Not found")
            tvLabel.text = label
            tvLabel.textSize = 12f
            // Always show the total for the month-year, regardless of which dot is selected
            val totalFromBreakdown = order.sumOf { breakdowns?.get(it) ?: 0 }
            val spannable = android.text.SpannableStringBuilder()
            val totalText = "Total: $totalFromBreakdown\n"
            spannable.append(totalText)
            spannable.setSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.WHITE), 0, totalText.length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            val colorMap = mapOf(
                "Seen" to android.graphics.Color.BLUE,
                "Heard" to android.graphics.Color.GREEN,
                "Not found" to android.graphics.Color.RED
            )
            order.forEach { key ->
                val value = breakdowns?.get(key) ?: 0
                val line = "$key: $value\n"
                val start = spannable.length
                spannable.append(line)
                val end = spannable.length
                val color = colorMap[key] ?: android.graphics.Color.WHITE
                spannable.setSpan(android.text.style.ForegroundColorSpan(color), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            tvValue.text = spannable
        } else {
            tvLabel.text = ""
            // Always show all keys with 0 if no data
            val order = listOf("Seen", "Heard", "Not found")
            val spannable = android.text.SpannableStringBuilder()
            val totalText = "Total: 0\n"
            spannable.append(totalText)
            spannable.setSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.WHITE), 0, totalText.length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            val colorMap = mapOf(
                "Seen" to android.graphics.Color.BLUE,
                "Heard" to android.graphics.Color.GREEN,
                "Not found" to android.graphics.Color.RED
            )
            order.forEach { key ->
                val line = "$key: 0\n"
                val start = spannable.length
                spannable.append(line)
                val end = spannable.length
                val color = colorMap[key] ?: android.graphics.Color.WHITE
                spannable.setSpan(android.text.style.ForegroundColorSpan(color), start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            tvValue.text = spannable
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
        offsetY = offsetY.coerceAtLeast(-markerHeight.toFloat())
            .coerceAtMost(chartHeight - markerHeight.toFloat())

        // Clamp X so marker stays within chart horizontally
        val safeOffsetX = offsetX.coerceAtLeast(-markerWidth.toFloat())
            .coerceAtMost(chartWidth - markerWidth.toFloat())
        return MPPointF(safeOffsetX, offsetY)
    }
}