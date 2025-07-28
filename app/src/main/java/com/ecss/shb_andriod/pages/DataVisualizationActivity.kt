package com.ecss.shb_andriod.pages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ecss.shb_andriod.R
import com.ecss.shb_andriod.api.SurveyApi
import com.ecss.shb_andriod.base.BaseActivity
import com.ecss.shb_andriod.model.PurposeRequest
import com.ecss.shb_andriod.model.Survey
import com.ecss.shb_andriod.view.LoadingView
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ecss.shb_andriod.fragments.LineChartFragment
import com.ecss.shb_andriod.fragments.PieChartFragment
import com.ecss.shb_andriod.fragments.TreeChartFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.ecss.shb_andriod.view.TreeChartView
import com.ecss.shb_andriod.view.TreePercentageChartView
import com.ecss.shb_andriod.model.ChartDataViewModel

class DataVisualizationActivity : BaseActivity() {
    private var surveys: List<Survey> = emptyList()
    private lateinit var loadingView: LoadingView
    private var reportType: String = "location" // Track current report type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DataVisualizationActivity", "onCreate called")
        // Hide system UI for fullscreen (top and bottom nav bars)
        // Hide system UI for fullscreen (top and bottom nav bars)
        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                        or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )

        setupDrawerAndToolbar(R.layout.activity_data_visualization)
        loadingView = LoadingView(this)
        val chartDataViewModel = ViewModelProvider(this)[ChartDataViewModel::class.java]
        Log.d("DataVisualizationActivity", "Calling getAllSurveys...")
        getAllSurveys(chartDataViewModel)
        Log.d("DataVisualizationActivity", "After getAllSurveys...")
        // Remove direct chart view references
        // val btnPieChart = findViewById<Button>(R.id.btnPieChart)
        // val btnLineChart = findViewById<Button>(R.id.btnLineChart)
        // val btnTreeHeightChart = findViewById<Button>(R.id.btnTreeHeightChart)
        val btnGenerateReport = findViewById<Button>(R.id.btnGenerateReport)

        // Setup ViewPager2 and TabLayout
        val viewPager = findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.chartViewPager)
        val tabLayout = findViewById<com.google.android.material.tabs.TabLayout>(R.id.chartTabLayout)
        val chartFragments = listOf(
            PieChartFragment(),
            LineChartFragment(),
            TreeChartFragment()
        )
        val chartTitles = listOf("Observations by Location", "Observations by Month Year", "Observations by Trees")
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = chartFragments.size
            override fun createFragment(position: Int): Fragment = chartFragments[position]
        }
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = chartTitles[position]
        }.attach()

        // Use MaterialButton instead of ImageButton for nav bar
        val btnHome = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnHome)
        val btnBack = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBack)
        btnHome.setOnClickListener {
            // Go to main page (MainActivity)
            val intent = Intent(this, MainPage::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        btnBack.setOnClickListener {
            // Go to previous page
            onBackPressedDispatcher.onBackPressed()
        }

        btnGenerateReport.setOnClickListener {
            if (btnGenerateReport.text == "Show Percentage") {
                // treeChartView.visibility = View.GONE
                // treePercentageChartView.visibility = View.VISIBLE
                btnGenerateReport.text = "Show Tree Height"
            } else if (btnGenerateReport.text == "Show Tree Height") {
                // treeChartView.visibility = View.VISIBLE
                // treePercentageChartView.visibility = View.GONE
                btnGenerateReport.text = "Show Percentage"
            } else {
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
        }
    }

    private fun getAllSurveys(chartDataViewModel: ChartDataViewModel) {
        Log.d("DataVisualizationActivity", "getAllSurveys called")
        loadingView.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("DataVisualizationActivity", "Starting network call for surveys...")
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://shb-backend.azurewebsites.net/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val surveyApi = retrofit.create(SurveyApi::class.java)
                Log.d("DataVisualizationActivity", "Survey API: $surveyApi")
                val request = PurposeRequest(purpose = "retrieveAndriod")
                Log.d("DataVisualizationActivity", "Request body: $request")
                val response = withContext(Dispatchers.IO) {
                    try {
                        surveyApi.getSurveysRaw(request)
                    } catch (apiEx: Exception) {
                        Log.e("DataVisualizationActivity", "Exception in getSurveysRaw: ${apiEx.message}", apiEx)
                        throw apiEx
                    }
                }
                Log.d("DataVisualizationActivity", "API response: $response")
                surveys = response.result?.surveys ?: emptyList<Survey>()
                Log.d("DataVisualizationActivity", "Retrieved surveys size: ${surveys.size}")
                surveys.forEach { Log.d("DataVisualizationActivity", "Survey: $it") }

                // --- Build breakdown map for PieChart markers ---
                val locationCounts = countObservationsByLocation(surveys)
                val breakdownMapByLabel = locationCounts.mapValues { (location, _) ->
                    listOf("Seen", "Heard", "Not found").associateWith { seenHeardType ->
                        surveys.count { (it.location ?: "Unknown") == location && (it.seenHeard ?: "Unknown") == seenHeardType }
                    }
                }
                // --- Build PieEntry list with breakdown in data ---
                val totalObservations = locationCounts.values.sum()
                val chartData: List<PieEntry> = locationCounts.map { entry ->
                    val location = entry.key
                    val count = entry.value
                    val breakdownMap = breakdownMapByLabel[location] ?: emptyMap()
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

                withContext(Dispatchers.Main) {
                    loadingView.hide()
                    // Update ViewModel with surveys
                    chartDataViewModel.setSurveys(surveys)
                    val btnGenerateReport = findViewById<android.widget.Button>(R.id.btnGenerateReport)
                    val tvNoData = findViewById<View>(R.id.tvNoData)
                    tvNoData.visibility = if (surveys.isEmpty()) View.VISIBLE else View.GONE
                    btnGenerateReport.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingView.hide()
                    Log.e("DataVisualizationActivity", "Error loading surveys: ${e.message}", e)
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

    // Helper function to determine which report type to show
    private fun getSelectedReportType(): String {
        return reportType
    }
}