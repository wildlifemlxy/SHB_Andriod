package com.ecss.shb_andriod.pages

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ecss.shb_andriod.R
import com.ecss.shb_andriod.api.SurveyApi
import com.ecss.shb_andriod.model.PurposeRequest
import com.ecss.shb_andriod.model.Survey
import com.ecss.shb_andriod.view.PieChartView
import com.ecss.shb_andriod.view.LoadingView
import com.ecss.shb_andriod.view.LineChartView
import com.ecss.shb_andriod.view.TreeChartView
import com.ecss.shb_andriod.view.TreePercentageChartView
import com.github.mikephil.charting.data.PieEntry
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
    private lateinit var loadingView: LoadingView
    private var reportType: String = "location" // Track current report type

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
        loadingView = LoadingView(this)

        btnPieChart.setOnClickListener {
            pieChartView.visibility = View.VISIBLE
            lineChartView.visibility = View.GONE
            treeChartView.visibility = View.GONE
            treePercentageChartView.visibility = View.GONE
            btnGenerateReport.text = "Generate Report"
            btnGenerateReport.visibility = View.VISIBLE
            reportType = "location" // Pie chart = location report
        }
        btnLineChart.setOnClickListener {
            pieChartView.visibility = View.GONE
            lineChartView.visibility = View.VISIBLE
            treeChartView.visibility = View.GONE
            treePercentageChartView.visibility = View.GONE
            btnGenerateReport.text = "Generate Report"
            btnGenerateReport.visibility = View.VISIBLE
            reportType = "monthYear" // Line chart = month/year report
        }
        btnTreeHeightChart.setOnClickListener {
            pieChartView.visibility = View.GONE
            lineChartView.visibility = View.GONE
            isPercentageMode = false
            treeChartView.visibility = View.VISIBLE
            treePercentageChartView.visibility = View.GONE
            btnGenerateReport.visibility = View.VISIBLE
            btnGenerateReport.text = "Show Percentage"
            // Optionally set reportType if needed for tree chart
        }
        btnGenerateReport.setOnClickListener {
            val seenHeardCounts = surveys.filter { !it.seenHeard.isNullOrBlank() }
                .groupingBy { it.seenHeard!!.trim() }
                .eachCount()
            val seenCount = seenHeardCounts["Seen"] ?: 0
            val heardCount = seenHeardCounts["Heard"] ?: 0
            val notFoundCount = seenHeardCounts["Not found"] ?: 0
            val seenHeardTotal = seenCount + heardCount + notFoundCount
            val seenHeardBreakdown = "Seen: $seenCount, Heard: $heardCount, Not found: $notFoundCount"
            // Prepare locationBreakdown for ReportView
            val locationCounts = countObservationsByLocation(surveys)
            val locationBreakdown = locationCounts.mapValues { (location, count) ->
                val breakdown = surveys.filter { (it.location ?: "Unknown") == location }
                    .groupingBy { it.seenHeard ?: "Unknown" }
                    .eachCount()
                Pair(count, breakdown)
            }
            // Show only one report popup depending on observation type
            val selectedReportType = getSelectedReportType() // Implement this function to return "location" or "monthYear"
            if (selectedReportType == "location") {
                com.ecss.shb_andriod.view.ReportView.showReportPopup(
                    context = this,
                    title = "Report by Location",
                    locationBreakdown = locationBreakdown,
                    tableHeaderLocation = "Location",
                    tableHeaderObservations = "Observations",
                    seenHeardTotal = seenHeardTotal,
                    seenHeardBreakdown = seenHeardBreakdown
                )
            } else if (selectedReportType == "monthYear") {
                val monthYearBreakdown = surveys.groupBy { com.ecss.shb_andriod.view.ReportView.normalizeToMonthYear(it.date ?: "") }
                    .mapValues { entry ->
                        entry.value.groupingBy { it.seenHeard ?: "Unknown" }.eachCount()
                    }
                com.ecss.shb_andriod.view.ReportView.showMonthYearReportPopup(
                    context = this,
                    title = "Observations by Month Year",
                    monthYearBreakdown = monthYearBreakdown,
                    tableHeaderMonthYear = "Month Year",
                    tableHeaderObservations = "Observations"
                )
            }
        }
        getAllSurveys()
        pieChartView.attachCustomMarker(this)
    }

    private fun getAllSurveys() {
        loadingView.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simulate network call or use your actual API call
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://shb-backend.azurewebsites.net/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val surveyApi = retrofit.create(SurveyApi::class.java)
                val request = PurposeRequest(purpose = "retrieveAndriod")
                val response = withContext(Dispatchers.IO) { surveyApi.getSurveysRaw(request) }
                surveys = response.result?.surveys ?: emptyList<Survey>()
                Log.d("DataVisualizationActivity", "Retrieved ${surveys}")

                val locationCounts: Map<String, Int> = countObservationsByLocation(surveys)
                val totalObservations: Int = locationCounts.values.sum()
                val chartData: List<PieEntry> = locationCounts.map { entry: Map.Entry<String, Int> ->
                    val location = entry.key
                    val count = entry.value
                    val breakdownMap: Map<String, Int> = surveys.filter { (it.location ?: "Unknown") == location }
                        .groupingBy { it.seenHeard ?: "Unknown" }
                        .eachCount()
                    val percentage: Double = if (totalObservations > 0) count * 100.0 / totalObservations else 0.0
                    PieEntry(
                        count.toFloat(),
                        location,
                        mapOf(
                            "percentage" to percentage,
                            "breakdown" to breakdownMap
                        )
                    )
                }
                withContext(Dispatchers.Main) {
                    loadingView.hide()
                    // Setup PieChartView (custom view for pie chart)
                    val btnGenerateReport = findViewById<android.widget.Button>(R.id.btnGenerateReport)
                    val tvNoData = findViewById<View>(R.id.tvNoData)
                    pieChartView.visibility = View.GONE
                    lineChartView.visibility = View.GONE
                    tvNoData.visibility = View.GONE

                    Log.d("DataVisualizationActivity", "Setting pie chart data: $chartData")
                    pieChartView.showPieChart(chartData)

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
                    val monthYearCounts: Map<String, Pair<Int, Map<String, Int>>> = countObservationsByMonthYearWithSeenHeard(surveys)
                    val monthYearLabels: List<String> = monthYearCounts.keys.toList()
                    val totalCounts: List<Float> = monthYearCounts.values.map { it.first.toFloat() }
                    val seenHeardTypes: Set<String> = monthYearCounts.values.flatMap { it.second.keys }.toSet()
                    val breakdowns: Map<String, List<Float>> = seenHeardTypes.associateWith { type ->
                        monthYearLabels.map { label ->
                            monthYearCounts[label]?.second?.get(type)?.toFloat() ?: 0f
                        }
                    }
                    lineChartView.setLineChartData(monthYearLabels, totalCounts, breakdowns)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingView.hide()
                    // Handle error (show a message, etc.)
                }
            }
        }
    }

    // Move helper functions to class level
    fun countObservationsByLocation(surveys: List<Survey>): Map<String, Int> {
        val locationCounts = mutableMapOf<String, Int>()
        for (survey in surveys) {
            val location = survey.location ?: "Unknown"
            locationCounts[location] = locationCounts.getOrDefault(location, 0) + 1
            Log.d("DataVisualizationActivity","Location: $location, Count: ${locationCounts[location]}")
        }
        return locationCounts
    }

    fun countObservationsByMonthYearWithSeenHeard(surveys: List<Survey>): Map<String, Pair<Int, Map<String, Int>>> {
        val monthYearCounts = mutableMapOf<String, Pair<Int, Map<String, Int>>>()
        val inputFormats = listOf(
            java.text.SimpleDateFormat("dd-MMM-yy", java.util.Locale.UK),
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.UK)
        )
        for (survey in surveys) {
            val dateStr = survey.date ?: continue
            val date = inputFormats.firstNotNullOfOrNull { format ->
                try { format.parse(dateStr) } catch (_: Exception) { null }
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

    // Helper function to determine which report type to show
    private fun getSelectedReportType(): String {
        return reportType
    }
}