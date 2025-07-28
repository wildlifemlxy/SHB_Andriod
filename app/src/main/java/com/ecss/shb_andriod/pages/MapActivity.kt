package com.ecss.shb_andriod.pages


import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.ecss.shb_andriod.R
import com.ecss.shb_andriod.api.SurveyApi
import com.ecss.shb_andriod.base.BaseActivity
import com.ecss.shb_andriod.model.PurposeRequest
import com.ecss.shb_andriod.model.Survey
import com.ecss.shb_andriod.view.MapViewContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapActivity : BaseActivity() {
    private var mapViewContainer: MapViewContainer? = null
    private var isLive = false
    private var liveStatusTextView: TextView? = null
    private var liveUpdateHandler: android.os.Handler? = null
    private var liveUpdateRunnable: Runnable? = null
    private var surveys: List<Survey> = emptyList() // <-- Add this property

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the status bar and make the activity full screen
        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        supportActionBar?.hide()
        setupDrawerAndToolbar(R.layout.activity_map)
        mapViewContainer = findViewById(R.id.mapViewContainer)
        mapViewContainer?.onCreate(savedInstanceState)
        // Set up zoom level and live status listener
        mapViewContainer?.setOnZoomLevelChangeListener(object : MapViewContainer.OnZoomLevelChangeListener {
            override fun onZoomLevelChanged(zoom: Float) {
                val zoomTextView = findViewById<TextView>(R.id.tvMapZoomLevel)
                zoomTextView?.text = "Zoom: ${String.format("%.1f", zoom)}"
            }
        })

        // Clear More Information section on load
        findViewById<TextView>(R.id.tvMoreInfoContent)?.text = "Tap a marker to see more information."

        getAllSurveys() // Fetch surveys and update statistics/markers
    }

    private fun getAllSurveys() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://shb-backend.azurewebsites.net/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val surveyApi = retrofit.create(SurveyApi::class.java)
                val request = PurposeRequest(purpose = "retrieveAndriod")
                val response = withContext(Dispatchers.IO) { surveyApi.getSurveysRaw(request) }
                surveys = response.result?.surveys ?: emptyList<Survey>()
                Log.d("MapViewContainer", "Retrieved "+surveys)
                surveys.forEachIndexed { index, survey ->
                    Log.d("SurveyLatLong", "Survey #${index + 1}: Lat=${survey.lat}, Long=${survey.long}")
                }
                withContext(Dispatchers.Main) {
                    // Update statistics in the legend
                    val locationCount = surveys.size
                    val seenCount = surveys.count { it.seenHeard.equals("Seen", ignoreCase = true) }
                    val heardCount = surveys.count { it.seenHeard.equals("Heard", ignoreCase = true) }
                    val notFoundCount = surveys.count { it.seenHeard.equals("Not Found", ignoreCase = true) }
                    findViewById<TextView>(R.id.tvLegendStatsLocation).text = "Locations: $locationCount"
                    findViewById<TextView>(R.id.tvLegendStatsSeen).text = "Seen: $seenCount"
                    findViewById<TextView>(R.id.tvLegendStatsHeard).text = "Heard: $heardCount"
                    findViewById<TextView>(R.id.tvLegendStatsNotFound).text = "Not Found: $notFoundCount"
                    mapViewContainer?.setSurveys(surveys)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Handle error (show a message, etc.)
                }
            }
        }
    }

    /**
     * Render survey markers on the map using the current list of surveys.
     * This is a stub. You should implement marker rendering logic here.
     */
    private fun renderSurveyMarkers() {
        // Example: If your MapViewContainer has a method to add markers, call it here
        // mapViewContainer?.addSurveyMarkers(surveys)
        // For now, just log the surveys
        Log.d("MapActivity", "Rendering ${surveys.size} survey markers on the map.")
    }

    override fun onResume() {
        super.onResume()
        mapViewContainer?.onResume()
    }

    override fun onPause() {
        mapViewContainer?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapViewContainer?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapViewContainer?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapViewContainer?.onSaveInstanceState(outState)
    }
}
