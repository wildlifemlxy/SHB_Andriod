package com.ecss.shb_andriod.model

data class Survey(
    @com.google.gson.annotations.SerializedName("Observer name")
    val observerName: String?,
    @com.google.gson.annotations.SerializedName("SHB individual ID")
    val shbIndividualId: String?,
    @com.google.gson.annotations.SerializedName("Number of Birds")
    val numberOfBirds: String?,
    @com.google.gson.annotations.SerializedName("Location")
    val location: String?,
    @com.google.gson.annotations.SerializedName("Date")
    val date: String?,
    @com.google.gson.annotations.SerializedName("Time")
    val time: String?,
    @com.google.gson.annotations.SerializedName("Height of tree/m")
    val heightOfTree: String?,
    @com.google.gson.annotations.SerializedName("Height of bird/m")
    val heightOfBird: String?,
    @com.google.gson.annotations.SerializedName("Activity (foraging, preening, calling, perching, others)")
    val activityType: String?,
    @com.google.gson.annotations.SerializedName("Seen/Heard")
    val seenHeard: String?,
    @com.google.gson.annotations.SerializedName("Activity Details")
    val activityDetails: String?,
    @com.google.gson.annotations.SerializedName("Activity")
    val activity: String?
)

// Wrapper for the backend response
// The backend returns: { result: { success: true, surveys: [...] } }
data class ResponseBody(
    val success: Boolean? = null,
    val surveys: List<Survey>? = null
)
