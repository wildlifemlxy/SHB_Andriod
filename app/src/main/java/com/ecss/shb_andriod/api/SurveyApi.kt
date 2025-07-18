package com.ecss.shb_andriod.api

import com.ecss.shb_andriod.model.PurposeRequest
import com.ecss.shb_andriod.model.ResponseBody
import com.ecss.shb_andriod.model.SurveyResultWrapper
import retrofit2.http.Body
import retrofit2.http.POST

// Retrofit interface for the surveys API
interface SurveyApi {
    // POST request to fetch raw survey response
    @POST("surveys")
    suspend fun getSurveysRaw(@Body request: PurposeRequest): SurveyResultWrapper
}
