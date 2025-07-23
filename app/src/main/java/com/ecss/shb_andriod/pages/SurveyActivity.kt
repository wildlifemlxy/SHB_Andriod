package com.ecss.shb_andriod.pages

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.ecss.shb_andriod.R
import com.ecss.shb_andriod.api.SurveyApi
import com.ecss.shb_andriod.model.PurposeRequest
import com.ecss.shb_andriod.model.Survey
import com.ecss.shb_andriod.view.CardsPerPagesView
import com.ecss.shb_andriod.view.FullView
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
    private var loadingDialog: android.app.AlertDialog? = null

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
        setContentView(R.layout.activity_observation)
        val cardsPerPagesView = findViewById<CardsPerPagesView>(R.id.cardsPerPagesView)
        val footerControls = findViewById<footer_pages_view>(R.id.footerControls)
        footerControls.visibility = View.GONE // Hide footer by default
        cardsPerPagesView.visibility = View.GONE // Hide cards per page view by default
        cardsPerPagesView.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && isPaginatedView) {
                val pageSize = cardsPerPagesView.getSelectedValue().trim().toIntOrNull()?.takeIf { it > 0 } ?: 5
                showPaginatedView(pageSize)
            }
        }
        cardsPerPagesView.setOnEditorActionListener { v: android.widget.TextView, actionId: Int, event: android.view.KeyEvent? ->
            if (isPaginatedView) {
                val pageSize = cardsPerPagesView.getSelectedValue().trim().toIntOrNull()?.takeIf { it > 0 } ?: 5
                showPaginatedView(pageSize)
            }
            false
        }
        // Listen for changes in the cards per page input and update immediately
        cardsPerPagesView.setOnCardsPerPageChangedListener { value ->
            if (isPaginatedView) {
                val pageSize = value.trim().toIntOrNull()?.takeIf { it > 0 } ?: 5
                showPaginatedView(pageSize)
            }
        }
        getAllSurveys()

        footerControls.btnPrevPage.setOnClickListener {
            val pageSize = cardsPerPagesView.getSelectedValue().trim().toIntOrNull()?.takeIf { it > 0 } ?: 5
            ((surveys.size + pageSize - 1) / pageSize).coerceAtLeast(1)
            if (currentPage > 1) {
                currentPage--
                showPaginatedView(pageSize)
                updatePageInfo(pageSize)
            }
        }
        footerControls.btnNextPage.setOnClickListener {
            val pageSize = cardsPerPagesView.getSelectedValue().trim().toIntOrNull()?.takeIf { it > 0 } ?: 5
            val totalPages = ((surveys.size + pageSize - 1) / pageSize).coerceAtLeast(1)
            if (currentPage < totalPages) {
                currentPage++
                showPaginatedView(pageSize)
                updatePageInfo(pageSize)
            }
        }
        footerControls.btnFirstPage.setOnClickListener {
            val pageSize = cardsPerPagesView.getSelectedValue().trim().toIntOrNull()?.takeIf { it > 0 } ?: 5
            currentPage = 1
            showPaginatedView(pageSize)
            updatePageInfo(pageSize)
        }
        footerControls.btnLastPage.setOnClickListener {
            val pageSize = cardsPerPagesView.getSelectedValue().trim().toIntOrNull()?.takeIf { it > 0 } ?: 5
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
            loadingDialog?.dismiss()
            hideLoadingDialog()

            if (isPaginatedView) {
                val cardsPerPagesView = findViewById<CardsPerPagesView>(R.id.cardsPerPagesView)
                val pageSize = cardsPerPagesView.getSelectedValue().toIntOrNull()?.takeIf { it > 0 } ?: 5
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
        val cardsPerPagesView = findViewById<CardsPerPagesView>(R.id.cardsPerPagesView)
        surveyCardContainer.visibility = View.VISIBLE
        rvSurveys.visibility = View.GONE
        footerControls.visibility = View.GONE
        cardsPerPagesView.visibility = View.GONE
        FullView().displaySurveysAsCards(this, surveyCardContainer, surveys)
    }

    private fun showPaginatedView(pageSize: Int) {
        val surveyCardContainer: android.widget.LinearLayout = findViewById(R.id.surveyCardContainer)
        val rvSurveys: androidx.recyclerview.widget.RecyclerView = findViewById(R.id.rvSurveys)
        val footerControls = findViewById<footer_pages_view>(R.id.footerControls)
        val cardsPerPagesView = findViewById<CardsPerPagesView>(R.id.cardsPerPagesView)
        // Set continuous card index options for paginated view
        cardsPerPagesView.setCardIndexContinuous(1, surveys.size)
        val actualPageSize = cardsPerPagesView.getSelectedValue().trim().toIntOrNull()?.takeIf { it > 0 } ?: pageSize
        surveyCardContainer.visibility = View.VISIBLE
        rvSurveys.visibility = View.GONE
        footerControls.visibility = View.VISIBLE
        cardsPerPagesView.visibility = View.VISIBLE
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
        val rootView = window.decorView.rootView
        rootView.alpha = 0.5f // Blur effect (simple dimming)
        val popupView = layoutInflater.inflate(R.layout.popup, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(popupView)
            .setCancelable(false)
            .create()
        dialog.setOnDismissListener { rootView.alpha = 1f }
        dialog.show()
        loadingDialog = dialog
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        window.decorView.rootView.alpha = 1f
    }

}
