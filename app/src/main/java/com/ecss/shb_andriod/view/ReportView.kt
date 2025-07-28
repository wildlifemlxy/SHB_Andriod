package com.ecss.shb_andriod.view

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.ecss.shb_andriod.R

class ReportView {
    companion object {
        fun showReportPopup(
            context: Context,
            title: String,
            locationBreakdown: Map<String, Pair<Int, Map<String, Int>>>, // location -> (count, breakdown)
            tableHeaderLocation: String,
            tableHeaderObservations: String,
            seenHeardTotal: Int,
            seenHeardBreakdown: String
        ) {
            val dialog = Dialog(context)
            dialog.window?.setLayout(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
            val inflater = LayoutInflater.from(context)
            val popupView = inflater.inflate(R.layout.popup, null)
            val contentRoot = popupView.findViewById<android.widget.LinearLayout>(R.id.popup_content_root)

            // Remove all children to ensure only one layer
            contentRoot.removeAllViews()
            // Set contentRoot width to match parent safely
            if (contentRoot.layoutParams == null) {
                contentRoot.layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            } else {
                contentRoot.layoutParams.width = android.widget.LinearLayout.LayoutParams.MATCH_PARENT
            }

            // Parse breakdown into a map and calculate total before layouts
            val breakdownMap = seenHeardBreakdown.split(",").mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) parts[0].trim() to parts[1].trim().toIntOrNull() else null
            }.toMap()
            val total = breakdownMap.values.mapNotNull { it }.sum().toDouble()

            // --- Section 1: Header ---
            val reportHeaderLayout = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 0, 0, 0)
                gravity = android.view.Gravity.CENTER_VERTICAL // Ensure vertical centering
            }
            val titleView = android.widget.TextView(context).apply {
                text = title
                textSize = 16f // Smaller font size
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                gravity = android.view.Gravity.CENTER_VERTICAL // Center vertically only
                setTextColor(android.graphics.Color.parseColor("#FFFFFF")) // Use pure white
            }
            val closeButton = android.widget.ImageButton(context).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                val sizePx = (context.resources.displayMetrics.density * 24).toInt() // 24dp size
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    sizePx,
                    sizePx
                ).apply {
                    gravity = android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL // Align right and center vertically
                }
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setOnClickListener { dialog.dismiss() }
            }
            reportHeaderLayout.addView(titleView)
            reportHeaderLayout.addView(closeButton)
            contentRoot.addView(reportHeaderLayout)

            // Add gap between header and body
            val headerBodyGap = android.view.View(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    24 // 24px gap, adjust as needed
                )
            }
            contentRoot.addView(headerBodyGap)

            // --- Section 1: Summary ---
            val summarySection = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.VERTICAL // Change to vertical for label row + stats row
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            // Create a horizontal row for labels
            val labelRow = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            val labels = listOf("Total", "Seen", "Heard", "Not found")
            labels.forEach { label ->
                labelRow.addView(android.widget.TextView(context).apply {
                    text = label
                    textSize = 14f // Smaller font size
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(android.graphics.Color.parseColor("#FFFFFF")) // Use pure white for labels
                })
            }
            summarySection.addView(labelRow)
            // Create a horizontal row for values
            val valueRow = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            val values = listOf(
                total.toInt(),
                breakdownMap["Seen"] ?: 0,
                breakdownMap["Heard"] ?: 0,
                breakdownMap["Not found"] ?: 0
            )
            val percents = listOf(
                100.0,
                if (total > 0) (breakdownMap["Seen"] ?: 0) / total * 100 else 0.0,
                if (total > 0) (breakdownMap["Heard"] ?: 0) / total * 100 else 0.0,
                if (total > 0) (breakdownMap["Not found"] ?: 0) / total * 100 else 0.0
            )
            val labelColor = android.graphics.Color.parseColor("#FFFFFF") // Use pure white for label and total/percentage
            val colors = listOf(
                labelColor, // Total: same as label (white)
                android.graphics.Color.parseColor("#388E3C"), // Seen: green
                android.graphics.Color.parseColor("#1976D2"), // Heard: blue
                android.graphics.Color.parseColor("#D32F2F")  // Not found: red
            )
            // Location report summary section
            values.indices.forEach { i ->
                val number = values[i]
                val percent = percents[i]
                val color = colors[i]
                valueRow.addView(android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    gravity = android.view.Gravity.CENTER
                    addView(android.widget.TextView(context).apply {
                        text = number.toString()
                        textSize = 13f // Smaller font size
                        setTextColor(color)
                        gravity = android.view.Gravity.CENTER
                    })
                    if (percent != 0.0) {
                        val percentText = "(${formatPercent(percent)}%)"
                        addView(android.widget.TextView(context).apply {
                            text = percentText
                            textSize = 12f // Smaller font size
                            setTextColor(labelColor) // Percentage in label color (white)
                            gravity = android.view.Gravity.CENTER
                        })
                    }
                })
            }
            summarySection.addView(valueRow)

            // Add gap between summary section and table
            val summaryTableGap = android.view.View(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    32 // 32px gap, adjust as needed
                )
            }

            // --- Section 2: Location Breakdown ---
            val headerHeightPx = (context.resources.displayMetrics.density * 40).toInt()
            val locationTableContainer = android.widget.FrameLayout(context).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    800
                )
            }
            // Sticky header row
            val locationHeaderRow = android.widget.TableRow(context)
            val locationHeader = android.widget.TextView(context).apply {
                text = tableHeaderLocation
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
                textSize = 13f
                setBackgroundColor(android.graphics.Color.parseColor("#333333"))
                setPadding(24, 12, 8, 12)
                layoutParams = android.widget.TableRow.LayoutParams(0, android.widget.TableRow.LayoutParams.WRAP_CONTENT, 2f)
                elevation = 8f
            }
            val countHeader = android.widget.TextView(context).apply {
                text = tableHeaderObservations
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
                textSize = 13f
                setBackgroundColor(android.graphics.Color.parseColor("#333333"))
                setPadding(8, 12, 24, 12)
                gravity = android.view.Gravity.END
                layoutParams = android.widget.TableRow.LayoutParams(0, android.widget.TableRow.LayoutParams.WRAP_CONTENT, 1f)
                elevation = 8f
            }
            locationHeaderRow.addView(locationHeader)
            locationHeaderRow.addView(countHeader)
            val headerLayout = android.widget.TableLayout(context).apply {
                layoutParams = android.widget.TableLayout.LayoutParams(
                    android.widget.TableLayout.LayoutParams.MATCH_PARENT,
                    headerHeightPx
                )
                z = 10f
            }
            headerLayout.addView(locationHeaderRow)
            // Scrollable table rows
            val locationTableScroll = android.widget.ScrollView(context).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
                setPadding(0, headerHeightPx, 0, 0)
            }
            val locationTable = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            var expandedIndex = -1
            val locations = locationBreakdown.keys.toList()
            locations.forEachIndexed { idx, location ->
                val (count, breakdown) = locationBreakdown[location] ?: Pair(0, emptyMap())
                val rowLayout = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                val mainRow = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setPadding(0, 0, 0, 0)
                }
                val locationCell = android.widget.TextView(context).apply {
                    text = location
                    setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
                    textSize = 15f
                    setPadding(24, 12, 8, 12)
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                }
                val countCell = android.widget.TextView(context).apply {
                    text = count.toString()
                    setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
                    textSize = 15f
                    setPadding(8, 12, 24, 12)
                    gravity = android.view.Gravity.END
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                mainRow.addView(locationCell)
                mainRow.addView(countCell)
                rowLayout.addView(mainRow)
                // Breakdown view (hidden by default)
                val breakdownView = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    setBackgroundColor(android.graphics.Color.parseColor("#222222"))
                    setPadding(8, 4, 8, 4)
                    visibility = View.GONE
                    val breakdownLabels = breakdown.keys.toList()
                    val breakdownRow = android.widget.LinearLayout(context).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        setPadding(0, 0, 0, 0)
                    }
                    breakdownLabels.forEachIndexed { idx, label ->
                        val value = breakdown[label] ?: 0
                        val color = when (label) {
                            "Seen" -> android.graphics.Color.parseColor("#388E3C")
                            "Heard" -> android.graphics.Color.parseColor("#1976D2")
                            "Not found" -> android.graphics.Color.parseColor("#D32F2F")
                            else -> android.graphics.Color.parseColor("#FFFFFF")
                        }
                        val cell = android.widget.LinearLayout(context).apply {
                            orientation = android.widget.LinearLayout.VERTICAL
                            layoutParams = android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                if (idx != 0) setMargins(12, 0, 0, 0) // Add left margin except for first cell
                            }
                            gravity = android.view.Gravity.CENTER
                        }
                        cell.addView(android.widget.TextView(context).apply {
                            text = label
                            textSize = 14f
                            setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
                            gravity = android.view.Gravity.CENTER
                        })
                        cell.addView(android.widget.TextView(context).apply {
                            text = value.toString()
                            textSize = 13f
                            setTextColor(color)
                            gravity = android.view.Gravity.CENTER
                        })
                        breakdownRow.addView(cell)
                    }
                    addView(breakdownRow)
                }
                rowLayout.addView(breakdownView)
                // Click to expand/collapse
                mainRow.setOnClickListener {
                    // Collapse all
                    for (i in 0 until locationTable.childCount) {
                        val child = locationTable.getChildAt(i)
                        if (child is android.widget.LinearLayout) {
                            val breakdownChild = child.getChildAt(1)
                            breakdownChild.visibility = View.GONE
                        }
                    }
                    // Expand this
                    breakdownView.visibility = View.VISIBLE
                }
                locationTable.addView(rowLayout)
                // Divider
                if (idx < locations.size - 1) {
                    locationTable.addView(android.view.View(context).apply {
                        setBackgroundColor(android.graphics.Color.parseColor("#444444"))
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            2
                        )
                    })
                }
            }
            locationTableScroll.addView(locationTable)
            locationTableContainer.addView(locationTableScroll)
            locationTableContainer.addView(headerLayout)
            // Add both sections to the root layout
            contentRoot.addView(summarySection)
            contentRoot.addView(summaryTableGap)
            contentRoot.addView(locationTableContainer)
            dialog.setContentView(popupView)

            dialog.show()
        }

        fun showMonthYearReportPopup(
            context: Context,
            title: String,
            monthYearBreakdown: Map<String, Map<String, Int>>,
            tableHeaderMonthYear: String,
            tableHeaderObservations: String
        ) {
            val dialog = Dialog(context)
            dialog.window?.setLayout(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
            val inflater = LayoutInflater.from(context)
            val popupView = inflater.inflate(R.layout.popup, null)
            val contentRoot = popupView.findViewById<android.widget.LinearLayout>(R.id.popup_content_root)
            contentRoot.removeAllViews()
            if (contentRoot.layoutParams == null) {
                contentRoot.layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            } else {
                contentRoot.layoutParams.width = android.widget.LinearLayout.LayoutParams.MATCH_PARENT
            }
            // --- Section 1: Header ---
            val reportHeaderLayout = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 0, 0, 0)
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            val titleView = android.widget.TextView(context).apply {
                text = title
                textSize = 16f // Smaller font size
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                gravity = android.view.Gravity.CENTER_VERTICAL
                setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            }
            val closeButton = android.widget.ImageButton(context).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                val sizePx = (context.resources.displayMetrics.density * 24).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(sizePx, sizePx).apply {
                    gravity = android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL
                }
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setOnClickListener { dialog.dismiss() }
            }
            reportHeaderLayout.addView(titleView)
            reportHeaderLayout.addView(closeButton)
            contentRoot.addView(reportHeaderLayout)
            // Gap
            val headerBodyGap = android.view.View(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 24)
            }
            contentRoot.addView(headerBodyGap)
            // --- Section 2: Summary ---
            // Calculate summary values
            val summaryLabels = listOf("Total", "Seen", "Heard", "Not found")
            var total = 0
            var seen = 0
            var heard = 0
            var notFound = 0
            monthYearBreakdown.values.forEach { breakdown ->
                total += breakdown.values.sum()
                seen += breakdown["Seen"] ?: 0
                heard += breakdown["Heard"] ?: 0
                notFound += breakdown["Not found"] ?: 0
            }
            val percents = listOf(
                100.0,
                if (total > 0) seen / total.toDouble() * 100 else 0.0,
                if (total > 0) heard / total.toDouble() * 100 else 0.0,
                if (total > 0) notFound / total.toDouble() * 100 else 0.0
            )
            val summaryValues = listOf(total, seen, heard, notFound)
            val labelColor = android.graphics.Color.parseColor("#FFFFFF")
            val colors = listOf(
                labelColor,
                android.graphics.Color.parseColor("#388E3C"),
                android.graphics.Color.parseColor("#1976D2"),
                android.graphics.Color.parseColor("#D32F2F")
            )
            val summarySection = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            val labelRow = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            summaryLabels.forEach { label ->
                labelRow.addView(android.widget.TextView(context).apply {
                    text = label
                    textSize = 14f
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    gravity = android.view.Gravity.CENTER
                    setTextColor(labelColor)
                })
            }
            summarySection.addView(labelRow)
            val valueRow = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            // Month-year report summary section
            summaryValues.indices.forEach { i ->
                val number = summaryValues[i]
                val percent = percents[i]
                val color = colors[i]
                valueRow.addView(android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    gravity = android.view.Gravity.CENTER
                    addView(android.widget.TextView(context).apply {
                        text = number.toString()
                        textSize = 13f
                        setTextColor(color)
                        gravity = android.view.Gravity.CENTER
                    })
                    if (percent != 0.0) {
                        val percentText = "(${formatPercent(percent)}%)"
                        addView(android.widget.TextView(context).apply {
                            text = percentText
                            textSize = 12f
                            setTextColor(labelColor)
                            gravity = android.view.Gravity.CENTER
                        })
                    }
                })
            }
            summarySection.addView(valueRow)
            contentRoot.addView(summarySection)
            // Gap between summary and table
            val summaryTableGap = android.view.View(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 32)
            }
            contentRoot.addView(summaryTableGap)
            // --- Section 3: Breakdown Table ---
            val headerHeightPx = (context.resources.displayMetrics.density * 40).toInt()
            val tableContainer = android.widget.FrameLayout(context).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    800
                )
            }
            val tableHeaderRow = android.widget.TableRow(context)
            val monthYearHeader = android.widget.TextView(context).apply {
                text = tableHeaderMonthYear
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(labelColor)
                textSize = 13f
                setBackgroundColor(android.graphics.Color.parseColor("#333333"))
                setPadding(24, 12, 8, 12)
                layoutParams = android.widget.TableRow.LayoutParams(0, android.widget.TableRow.LayoutParams.WRAP_CONTENT, 2f)
                elevation = 8f
            }
            val countHeader = android.widget.TextView(context).apply {
                text = tableHeaderObservations
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(labelColor)
                textSize = 13f
                setBackgroundColor(android.graphics.Color.parseColor("#333333"))
                setPadding(8, 12, 24, 12)
                gravity = android.view.Gravity.END
                layoutParams = android.widget.TableRow.LayoutParams(0, android.widget.TableRow.LayoutParams.WRAP_CONTENT, 1f)
                elevation = 8f
            }
            tableHeaderRow.addView(monthYearHeader)
            tableHeaderRow.addView(countHeader)
            val headerLayout = android.widget.TableLayout(context).apply {
                layoutParams = android.widget.TableLayout.LayoutParams(
                    android.widget.TableLayout.LayoutParams.MATCH_PARENT,
                    headerHeightPx
                )
                z = 10f
            }
            headerLayout.addView(tableHeaderRow)
            val tableScroll = android.widget.ScrollView(context).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
                setPadding(0, headerHeightPx, 0, 0)
            }
            val tableRows = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            // Group and sum breakdowns by normalized month-year
            val groupedMonthYearBreakdown = mutableMapOf<String, MutableMap<String, Int>>()
            monthYearBreakdown.forEach { (rawMonthYear, breakdown) ->
                val inputFormats = listOf(
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.UK),
                    java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.UK),
                    java.text.SimpleDateFormat("dd MMM yy", java.util.Locale.UK),
                    java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.UK),
                    java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.UK)
                )
                val date = inputFormats.firstNotNullOfOrNull { format ->
                    try { format.parse(rawMonthYear) } catch (_: Exception) { null }
                }
                val normalizedMonthYear = if (date != null) java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.UK).format(date) else rawMonthYear
                val group = groupedMonthYearBreakdown.getOrPut(normalizedMonthYear) { mutableMapOf() }
                breakdown.forEach { (k, v) -> group[k] = (group[k] ?: 0) + v }
            }
            val monthYears = groupedMonthYearBreakdown.keys.toList()
            monthYears.forEachIndexed { idx, formattedMonthYear ->
                val breakdown = groupedMonthYearBreakdown[formattedMonthYear] ?: emptyMap()
                val count = breakdown.values.sum()
                val rowLayout = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                val mainRow = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setPadding(0, 0, 0, 0)
                }
                val monthYearCell = android.widget.TextView(context).apply {
                    text = formattedMonthYear
                    setTextColor(labelColor)
                    textSize = 15f
                    setPadding(24, 12, 8, 12)
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                }
                val countCell = android.widget.TextView(context).apply {
                    text = count.toString()
                    setTextColor(labelColor)
                    textSize = 15f
                    setPadding(8, 12, 24, 12)
                    gravity = android.view.Gravity.END
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                mainRow.addView(monthYearCell)
                mainRow.addView(countCell)
                rowLayout.addView(mainRow)
                // Breakdown view (hidden by default)
                val breakdownView = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    setBackgroundColor(android.graphics.Color.parseColor("#222222"))
                    setPadding(8, 4, 8, 4)
                    visibility = View.GONE
                    val breakdownLabels = breakdown.keys.toList()
                    val breakdownRow = android.widget.LinearLayout(context).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        setPadding(0, 0, 0, 0)
                    }
                    breakdownLabels.forEachIndexed { idx, label ->
                        val value = breakdown[label] ?: 0
                        val color = when (label) {
                            "Seen" -> android.graphics.Color.parseColor("#388E3C")
                            "Heard" -> android.graphics.Color.parseColor("#1976D2")
                            "Not found" -> android.graphics.Color.parseColor("#D32F2F")
                            else -> labelColor
                        }
                        val cell = android.widget.LinearLayout(context).apply {
                            orientation = android.widget.LinearLayout.VERTICAL
                            layoutParams = android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                if (idx != 0) setMargins(12, 0, 0, 0)
                            }
                            gravity = android.view.Gravity.CENTER
                        }
                        cell.addView(android.widget.TextView(context).apply {
                            text = label
                            textSize = 14f
                            setTextColor(labelColor)
                            gravity = android.view.Gravity.CENTER
                        })
                        cell.addView(android.widget.TextView(context).apply {
                            text = value.toString()
                            textSize = 13f
                            setTextColor(color)
                            gravity = android.view.Gravity.CENTER
                        })
                        breakdownRow.addView(cell)
                    }
                    addView(breakdownRow)
                }
                rowLayout.addView(breakdownView)
                // Click to expand/collapse
                mainRow.setOnClickListener {
                    for (i in 0 until tableRows.childCount) {
                        val child = tableRows.getChildAt(i)
                        if (child is android.widget.LinearLayout) {
                            val breakdownChild = child.getChildAt(1)
                            breakdownChild.visibility = View.GONE
                        }
                    }
                    breakdownView.visibility = View.VISIBLE
                }
                tableRows.addView(rowLayout)
                // Divider
                if (idx < monthYears.size - 1) {
                    tableRows.addView(android.view.View(context).apply {
                        setBackgroundColor(android.graphics.Color.parseColor("#444444"))
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 2)
                    })
                }
            }
            tableScroll.addView(tableRows)
            tableContainer.addView(tableScroll)
            tableContainer.addView(headerLayout)
            contentRoot.addView(tableContainer)
            dialog.setContentView(popupView)
            dialog.show()
        }

        // Format percentage to 2 decimal places, remove trailing zeros
        private fun formatPercent(percent: Double): String {
            val formatted = String.format("%.2f", percent).trimEnd('0').trimEnd('.')
            return formatted
        }

        fun normalizeToMonthYear(dateStr: String): String {
            // Try dd/MM/yyyy first
            try {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
                val date = sdf.parse(dateStr)
                val outFormat = java.text.SimpleDateFormat("MMM-yy", java.util.Locale.US)
                return outFormat.format(date)
            } catch (_: Exception) {}
            // Try MMM-yy (already normalized)
            try {
                val sdf = java.text.SimpleDateFormat("MMM-yy", java.util.Locale.US)
                val date = sdf.parse(dateStr)
                val outFormat = java.text.SimpleDateFormat("MMM-yy", java.util.Locale.US)
                return outFormat.format(date)
            } catch (_: Exception) {}
            // If not parsable, return as is
            return dateStr
        }
    }
}