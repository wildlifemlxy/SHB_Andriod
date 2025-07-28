package com.ecss.shb_andriod.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.ecss.shb_andriod.R
import java.util.Locale

class TreePercentageChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    data class DataPoint(
        val label: String,
        val treeHeight: Float?,
        val birdHeight: Float?,
        val observerName: String?,
        val numberOfBirds: String?,
        val location: String?,
        val date: String?,
        val time: String?,
        val activity: String?,
        val seenHeard: String?
    )
    private var data: List<DataPoint> = emptyList()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }
    private var birdDrawable = ContextCompat.getDrawable(context, R.drawable.shb)
    private var scrollOffset = 0f
    private var lastTouchX = 0f
    private var markerIndex: Int? = null

    fun setData(data: List<DataPoint>) {
        this.data = data
        invalidate()
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        when (event.action) {
            android.view.MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                return true
            }
            android.view.MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                lastTouchX = event.x
                val groupWidth = width * 0.7f / minOf(data.size, 7)
                val gap = width * 0.03f / (minOf(data.size, 7) + 1)
                val yAxisStartX = width * 0.1f
                val totalWidth = data.size * (groupWidth + gap) + gap + 40f
                val maxScroll = 0f
                val minScroll = width - totalWidth
                val xFirstTree = yAxisStartX + gap + scrollOffset + dx
                val xLastTree = yAxisStartX + gap + (data.size - 1) * (groupWidth + gap) + scrollOffset + 40f + groupWidth / 2
                if ((dx > 0 && xFirstTree >= yAxisStartX + gap) || (dx < 0 && xLastTree <= width * 0.95f)) {
                    return true
                }
                scrollOffset += dx
                scrollOffset = scrollOffset.coerceIn(minScroll, maxScroll)
                invalidate()
                return true
            }
            android.view.MotionEvent.ACTION_UP -> {
                // Detect tap on tree or bird
                val width = width
                val height = height
                val barCount = data.size
                if (barCount == 0) return super.onTouchEvent(event)
                val groupWidth = width * 0.7f / minOf(barCount, 7)
                val gap = width * 0.03f / (minOf(barCount, 7) + 1)
                val yAxisStartX = width * 0.1f
                val yAxisStartY = height * 0.1f
                val yAxisEndY = height * 0.8f
                for ((i, dp) in data.withIndex()) {
                    if (dp.treeHeight == null || dp.birdHeight == null) continue
                    val treeWidth = groupWidth * 1.1f
                    val centerX = yAxisStartX + gap + i * (groupWidth + gap) + scrollOffset + 40f + treeWidth / 2
                    val treeBaseY = yAxisEndY
                    val treeTopY = yAxisStartY
                    val treeHeightPx = treeBaseY - treeTopY
                    val percent = (dp.birdHeight ?: 0f) / (dp.treeHeight ?: 1f)
                    val birdY = treeBaseY - percent * treeHeightPx
                    val birdRadius = treeWidth * 0.08f * 5f
                    // Tree hit test: bounding rect from treeTopY to treeBaseY, horizontally +/- canopyRadius
                    val canopyRadius = treeWidth * 0.85f
                    val left = centerX - canopyRadius
                    val right = centerX + canopyRadius
                    val top = treeTopY
                    val bottom = treeBaseY
                    if (event.x in left..right && event.y in top..bottom) {
                        markerIndex = i
                        invalidate()
                        return true
                    }
                    // Bird hit test: circle
                    val dxBird = event.x - centerX
                    val dyBird = event.y - birdY
                    if (dxBird * dxBird + dyBird * dyBird <= birdRadius * birdRadius) {
                        markerIndex = i
                        invalidate()
                        return true
                    }
                }
                markerIndex = null
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return
        // Draw chart title at the top center
        val title = "Observations by Tree Heights (%)"
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 54f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT_BOLD, android.graphics.Typeface.BOLD)
        }
        val titleY = 120f // Increased gap from top
        canvas.drawText(title, width / 2f, titleY, titlePaint)
        val width = width
        val height = height
        val barCount = data.size
        if (barCount == 0) return
        val groupWidth = width * 0.7f / minOf(barCount, 7)
        val gap = width * 0.03f / (minOf(barCount, 7) + 1)
        val yAxisStartX = width * 0.1f
        val yAxisStartY = titleY + 100f // Add more gap below title before chart starts
        val yAxisEndY = height * 0.8f
        val xAxisStartX = yAxisStartX
        val xAxisEndX = width * 0.95f
        val yMax = 1f
        val yInterval = yMax / 5
        val tickCount = 5
        val tickLength = 24f
        val labelPaint = Paint(textPaint).apply {
            textAlign = Paint.Align.RIGHT
            textSize = 32f
        }
        // y-axis labels and ticks
        for (i in 0..tickCount) {
            val value = yInterval * i
            val y = yAxisEndY - (value / yMax) * (yAxisEndY - yAxisStartY)
            canvas.drawLine(
                yAxisStartX - tickLength / 2, y,
                yAxisStartX + tickLength / 2, y,
                axisPaint
            )
            // Label (show as %)
            val label = "${((value / yMax) * 100).toInt()}%"
            canvas.drawText(label, yAxisStartX - tickLength, y + 10f, labelPaint)
        }
        // Draw all trees first
        for ((i, dp) in data.withIndex()) {
            if (dp.treeHeight == null || dp.birdHeight == null) continue
            val treeWidth = groupWidth * 1.1f
            // Calculate x position for each tree
            val x = yAxisStartX + gap + i * (groupWidth + gap) + scrollOffset + 40f
            val centerX = x + treeWidth / 2
            val treeBaseY = yAxisEndY
            val treeTopY = yAxisStartY
            val treeHeightPx = treeBaseY - treeTopY
            // --- Draw tree as in TreeChartView, always 100% height ---
            val canopyRadius = treeWidth * 0.85f
            val trunkWidth = treeWidth * 0.18f
            val crownOffset = canopyRadius * 0.85f * 0.8f
            val canopyCenterY1 = treeTopY + crownOffset // top canopy
            val canopyCenterY2 = canopyCenterY1 + canopyRadius * 0.7f // lower canopy
            val bushCanopyBottomY = maxOf(canopyCenterY1, canopyCenterY2) + canopyRadius * 0.8f
            val isBush = bushCanopyBottomY >= treeBaseY - 1f
            if (isBush) {
                val bushPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                bushPaint.style = Paint.Style.FILL
                val bushRadiusX = canopyRadius * 1.2f
                val bushRadiusY = canopyRadius * 0.5f
                val bushCenterX = centerX
                val bushCenterY = treeBaseY - bushRadiusY
                val outerColor = Color.parseColor("#A5D6A7")
                val innerColor = Color.parseColor("#388E3C")
                val shader = android.graphics.RadialGradient(
                    bushCenterX, bushCenterY, bushRadiusX,
                    intArrayOf(innerColor, outerColor),
                    floatArrayOf(0.0f, 1.0f),
                    android.graphics.Shader.TileMode.CLAMP
                )
                bushPaint.shader = shader
                val isSpiky = (i % 2 == 1)
                if (isSpiky) {
                    val bushPath = android.graphics.Path()
                    val spikes = 13
                    val spikeLength = bushRadiusY * 0.7f
                    for (j in 0..spikes) {
                        val angle = Math.PI * 2 * j / spikes
                        val rX = bushRadiusX + if (j % 2 == 0) spikeLength else 0f
                        val rY = bushRadiusY + if (j % 2 == 0) spikeLength else 0f
                        val bx = bushCenterX + Math.cos(angle) * rX
                        val by = bushCenterY + Math.sin(angle) * rY
                        val yFinal = if (angle > Math.PI && angle < 2 * Math.PI && j % 2 == 1) treeBaseY else by
                        if (j == 0) bushPath.moveTo(bx.toFloat(), yFinal.toFloat())
                        else bushPath.lineTo(bx.toFloat(), yFinal.toFloat())
                    }
                    bushPath.close()
                    canvas.drawPath(bushPath, bushPaint)
                } else {
                    canvas.drawOval(
                        bushCenterX - bushRadiusX, treeBaseY - bushRadiusY * 2,
                        bushCenterX + bushRadiusX, treeBaseY,
                        bushPaint
                    )
                }
            } else {
                val greens = listOf("#388E3C", "#A5D6A7")
                val canopyPath = android.graphics.Path()
                canopyPath.moveTo(centerX - canopyRadius, canopyCenterY2)
                canopyPath.cubicTo(
                    centerX - canopyRadius, canopyCenterY2 - canopyRadius * 0.8f,
                    centerX - canopyRadius, canopyCenterY1 + canopyRadius * 0.8f,
                    centerX - canopyRadius, canopyCenterY1
                )
                canopyPath.cubicTo(
                    centerX - canopyRadius, canopyCenterY1 - canopyRadius * 0.8f,
                    centerX + canopyRadius, canopyCenterY1 - canopyRadius * 0.8f,
                    centerX + canopyRadius, canopyCenterY1
                )
                canopyPath.cubicTo(
                    centerX + canopyRadius, canopyCenterY1 + canopyRadius * 0.8f,
                    centerX + canopyRadius, canopyCenterY2 - canopyRadius * 0.8f,
                    centerX + canopyRadius, canopyCenterY2
                )
                canopyPath.cubicTo(
                    centerX + canopyRadius, canopyCenterY2 + canopyRadius * 0.8f,
                    centerX - canopyRadius, canopyCenterY2 + canopyRadius * 0.8f,
                    centerX - canopyRadius, canopyCenterY2
                )
                canopyPath.close()
                val canopyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                canopyPaint.style = Paint.Style.FILL
                val shader = android.graphics.LinearGradient(
                    centerX, canopyCenterY1, centerX, canopyCenterY2,
                    Color.parseColor(greens[0]), Color.parseColor(greens[1]),
                    android.graphics.Shader.TileMode.CLAMP
                )
                canopyPaint.shader = shader
                canvas.drawPath(canopyPath, canopyPaint)
                // Randomly draw ferns, veins, or nothing
                val random = java.util.Random(i.toLong())
                val decoType = random.nextInt(3)
                if (decoType == 1) {
                    val fernPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    fernPaint.color = Color.parseColor("#7CB342")
                    fernPaint.strokeWidth = 5f
                    fernPaint.style = Paint.Style.STROKE
                    val centerY = (canopyCenterY1 + canopyCenterY2) / 2
                    val fernCenterX = centerX
                    val fernCenterY = centerY
                    val fernRadius = canopyRadius * 0.35f
                    for (f in 0..2) {
                        val angle = Math.PI / 2 + (f - 1) * Math.PI / 7
                        val fx = fernCenterX + Math.cos(angle) * fernRadius
                        val fy = fernCenterY - Math.abs(Math.sin(angle)) * fernRadius * 1.1f
                        val rectF = android.graphics.RectF(
                            (fx - fernRadius * 0.7f).toFloat(), (fy - fernRadius * 0.7f).toFloat(),
                            (fx + fernRadius * 0.7f).toFloat(), (fy + fernRadius * 0.7f).toFloat()
                        )
                        canvas.drawArc(rectF, 200f, 140f, false, fernPaint)
                    }
                } else if (decoType == 2) {
                    val veinPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    veinPaint.color = Color.parseColor("#AEE571")
                    veinPaint.strokeWidth = 3f
                    veinPaint.style = Paint.Style.STROKE
                    val veinCount = 5 + random.nextInt(3)
                    val centerY = (canopyCenterY1 + canopyCenterY2) / 2
                    for (v in 0 until veinCount) {
                        val angle = Math.PI * (0.2 + 0.6 * random.nextFloat())
                        val ex = centerX + Math.cos(angle) * canopyRadius * (0.85 + 0.1 * random.nextFloat())
                        val ey = centerY + Math.sin(angle) * (canopyCenterY2 - canopyCenterY1) * 0.7
                        canvas.drawLine(centerX, centerY, ex.toFloat(), ey.toFloat(), veinPaint)
                    }
                }
            }
            // Draw bird nest fern in the middle of the trunk, always
            val trunkTopY = minOf(canopyCenterY2 + canopyRadius * 0.5f, treeBaseY)
            val trunkMidY = (treeBaseY + trunkTopY) / 2f
            val fernCenterX = centerX
            val fernCenterY = trunkMidY
            val fernRadius = canopyRadius * 0.35f
            val fernPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            fernPaint.color = Color.parseColor("#7CB342")
            fernPaint.strokeWidth = 5f
            fernPaint.style = Paint.Style.STROKE
            for (f in 0..2) {
                val angle = Math.PI / 2 + (f - 1) * Math.PI / 7
                val fx = fernCenterX + Math.cos(angle) * fernRadius
                val fy = fernCenterY - Math.abs(Math.sin(angle)) * fernRadius * 1.1f
                val rectF = android.graphics.RectF(
                    (fx - fernRadius * 0.7f).toFloat(), (fy - fernRadius * 0.7f).toFloat(),
                    (fx + fernRadius * 0.7f).toFloat(), (fy + fernRadius * 0.7f).toFloat()
                )
                canvas.drawArc(rectF, 200f, 140f, false, fernPaint)
            }
            // Only draw trunk if the bottom of the canopy is above the x-axis (treeBaseY)
            val canopyBottomY = maxOf(canopyCenterY1, canopyCenterY2) + canopyRadius * 0.8f
            if (canopyBottomY < treeBaseY - 1f) {
                val trunkTopY = minOf(canopyCenterY2 + canopyRadius * 0.5f, treeBaseY)
                val trunkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                trunkPaint.color = Color.parseColor("#8D5524")
                val trunkPath = android.graphics.Path()
                trunkPath.moveTo(centerX - trunkWidth * 0.4f, treeBaseY)
                trunkPath.lineTo(centerX - trunkWidth * 0.4f, trunkTopY)
                trunkPath.lineTo(centerX + trunkWidth * 0.4f, trunkTopY)
                trunkPath.lineTo(centerX + trunkWidth * 0.4f, treeBaseY)
                trunkPath.close()
                canvas.drawPath(trunkPath, trunkPaint)
            }
            // Draw x-label as "Tree n n" where n is index+1
            val xLabel = "Tree ${i + 1}"
            canvas.drawText(xLabel, centerX, yAxisEndY + 60, textPaint)
        }
        // Draw all birds after all trees so they overlap
        for ((i, dp) in data.withIndex()) {
            if (dp.treeHeight == null || dp.birdHeight == null) continue
            val treeWidth = groupWidth * 1.1f
            val x = yAxisStartX + gap + i * (groupWidth + gap) + scrollOffset + 40f
            val centerX = x + treeWidth / 2
            val treeBaseY = yAxisEndY
            val treeTopY = yAxisStartY
            val treeHeightPx = treeBaseY - treeTopY
            val percent = (dp.birdHeight ?: 0f) / (dp.treeHeight ?: 1f)
            val birdY = treeBaseY - percent * treeHeightPx
            val birdRadius = treeWidth * 0.08f * 5f
            if (birdDrawable != null) {
                val left = (centerX - birdRadius).toInt()
                val top = (birdY - birdRadius).toInt()
                val right = (centerX + birdRadius).toInt()
                val bottom = (birdY + birdRadius).toInt()
                birdDrawable.setBounds(left, top, right, bottom)
                birdDrawable.draw(canvas)
            }
        }
        // Draw percentage label marker tooltip if needed
        if (markerIndex != null) {
            val i = markerIndex!!
            val dp = data[i]
            val treeWidth = groupWidth * 1.1f
            val x = yAxisStartX + gap + i * (groupWidth + gap) + scrollOffset + 40f
            val centerX = x + treeWidth / 2
            val treeBaseY = yAxisEndY
            val treeTopY = yAxisStartY
            val treeHeightPx = treeBaseY - treeTopY
            val percent = (dp.birdHeight ?: 0f) / (dp.treeHeight ?: 1f)
            val birdY = treeBaseY - percent * treeHeightPx
            // Marker tooltip content
            val markerLines = listOf(
                "Number of Birds: ${dp.numberOfBirds}",
                "Location: ${dp.location}",
                "Percentage: ${String.format(Locale.getDefault(), "%.2f", percent * 100)}%",
                "Date: ${formatDate(dp.date)}",
                "Time: ${formatTime(dp.time)}",
                "Activity: ${dp.activity}",
                "Seen/Heard: ${dp.seenHeard}"
            )
            val padding = 60f // Add missing definition
            val fixedWidth = 700f // Add missing definition
            val textHeight = 70f * markerLines.size + padding
            val gapToTree = 32f
            // --- Tooltip always aligns top with bird ---
            var left = centerX + gapToTree
            var top = birdY
            var right = left + fixedWidth
            var bottom = top + textHeight
            // Clamp left/right/top/bottom to stay inside canvas
            if (right > width) {
                left -= (right - width)
                right = width.toFloat()
            }
            if (left < 0) {
                right += -left
                left = 0f
            }
            if (top < 0) {
                bottom += -top
                top = 0f
            }
            if (bottom > height) {
                top -= (bottom - height)
                bottom = height.toFloat()
            }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(220, 50, 50, 50)
                style = Paint.Style.FILL
            }
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 36f // Slightly bigger font size
            }
            val boldPaint = Paint(textPaint).apply {
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT_BOLD, android.graphics.Typeface.BOLD)
            }
            canvas.drawRoundRect(left, top, right, bottom, 32f, 32f, paint)
            markerLines.forEachIndexed { j, line ->
                val y = top + padding + 60f * (j + 1)
                if (j == 0) {
                    canvas.drawText(line, left + padding, y, boldPaint)
                } else {
                    canvas.drawText(line, left + padding, y, textPaint)
                }
            }
        }
        // Draw axes over everything
        axisPaint.color = Color.WHITE
        axisPaint.strokeWidth = 8f
        axisPaint.style = Paint.Style.STROKE
        // y-axis
        canvas.drawLine(yAxisStartX, yAxisStartY, yAxisStartX, yAxisEndY, axisPaint)
        // x-axis
        canvas.drawLine(xAxisStartX, yAxisEndY, xAxisEndX, yAxisEndY, axisPaint)
    }

    private fun formatDate(date: String?): String {
        if (date.isNullOrBlank()) return ""
        val formats = listOf(
            "yyyy-MM-dd",
            "MM/dd/yyyy",
            "dd/MM/yyyy",
            "dd-MMM-yy"
        )
        for (fmt in formats) {
            try {
                val input = java.text.SimpleDateFormat(fmt, java.util.Locale.getDefault())
                val output = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                return output.format(input.parse(date)!!)
            } catch (_: Exception) {}
        }
        return date
    }
    private fun formatTime(time: String?): String {
        if (time.isNullOrBlank()) return ""
        val formats = listOf(
            "HH:mm:ss",
            "h:mm:ss a",
            "h:mm a"
        )
        for (fmt in formats) {
            try {
                val input = java.text.SimpleDateFormat(fmt, java.util.Locale.getDefault())
                val output = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                return output.format(input.parse(time)!!)
            } catch (_: Exception) {}
        }
        return time
    }
}
