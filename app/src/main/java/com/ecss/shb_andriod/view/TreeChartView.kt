package com.ecss.shb_andriod.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.ecss.shb_andriod.R
import kotlin.math.ceil

class TreeChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    data class DataPoint(
        val observerName: String?,
        val shbIndividualId: String?,
        val numberOfBirds: String?,
        val location: String?,
        val date: String?,
        val time: String?,
        val heightOfTree: String?,
        val heightOfBird: String?,
        val seenHeard: String?,
        val activityDetails: String?,
        val activity: String?
    )

    private var data: List<DataPoint> = emptyList()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 8f // Make axis thicker for better visibility
        style = Paint.Style.STROKE // Ensure axis is drawn as a line
    }
    private var maxValue = 1f
    private var scrollOffset = 0f
    private var lastTouchX = 0f
    private var birdDrawable = ContextCompat.getDrawable(context, R.drawable.shb)
    private var showPercentage: Boolean = false
    private var markerIndex: Int? = null
    private var markerX: Float = 0f
    private var markerY: Float = 0f

    init {
        // No XML drawable available, so nothing to load here.
        // Tree will be drawn directly in onDraw using Canvas commands.
    }

    fun setData(data: List<DataPoint>) {
        this.data = data
        maxValue = 1f
        for (dp in data) {
            val treeHeight = dp.heightOfTree?.toFloatOrNull()
            val birdHeight = dp.heightOfBird?.toFloatOrNull()
            if (treeHeight != null && treeHeight > maxValue) maxValue = treeHeight
            if (birdHeight != null && birdHeight > maxValue) maxValue = birdHeight
        }
        invalidate()
    }

    fun setShowPercentage(show: Boolean) {
        showPercentage = show
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                return true
            }
            MotionEvent.ACTION_MOVE -> {
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
                // Prevent scrolling past the first and last tree
                if ((dx > 0 && xFirstTree >= yAxisStartX + gap) || (dx < 0 && xLastTree <= width * 0.95f)) {
                    return true
                }
                scrollOffset += dx
                // Clamp scrollOffset so you can't scroll past the first or last tree
                scrollOffset = scrollOffset.coerceIn(minScroll, maxScroll)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
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
                val yMax = maxValue
                for ((i, dp) in data.withIndex()) {
                    if (dp.heightOfTree == null || dp.heightOfBird == null) continue
                    var x = yAxisStartX + gap + i * (groupWidth + gap) + scrollOffset + 40f
                    val treeWidth = groupWidth * 1.1f
                    val centerX = x + treeWidth / 2
                    val treeBaseY = yAxisEndY
                    val treeValue = dp.heightOfTree?.toFloatOrNull() ?: 0f
                    val normValue = treeValue / yMax
                    val treeHeightNorm = normValue * (yAxisEndY - yAxisStartY)
                    val treeTopY = yAxisEndY - treeHeightNorm
                    val canopyRadius = treeWidth * 0.85f
                    // Hit test for the whole tree: bounding rect from treeTopY to treeBaseY, horizontally +/- canopyRadius
                    val left = centerX - canopyRadius
                    val right = centerX + canopyRadius
                    val top = treeTopY
                    val bottom = treeBaseY
                    if (event.x in left..right && event.y in top..bottom) {
                        markerIndex = i
                        markerX = centerX
                        markerY = treeTopY
                        invalidate()
                        return true
                    }
                    // Hit test for bird
                    val birdY = yAxisEndY - ((dp.heightOfBird?.toFloatOrNull() ?: 0f) / yMax) * (yAxisEndY - yAxisStartY)
                    val birdRadius = treeWidth * 0.08f * 5f
                    val dxBird = event.x - centerX
                    val dyBird = event.y - birdY
                    if (dxBird * dxBird + dyBird * dyBird <= birdRadius * birdRadius) {
                        markerIndex = i
                        markerX = centerX
                        markerY = birdY
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
        val title = "Observations by Tree Heights"
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 54f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT_BOLD, android.graphics.Typeface.BOLD)
        }
        canvas.drawText(title, width / 2f, 80f, titlePaint)

        val width = width
        val height = height
        val barCount = data.size
        if (barCount == 0) return
        val groupWidth = width * 0.7f / minOf(barCount, 7)
        val gap = width * 0.03f / (minOf(barCount, 7) + 1)
        val yAxisStartX = width * 0.1f
        val yAxisStartY = height * 0.1f
        val yAxisEndY = height * 0.8f
        val xAxisStartX = yAxisStartX
        val xAxisEndX = width * 0.95f

        // Calculate yMax as the next rounded interval above the max value
        val intervalBase = 10f
        val maxValueRounded = if (maxValue <= intervalBase) intervalBase else Math.ceil((maxValue / intervalBase).toDouble()).toFloat() * intervalBase
        // Remove all 'if (showPercentage)' logic and always use the non-percentage logic
        // Set yInterval and tickCount dynamically based on your requirements
        // Dynamically determine yInterval based on maxValue for nice axis ticks
        val intervals = floatArrayOf(1f, 2f, 5f, 10f, 20f, 25f, 50f, 100f)
        var yInterval = intervals[0]
        for (i in intervals.indices) {
            if (maxValue / intervals[i] <= 6) {
                yInterval = intervals[i]
                break
            }
        }
        val yMax = (Math.ceil((maxValue / yInterval).toDouble()).toInt() + 1) * yInterval
        val tickCount = (yMax / yInterval).toInt()
        val tickLength = 24f
        val labelPaint = Paint(textPaint).apply {
            textAlign = Paint.Align.RIGHT
            textSize = 32f
        }

        for (i in 0..tickCount) {
            val value = yInterval * i
            val y = yAxisEndY - (value / yMax) * (yAxisEndY - yAxisStartY)
            canvas.drawLine(
                yAxisStartX - tickLength / 2, y,
                yAxisStartX + tickLength / 2, y,
                axisPaint
            )
            val label = "${value.toInt()} m"
            canvas.drawText(label, yAxisStartX - tickLength, y + 10f, labelPaint)
        }

        for ((i, dp) in data.withIndex()) {
            // Only skip if treeHeight is null (N/A), but allow birdHeight to be N/A
            if (dp.heightOfTree == null) continue
            var x = yAxisStartX + gap + i * (groupWidth + gap) + scrollOffset + 40f
            val treeWidth = groupWidth * 1.1f
            val centerX = x + treeWidth / 2
            val treeBaseY = yAxisEndY
            val treeValue = dp.heightOfTree?.toFloatOrNull() ?: 0f
            // Use yMax for normalization so each tree height is correct in the plotting area
            val normValue = treeValue / yMax
            val treeHeightNorm = normValue * (yAxisEndY - yAxisStartY)
            val treeTopY = yAxisEndY - treeHeightNorm
            // --- Only 2 canopy circles, and include crown offset in height ---
            val canopyRadius = treeWidth * 0.85f
            val trunkWidth = treeWidth * 0.18f
            val crownOffset = canopyRadius * 0.85f * 0.8f // similar to previous crownTipOffset
            // The top of the tree is the top of the upper canopy circle
            val canopyCenterY1 = treeTopY + crownOffset // top canopy
            val canopyCenterY2 = canopyCenterY1 + canopyRadius * 0.7f // lower canopy

            // If the tree is short (canopy touches or is very close to x-axis), draw a bush instead of a tall canopy
            val bushCanopyBottomY = maxOf(canopyCenterY1, canopyCenterY2) + canopyRadius * 0.8f
            val isBush = bushCanopyBottomY >= yAxisEndY - 1f
            if (isBush) {
                val bushPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                bushPaint.style = Paint.Style.FILL
                // Gradient for bush: radial from outer (lighter) to inner (darker)
                val bushRadiusX = canopyRadius * 1.2f
                val bushRadiusY = canopyRadius * 0.5f
                val bushCenterX = centerX
                val bushCenterY = yAxisEndY - bushRadiusY
                val outerColor = Color.parseColor("#A5D6A7") // lighter
                val innerColor = Color.parseColor("#388E3C") // darker
                val shader = android.graphics.RadialGradient(
                    bushCenterX, bushCenterY, bushRadiusX,
                    intArrayOf(innerColor, outerColor),
                    floatArrayOf(0.0f, 1.0f),
                    android.graphics.Shader.TileMode.CLAMP
                )
                bushPaint.shader = shader
                val isSpiky = (i % 2 == 1)
                if (isSpiky) {
                    // Draw a spiky bush with all spikes visible, base tips on x-axis
                    val bushPath = android.graphics.Path()
                    val spikes = 13
                    val spikeLength = bushRadiusY * 0.7f
                    for (j in 0..spikes) {
                        val angle = Math.PI * 2 * j / spikes
                        val rX = bushRadiusX + if (j % 2 == 0) spikeLength else 0f
                        val rY = bushRadiusY + if (j % 2 == 0) spikeLength else 0f
                        val x = bushCenterX + Math.cos(angle) * rX
                        val y = bushCenterY + Math.sin(angle) * rY
                        // For lower half (bottom), force all points to x-axis only if they are the base (not spikes)
                        val yFinal = if (angle > Math.PI && angle < 2 * Math.PI && j % 2 == 1) yAxisEndY else y
                        if (j == 0) bushPath.moveTo(x.toFloat(), yFinal.toFloat())
                        else bushPath.lineTo(x.toFloat(), yFinal.toFloat())
                    }
                    bushPath.close()
                    canvas.drawPath(bushPath, bushPaint)
                } else {
                    // Draw round bush (oval) with base on x-axis
                    canvas.drawOval(
                        bushCenterX - bushRadiusX, yAxisEndY - bushRadiusY * 2,
                        bushCenterX + bushRadiusX, yAxisEndY,
                        bushPaint
                    )
                }
            } else {
                // Draw a single combined canopy shape (tree)
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

                // --- Randomly draw ferns, veins, or nothing on the tree canopy ---
                val random = java.util.Random(i.toLong())
                val decoType = random.nextInt(3) // 0: nothing, 1: ferns, 2: veins
                if (decoType == 1) {
                    // Draw bird nest fern in the middle of the trunk, overlapping the branches, with the same starting point
                    val fernPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    fernPaint.color = Color.parseColor("#7CB342")
                    fernPaint.strokeWidth = 5f
                    fernPaint.style = Paint.Style.STROKE
                    // Fern and veins share the same starting point (center of canopy)
                    val centerY = (canopyCenterY1 + canopyCenterY2) / 2
                    val fernCenterX = centerX
                    val fernCenterY = centerY
                    val fernRadius = canopyRadius * 0.35f
                    for (f in 0..2) {
                        val angle = Math.PI / 2 + (f - 1) * Math.PI / 7 // upward, spread
                        val fx = fernCenterX + Math.cos(angle) * fernRadius
                        val fy = fernCenterY - Math.abs(Math.sin(angle)) * fernRadius * 1.1f
                        val rectF = android.graphics.RectF(
                            (fx - fernRadius * 0.7f).toFloat(), (fy - fernRadius * 0.7f).toFloat(),
                            (fx + fernRadius * 0.7f).toFloat(), (fy + fernRadius * 0.7f).toFloat()
                        )
                        canvas.drawArc(rectF, 200f, 140f, false, fernPaint)
                    }
                } else if (decoType == 2) {
                    // Draw veins: lines from center to INSIDE the crown (not outside)
                    val veinPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    veinPaint.color = Color.parseColor("#AEE571")
                    veinPaint.strokeWidth = 3f
                    veinPaint.style = Paint.Style.STROKE
                    val veinCount = 5 + random.nextInt(3)
                    val centerY = (canopyCenterY1 + canopyCenterY2) / 2
                    for (v in 0 until veinCount) {
                        val angle = Math.PI * (0.2 + 0.6 * random.nextFloat())
                        val ex = centerX + Math.cos(angle) * canopyRadius * (0.85 + 0.1 * random.nextFloat()) // stay inside
                        val ey = centerY + Math.sin(angle) * (canopyCenterY2 - canopyCenterY1) * 0.7
                        canvas.drawLine(centerX, centerY, ex.toFloat(), ey.toFloat(), veinPaint)
                    }
                }
            }

            // Draw bird nest fern in the middle of the trunk, always (not as decoType)
            val trunkTopY = minOf(canopyCenterY2 + canopyRadius * 0.5f, yAxisEndY)
            val trunkMidY = (treeBaseY + trunkTopY) / 2f
            val fernCenterX = centerX
            val fernCenterY = trunkMidY
            val fernRadius = canopyRadius * 0.35f
            val fernPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            fernPaint.color = Color.parseColor("#7CB342")
            fernPaint.strokeWidth = 5f
            fernPaint.style = Paint.Style.STROKE
            for (f in 0..2) {
                val angle = Math.PI / 2 + (f - 1) * Math.PI / 7 // upward, spread
                val fx = fernCenterX + Math.cos(angle) * fernRadius
                val fy = fernCenterY - Math.abs(Math.sin(angle)) * fernRadius * 1.1f
                val rectF = android.graphics.RectF(
                    (fx - fernRadius * 0.7f).toFloat(), (fy - fernRadius * 0.7f).toFloat(),
                    (fx + fernRadius * 0.7f).toFloat(), (fy + fernRadius * 0.7f).toFloat()
                )
                canvas.drawArc(rectF, 200f, 140f, false, fernPaint)
            }

            // Only draw trunk if the bottom of the canopy is above the x-axis (yAxisEndY)
            val canopyBottomY = maxOf(canopyCenterY1, canopyCenterY2) + canopyRadius * 0.8f
            if (canopyBottomY < yAxisEndY - 1f) { // allow for float rounding
                val trunkTopY = minOf(canopyCenterY2 + canopyRadius * 0.5f, yAxisEndY)
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
        }
        // Draw all bird icons after all trees and bushes, so they overlap every tree and bush
        val birdDrawList = mutableListOf<Triple<Float, Float, Float>>()
        for ((i, dp) in data.withIndex()) {
            // Only skip if treeHeight is null (N/A), but allow birdHeight to be N/A
            if (dp.heightOfTree == null) continue
            var x = yAxisStartX + gap + i * (groupWidth + gap) + scrollOffset + 40f
            val treeWidth = groupWidth * 1.1f
            val centerX = x + treeWidth / 2
            val birdRadius = treeWidth * 0.08f * 5f
            // Use yMax for normalization so bird height is relative to y-axis
            val birdY = yAxisEndY - ((dp.heightOfBird?.toFloatOrNull() ?: 0f) / yMax) * (yAxisEndY - yAxisStartY)
            birdDrawList.add(Triple(centerX, birdY, birdRadius))
        }
        if (birdDrawList.isNotEmpty() && birdDrawable != null) {
            for ((centerX, birdY, birdRadius) in birdDrawList) {
                val left = (centerX - birdRadius).toInt()
                val top = (birdY - birdRadius).toInt()
                val right = (centerX + birdRadius).toInt()
                val bottom = (birdY + birdRadius).toInt()
                birdDrawable.setBounds(left, top, right, bottom)
                birdDrawable.draw(canvas)
            }
        }

        // Draw axes and y-axis labels OVER the trees and birds (so they are always visible)
        axisPaint.color = Color.WHITE
        axisPaint.strokeWidth = 8f
        axisPaint.style = Paint.Style.STROKE
        // y-axis
        canvas.drawLine(yAxisStartX, yAxisStartY, yAxisStartX, yAxisEndY, axisPaint)
        // x-axis
        canvas.drawLine(xAxisStartX, yAxisEndY, xAxisEndX, yAxisEndY, axisPaint)

        // Draw x-axis labels for each tree
        for ((i, dp) in data.withIndex()) {
            // Skip if treeHeight or birdHeight is null (N/A)
            if (dp.heightOfTree == null || dp.heightOfBird == null) continue
            var x = yAxisStartX + gap + i * (groupWidth + gap) + scrollOffset + 40f
            val treeWidth = groupWidth * 1.1f
            val centerX = x + treeWidth / 2
            val xLabel = "Tree ${i + 1}"
            canvas.drawText(xLabel, centerX, yAxisEndY + 60, textPaint)
        }

        // Draw marker tooltip if needed
        markerIndex?.let { idx ->
            if (idx in data.indices) {
                val dp = data[idx]
                val markerLines = listOf(
                    "Tree ${idx + 1}",
                    "Number of Birds: ${dp.numberOfBirds}",
                    "Location: ${dp.location}",
                    "Date: ${formatDate(dp.date)}",
                    "Time: ${formatTime(dp.time)}",
                    "Height of Tree: ${dp.heightOfTree}",
                    "Height of Bird: ${dp.heightOfBird}",
                    "Activity: ${dp.activity}"
                ) +
                (dp.seenHeard?.split("\n")?.map { "Seen/Heard: $it" } ?: listOf("Seen/Heard: "))
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
                val padding = 60f // Increased padding for more space around text
                val maxTextWidth = 700f
                // Wrap lines to fit within maxTextWidth
                fun wrapLine(line: String, paint: Paint, maxWidth: Float): List<String> {
                    if (paint.measureText(line) <= maxWidth) return listOf(line)
                    val words = line.split(" ")
                    val lines = mutableListOf<String>()
                    var current = ""
                    for (word in words) {
                        val test = if (current.isEmpty()) word else "$current $word"
                        if (paint.measureText(test) > maxWidth) {
                            if (current.isNotEmpty()) lines.add(current)
                            current = word
                        } else {
                            current = test
                        }
                    }
                    if (current.isNotEmpty()) lines.add(current)
                    return lines
                }
                // Build all wrapped lines
                val wrappedLines = mutableListOf<Pair<String, Boolean>>() // Pair<line, isBold>
                markerLines.forEachIndexed { i, line ->
                    val isBold = (i == 0)
                    val paintToUse = if (isBold) boldPaint else textPaint
                    wrapLine(line, paintToUse, maxTextWidth).forEach { wrapped ->
                        wrappedLines.add(Pair(wrapped, isBold))
                    }
                }
                val textHeight = 70f * wrappedLines.size + padding
                val gapToTree = 32f
                val left = markerX + gapToTree
                val top = markerY - textHeight / 2
                val right = left + maxTextWidth + 2 * padding
                val bottom = top + textHeight
                canvas.drawRoundRect(left, top, right, bottom, 32f, 32f, paint)
                for ((i, pair) in wrappedLines.withIndex()) {
                    val (line, isBold) = pair
                    val y = top + padding + 60f * (i + 1)
                    canvas.drawText(line, left + padding, y, if (isBold) boldPaint else textPaint)
                }
            }
        }
    }

    // Add these helper functions to the class:
    private fun formatDate(date: String?): String {
        // Try to parse common formats and output as dd/MM/yyyy
        if (date.isNullOrBlank()) return ""
        try {
            // Try yyyy-MM-dd
            val input = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val output = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            return output.format(input.parse(date)!!)
        } catch (_: Exception) {}
        try {
            // Try MM/dd/yyyy
            val input = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
            val output = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            return output.format(input.parse(date)!!)
        } catch (_: Exception) {}
        try {
            // Try dd-MMM-yy (e.g., 23-Jul-25)
            val input = java.text.SimpleDateFormat("dd-MMM-yy", java.util.Locale.getDefault())
            val output = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            return output.format(input.parse(date)!!)
        } catch (_: Exception) {}
        // Fallback: return as is
        return date
    }

    private fun formatTime(time: String?): String {
        // Try to parse common formats and output as HH:mm (24hr)
        if (time.isNullOrBlank()) return ""
        try {
            // Try HH:mm:ss
            val input = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            val output = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return output.format(input.parse(time)!!)
        } catch (_: Exception) {}
        try {
            // Try h:mm a (12hr)
            val input = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            val output = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return output.format(input.parse(time)!!)
        } catch (_: Exception) {}
        // Fallback: return as is
        return time
    }
}
