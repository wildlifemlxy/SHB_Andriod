package com.ecss.shb_andriod.pages

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ecss.shb_andriod.R
import com.ecss.shb_andriod.api.SurveyApi
import com.ecss.shb_andriod.model.PurposeRequest
import com.ecss.shb_andriod.model.Survey
import com.ecss.shb_andriod.view.FullView
import com.ecss.shb_andriod.view.LoadingView
import com.ecss.shb_andriod.view.footer_pages_view
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SurveyActivity : AppCompatActivity() {

    private var currentPage = 1
    private var surveys: List<Survey> = emptyList()
    private var isPaginatedView = false
    private var loadingView: LoadingView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make activity full screen by default
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        setContentView(R.layout.activity_survey)
        val footerControls = findViewById<footer_pages_view>(R.id.footerControls)
        footerControls.visibility = View.GONE // Hide footer by default
        // Remove cardsPerPagesView listeners and logic
        // Use Spinner (selectBox) for cards per page selection
        getAllSurveys()

        val selectBox = findViewById<android.widget.Spinner>(R.id.spinnerCardsPerPage)
        footerControls.btnPrevPage.setOnClickListener {
            val pageSize = selectBox.selectedItem?.toString()?.toIntOrNull() ?: 5
            ((surveys.size + pageSize - 1) / pageSize).coerceAtLeast(1)
            if (currentPage > 1) {
                currentPage--
                showPaginatedView(pageSize)
                updatePageInfo(pageSize)
            }
        }
        footerControls.btnNextPage.setOnClickListener {
            val pageSize = selectBox.selectedItem?.toString()?.toIntOrNull() ?: 5
            val totalPages = ((surveys.size + pageSize - 1) / pageSize).coerceAtLeast(1)
            if (currentPage < totalPages) {
                currentPage++
                showPaginatedView(pageSize)
                updatePageInfo(pageSize)
            }
        }
        footerControls.btnFirstPage.setOnClickListener {
            val pageSize = selectBox.selectedItem?.toString()?.toIntOrNull() ?: 5
            currentPage = 1
            showPaginatedView(pageSize)
            updatePageInfo(pageSize)
        }
        footerControls.btnLastPage.setOnClickListener {
            val pageSize = selectBox.selectedItem?.toString()?.toIntOrNull() ?: 5
            val totalPages = ((surveys.size + pageSize - 1) / pageSize).coerceAtLeast(1)
            currentPage = totalPages
            showPaginatedView(pageSize)
            updatePageInfo(pageSize)
        }
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = android.content.Intent(this, MainPage::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        // Toggle between paginated and full view
        val btnToggleViewMode = findViewById<Button>(R.id.btnToggleViewMode)
        // val selectBox = findViewById<android.widget.Spinner>(R.id.spinnerCardsPerPage)
        var paginatedMode = false
        btnToggleViewMode.setOnClickListener {
            paginatedMode = !paginatedMode
            btnToggleViewMode.text = if (paginatedMode) "Full View" else "Paginated View"
            val selectBox = findViewById<android.widget.Spinner>(R.id.spinnerCardsPerPage)
            val labelCardsPerPage = findViewById<TextView>(R.id.labelCardsPerPage)
            // Always hide CardsPerPagesView (textbox)
            // Only show 4 options in select box
            val maxValue = surveys.size
            val values = mutableListOf<String>()
            val step = if (maxValue >= 4) maxValue / 4 else 1
            for (i in 1..4) {
                val value = (i * step).coerceAtMost(maxValue)
                values.add(value.toString())
            }
            val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            selectBox.adapter = adapter
            selectBox.setSelection(0)
            selectBox.visibility = if (paginatedMode) View.VISIBLE else View.GONE
            labelCardsPerPage.visibility = if (paginatedMode) View.VISIBLE else View.GONE
            val pageSize = selectBox.selectedItem?.toString()?.toIntOrNull() ?: 4
            if (paginatedMode) {
                showPaginatedView(pageSize)
                updatePageInfo(pageSize)
                val footerControls = findViewById<footer_pages_view>(R.id.footerControls)
                footerControls.visibility = View.VISIBLE
            } else {
                showFullView(surveys)
                val footerControls = findViewById<footer_pages_view>(R.id.footerControls)
                footerControls.visibility = View.GONE
            }
        }
        selectBox.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                if (paginatedMode) {
                    val pageSize = selectBox.selectedItem?.toString()?.toIntOrNull() ?: 4
                    currentPage = 1 // Reset to first page when changing page size
                    showPaginatedView(pageSize)
                    updatePageInfo(pageSize)
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
        // Remove references to spinnerCardsPerPage, labelCardsPerPage
    }

    private fun getAllSurveys() {
        showLoadingDialog()
        CoroutineScope(Dispatchers.Main).launch {
            // Fetch surveys from backend using Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl("https://shb-backend.azurewebsites.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val surveyApi = retrofit.create(SurveyApi::class.java)
            val request = PurposeRequest(purpose = "retrieveAndriod")
            val response = withContext(Dispatchers.IO) { surveyApi.getSurveysRaw(request) }
            surveys = response.result?.surveys ?: emptyList()
            Log.d("SurveyActivity", "Fetched ${surveys.size} surveys")
            hideLoadingDialog()

            if (isPaginatedView) {
                val selectBox = findViewById<android.widget.Spinner>(R.id.spinnerCardsPerPage)
                val pageSize = selectBox.selectedItem?.toString()?.toIntOrNull() ?: 5
                showPaginatedView(pageSize)
            } else {
                showFullView(surveys)
            }
        }
    }

    private fun showFullView(surveys: List<Survey>) {
        val surveyCardContainer: android.widget.LinearLayout = findViewById(R.id.surveyCardContainer)
        val rvSurveys: androidx.recyclerview.widget.RecyclerView = findViewById(R.id.rvSurveys)
        val footerControls = findViewById<footer_pages_view>(R.id.footerControls)
        surveyCardContainer.visibility = View.VISIBLE
        rvSurveys.visibility = View.GONE
        footerControls.visibility = View.GONE
        FullView().displaySurveysAsCards(this, surveyCardContainer, surveys)
    }

    private fun showPaginatedView(pageSize: Int) {
        val surveyCardContainer: android.widget.LinearLayout = findViewById(R.id.surveyCardContainer)
        val rvSurveys: androidx.recyclerview.widget.RecyclerView = findViewById(R.id.rvSurveys)
        val footerControls = findViewById<footer_pages_view>(R.id.footerControls)
        val selectBox = findViewById<android.widget.Spinner>(R.id.spinnerCardsPerPage)
        // Set continuous card index options for paginated view
        // val actualPageSize = cardsPerPagesView.getSelectedValue().trim().toIntOrNull()?.takeIf { it > 0 } ?: pageSize
        val actualPageSize = selectBox.selectedItem?.toString()?.toIntOrNull() ?: pageSize
        surveyCardContainer.visibility = View.VISIBLE
        rvSurveys.visibility = View.GONE
        footerControls.visibility = View.VISIBLE
        // Fetch paginated surveys from backend
        CoroutineScope(Dispatchers.Main).launch {
            val pageSurveys = withContext(Dispatchers.IO) {
                com.ecss.shb_andriod.view.PaginatedView().getPaginatedSurveys(currentPage, actualPageSize)
            }
            val startIndex = (currentPage - 1) * actualPageSize
            FullView().displaySurveysAsCards(this@SurveyActivity, surveyCardContainer, pageSurveys, startIndex)
            updatePageInfo(actualPageSize)
        }
    }

    private fun updatePageInfo(pageSize: Int) {
        val footerControls = findViewById<footer_pages_view>(R.id.footerControls)
        val tvPageInfo = footerControls.findViewById<android.widget.TextView>(R.id.tvPageInfo)
        val totalPages = ((surveys.size + pageSize - 1) / pageSize).coerceAtLeast(1)
        tvPageInfo.text = "Page $currentPage of $totalPages"
    }

    private fun showLoadingDialog() {
        loadingView?.show()
    }

    private fun hideLoadingDialog() {
        loadingView?.hide()
    }

}