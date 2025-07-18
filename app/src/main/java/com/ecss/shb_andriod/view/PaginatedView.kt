package com.ecss.shb_andriod.view

import com.ecss.shb_andriod.model.Survey
import com.ecss.shb_andriod.api.SurveyApi
import com.ecss.shb_andriod.model.PurposeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PaginatedView {
    suspend fun getPaginatedSurveys(page: Int, pageSize: Int): List<Survey> {
        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://shb-backend.azurewebsites.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val surveyApi = retrofit.create(SurveyApi::class.java)
            withContext(Dispatchers.IO) {
                val request = PurposeRequest(purpose = "retrieveAndriod")
                val response = surveyApi.getSurveysRaw(request)
                val allSurveys = response.result?.surveys ?: emptyList()
                // Calculate start and end indices for the current page
                val fromIndex = ((page - 1) * pageSize).coerceAtLeast(0)
                val toIndex = (fromIndex + pageSize).coerceAtMost(allSurveys.size)
                // Return the correct sublist for the page (e.g., 1-5, 6-10, ...)
                if (fromIndex < toIndex) allSurveys.subList(fromIndex, toIndex) else emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}