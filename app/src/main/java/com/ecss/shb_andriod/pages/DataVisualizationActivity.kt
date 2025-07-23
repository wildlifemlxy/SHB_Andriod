package com.ecss.shb_andriod.pages

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ecss.shb_andriod.R
import com.ecss.shb_andriod.api.SurveyApi
import com.ecss.shb_andriod.model.PurposeRequest
import com.ecss.shb_andriod.model.Survey
import com.ecss.shb_andriod.view.PieChartView
import com.ecss.shb_andriod.view.LineChartView
import com.ecss.shb_andriod.view.TreeChartView
import com.ecss.shb_andriod.view.TreePercentageChartView
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DataVisualizationActivity : AppCompatActivity() {
    private var surveys: List<Survey> = emptyList()
    private lateinit var pieChartView: PieChartView
    private lateinit var lineChartView: LineChartView
    private lateinit var treeChartView: TreeChartView
    private lateinit var treePercentageChartView: TreePercentageChartView
    private var isPercentageMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_visualization)
        val btnPieChart = findViewById<Button>(R.id.btnPieChart)
        val btnLineChart = findViewById<Button>(R.id.btnLineChart)
        val btnTreeHeightChart = findViewById<Button>(R.id.btnTreeHeightChart)
        val btnGenerateReport = findViewById<Button>(R.id.btnGenerateReport)
        pieChartView = findViewById(R.id.pieChartView)
        lineChartView = findViewById(R.id.lineChartView)
        treeChartView = findViewById(R.id.treeChartView)
        // Add percentage chart view
        treePercentageChartView = findViewById(R.id.treePercentageChartView)

        // Show pie chart by default with real data (after surveys are loaded)
        pieChartView.visibility = View.GONE // Hide until data is loaded

        btnPieChart.setOnClickListener {
            pieChartView.visibility = View.VISIBLE
            lineChartView.visibility = View.GONE
            treeChartView.visibility = View.GONE
            treePercentageChartView.visibility = View.GONE
            // Remove sampleEntries and showPieChart call
            // The pie chart will be shown after getAllSurveys() loads data
        }
        btnLineChart.setOnClickListener {
            pieChartView.visibility = View.GONE
            lineChartView.visibility = View.VISIBLE
            treeChartView.visibility = View.GONE
            treePercentageChartView.visibility = View.GONE
            // Hide marker for line chart
            lineChartView.findViewById<com.github.mikephil.charting.charts.LineChart>(R.id.lineChart)?.highlightValues(null)
        }
        btnTreeHeightChart.setOnClickListener {
            pieChartView.visibility = View.GONE
            lineChartView.visibility = View.GONE
            isPercentageMode = false
            treeChartView.visibility = View.VISIBLE
            treePercentageChartView.visibility = View.GONE
            btnGenerateReport.visibility = View.VISIBLE
            btnGenerateReport.text = "Show Percentage"
        }
        btnGenerateReport.setOnClickListener {
            // Only toggle between raw and percentage chart views for tree heights, no popup
            if (treeChartView.visibility == View.VISIBLE || treePercentageChartView.visibility == View.VISIBLE) {
                isPercentageMode = !isPercentageMode
                treeChartView.visibility = if (!isPercentageMode) View.VISIBLE else View.GONE
                treePercentageChartView.visibility = if (isPercentageMode) View.VISIBLE else View.GONE
                btnGenerateReport.text = if (isPercentageMode) "Show Raw Data" else "Show Percentage"
            } else if (pieChartView.visibility == View.VISIBLE || lineChartView.visibility == View.VISIBLE) {
                // Show popup for PieChartView or LineChartView
                val dialog = Dialog(this)
                val popupView = LayoutInflater.from(this).inflate(R.layout.popup, null)
                dialog.setContentView(popupView)
                dialog.setCancelable(true)
                val closeBtn = popupView.findViewById<ImageButton>(R.id.popup_close)
                closeBtn.setOnClickListener { dialog.dismiss() }
                val popupTitle = popupView.findViewById<TextView>(R.id.popup_title)
                popupTitle.text = "Report Summary"
                val progressBar = popupView.findViewById<View>(R.id.popup_progress)
                progressBar.visibility = View.GONE
                val summaryLayout = popupView.findViewById<LinearLayout>(R.id.popup_summary_breakdown)
                summaryLayout.removeAllViews()
                // Example: Add total summary
                val totalLayout = LinearLayout(this)
                totalLayout.orientation = LinearLayout.VERTICAL
                val totalLabel = TextView(this)
                totalLabel.text = "Total"
                totalLabel.setTextColor(android.graphics.Color.WHITE)
                totalLabel.textSize = 14f
                val totalNumber = TextView(this)
                totalNumber.text = surveys.size.toString()
                totalNumber.setTextColor(android.graphics.Color.WHITE)
                totalNumber.textSize = 16f
                totalLayout.addView(totalLabel)
                totalLayout.addView(totalNumber)
                totalLayout.setPadding(16, 0, 16, 0)
                summaryLayout.addView(totalLayout)
                // Example: Add breakdown by location
                val locationCounts = countObservationsByLocation(surveys)
                locationCounts.forEach { (location, count) ->
                    val itemLayout = LinearLayout(this)
                    itemLayout.orientation = LinearLayout.VERTICAL
                    val label = TextView(this)
                    label.text = location
                    label.setTextColor(android.graphics.Color.WHITE)
                    label.textSize = 14f
                    val number = TextView(this)
                    number.text = count.toString()
                    number.setTextColor(android.graphics.Color.LTGRAY)
                    number.textSize = 16f
                    itemLayout.addView(label)
                    itemLayout.addView(number)
                    itemLayout.setPadding(16, 0, 16, 0)
                    summaryLayout.addView(itemLayout)
                }
                dialog.show()
            }
        }
        val loadingPopup = findViewById<View>(R.id.loadingPopup)
        getAllSurveys()
        pieChartView.attachCustomMarker(this)
        pieChartView.visibility = View.VISIBLE

        btnGenerateReport.setOnClickListener {
            // Only toggle between raw and percentage chart views for tree heights, no popup
            if (treeChartView.visibility == View.VISIBLE || treePercentageChartView.visibility == View.VISIBLE) {
                isPercentageMode = !isPercentageMode
                treeChartView.visibility = if (!isPercentageMode) View.VISIBLE else View.GONE
                treePercentageChartView.visibility = if (isPercentageMode) View.VISIBLE else View.GONE
                btnGenerateReport.text = if (isPercentageMode) "Show Raw Data" else "Show Percentage"
            } else if (pieChartView.visibility == View.VISIBLE || lineChartView.visibility == View.VISIBLE) {
                // Show popup for PieChartView or LineChartView
                val dialog = Dialog(this)
                val popupView = LayoutInflater.from(this).inflate(R.layout.popup, null)
                dialog.setContentView(popupView)
                dialog.setCancelable(true)
                val closeBtn = popupView.findViewById<ImageButton>(R.id.popup_close)
                closeBtn.setOnClickListener { dialog.dismiss() }
                val popupTitle = popupView.findViewById<TextView>(R.id.popup_title)
                popupTitle.text = "Report Summary"
                val progressBar = popupView.findViewById<View>(R.id.popup_progress)
                progressBar.visibility = View.GONE
                val summaryLayout = popupView.findViewById<LinearLayout>(R.id.popup_summary_breakdown)
                summaryLayout.removeAllViews()
                // Example: Add total summary
                val totalLayout = LinearLayout(this)
                totalLayout.orientation = LinearLayout.VERTICAL
                val totalLabel = TextView(this)
                totalLabel.text = "Total"
                totalLabel.setTextColor(android.graphics.Color.WHITE)
                totalLabel.textSize = 14f
                val totalNumber = TextView(this)
                totalNumber.text = surveys.size.toString()
                totalNumber.setTextColor(android.graphics.Color.WHITE)
                totalNumber.textSize = 16f
                totalLayout.addView(totalLabel)
                totalLayout.addView(totalNumber)
                totalLayout.setPadding(16, 0, 16, 0)
                summaryLayout.addView(totalLayout)
                // Example: Add breakdown by location
                val locationCounts = countObservationsByLocation(surveys)
                locationCounts.forEach { (location, count) ->
                    val itemLayout = LinearLayout(this)
                    itemLayout.orientation = LinearLayout.VERTICAL
                    val label = TextView(this)
                    label.text = location
                    label.setTextColor(android.graphics.Color.WHITE)
                    label.textSize = 14f
                    val number = TextView(this)
                    number.text = count.toString()
                    number.setTextColor(android.graphics.Color.LTGRAY)
                    number.textSize = 16f
                    itemLayout.addView(label)
                    itemLayout.addView(number)
                    itemLayout.setPadding(16, 0, 16, 0)
                    summaryLayout.addView(itemLayout)
                }
                dialog.show()
            }
        }
    }

    private fun getAllSurveys() {
        CoroutineScope(Dispatchers.Main).launch {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://shb-backend.azurewebsites.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val surveyApi = retrofit.create(SurveyApi::class.java)
            val request = PurposeRequest(purpose = "retrieveAndriod")
            val response = withContext(Dispatchers.IO) { surveyApi.getSurveysRaw(request) }
            surveys = response.result?.surveys ?: emptyList<Survey>()
            Log.d("DataVisualizationActivity", "Retrieved ${surveys}")

            val locationCounts = countObservationsByLocation(surveys)
            val totalObservations = locationCounts.values.sum()
            val chartData = locationCounts.map { (location, count) ->
                val breakdownMap = surveys.filter { (it.location ?: "Unknown") == location }
                    .groupingBy { it.seenHeard ?: "Unknown" }
                    .eachCount()
                val percentage = if (totalObservations > 0) count * 100.0 / totalObservations else 0.0
                PieEntry(
                    count.toFloat(),
                    location,
                    mapOf(
                        "percentage" to percentage,
                        "breakdown" to breakdownMap
                    )
                )
            }
            // Setup PieChartView (custom view for pie chart)
            val btnGenerateReport = findViewById<android.widget.Button>(R.id.btnGenerateReport)
            val progressBar = findViewById<View>(R.id.progressBar)
            val tvNoData = findViewById<View>(R.id.tvNoData)
            val loadingPopup = findViewById<View>(R.id.loadingPopup)
            loadingPopup.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            pieChartView.visibility = View.GONE
            tvNoData.visibility = View.GONE

            Log.d("DataVisualizationActivity", "Setting pie chart data: $chartData")
            pieChartView.showPieChart(chartData)
            pieChartView.attachCustomMarker(this@DataVisualizationActivity)

            // Prepare data for TreeChartView
            val treeChartData = surveys.mapNotNull { survey ->
                TreeChartView.DataPoint(
                    observerName = survey.observerName,
                    shbIndividualId = survey.shbIndividualId,
                    numberOfBirds = survey.numberOfBirds,
                    location = survey.location,
                    date = survey.date,
                    time = survey.time,
                    heightOfTree = survey.heightOfTree,
                    heightOfBird = survey.heightOfBird,
                    activityType = survey.activityType,
                    seenHeard = survey.seenHeard,
                    activityDetails = survey.activityDetails,
                    activity = survey.activity
                )
            }
            val treePercentageChartData = surveys.mapNotNull { survey ->
                val label = survey.date ?: survey.location ?: ""
                val treeHeight = survey.heightOfTree?.toFloatOrNull()
                val birdHeight = survey.heightOfBird?.toFloatOrNull()
                if (treeHeight != null && birdHeight != null && treeHeight > 0f) {
                    val percentage = birdHeight / treeHeight
                    TreePercentageChartView.DataPoint(
                        label = label,
                        treeHeight = 1f,
                        birdHeight = percentage,
                        observerName = survey.observerName,
                        numberOfBirds = survey.numberOfBirds,
                        location = survey.location,
                        date = survey.date,
                        time = survey.time,
                        activityType = survey.activityType,
                        seenHeard = survey.seenHeard
                    )
                } else {
                    null
                }
            }
            treeChartView.setData(treeChartData)
            treePercentageChartView.setData(treePercentageChartData)

            loadingPopup.visibility = View.GONE
            btnGenerateReport.visibility = View.VISIBLE // Show button after fetching data
            // After loading data, only show the chart that matches the selected button
            if (surveys.isEmpty()) {
                pieChartView.visibility = View.GONE
                lineChartView.visibility = View.GONE
                tvNoData.visibility = View.VISIBLE
            } else {
                // Default: show pie chart, hide line chart
                pieChartView.visibility = View.VISIBLE
                lineChartView.visibility = View.GONE
                tvNoData.visibility = View.GONE
            }
            // Update line chart with month-year data
            val monthYearCounts = countObservationsByMonthYearWithSeenHeard(surveys)
            val monthYearLabels = monthYearCounts.keys.toList()
            val totalCounts = monthYearCounts.values.map { it.first.toFloat() }
            // Build breakdowns for each seenHeard type
            val seenHeardTypes = monthYearCounts.values.flatMap { it.second.keys }.toSet()
            val breakdowns = seenHeardTypes.associateWith { type ->
                monthYearLabels.map { label ->
                    monthYearCounts[label]?.second?.get(type)?.toFloat() ?: 0f
                }
            }
            lineChartView.setLineChartData(monthYearLabels, totalCounts, breakdowns)
        }
    }

    private fun countObservationsByLocation(surveys: List<Survey>): Map<String, Int> {
        val locationCounts = mutableMapOf<String, Int>()
        for (survey in surveys) {
            val location = survey.location ?: "Unknown"
            locationCounts[location] = locationCounts.getOrDefault(location, 0) + 1
            Log.d("DataVisualizationActivity","Location: $location, Count: ${locationCounts[location]}")
        }
        return locationCounts
    }

    private fun countObservationsByMonthYearWithSeenHeard(surveys: List<Survey>): Map<String, Pair<Int, Map<String, Int>>> {
        val monthYearCounts = mutableMapOf<String, Pair<Int, Map<String, Int>>>()
        val inputFormats = listOf(
            java.text.SimpleDateFormat("dd-MMM-yy", java.util.Locale.UK),
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.UK)
        )
        for (survey in surveys) {
            val dateStr = survey.date ?: continue
            val date = inputFormats.firstNotNullOfOrNull { format ->
                try { format.parse(dateStr) } catch (e: Exception) { null }
            } ?: continue
            val monthYear = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.UK).format(date)
            val seenHeard = survey.seenHeard ?: "Unknown"
            val (count, seenHeardMap) = monthYearCounts.getOrDefault(monthYear, Pair(0, mutableMapOf()))
            val newSeenHeardMap = seenHeardMap.toMutableMap()
            newSeenHeardMap[seenHeard] = newSeenHeardMap.getOrDefault(seenHeard, 0) + 1
            monthYearCounts[monthYear] = Pair(count + 1, newSeenHeardMap)
            Log.d("DataVisualizationActivity", "MonthYear: $monthYear, Count: ${count + 1}, SeenHeard: $newSeenHeardMap")
        }
        return monthYearCounts
    }
}