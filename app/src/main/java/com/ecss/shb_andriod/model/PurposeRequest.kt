package com.ecss.shb_andriod.model

// Request body for POST /surveys
// This matches the backend expectation: req.body.purpose = "retrieve"
data class PurposeRequest(
    val purpose: String
)

