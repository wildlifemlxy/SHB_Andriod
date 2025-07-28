package com.ecss.shb_andriod.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class SurveyClusterItem(
    val lat: Double?,
    val lng: Double?,
    val seenHeard: String?,
    val location: String?,
    val date: String?, // Add time and date field
    val time: String?,// Add time and date field
    val activity: String?,
    val observerName: String?
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(lat ?: 0.0, lng ?: 0.0)
    override fun getZIndex(): Float = 0f
    override fun getTitle(): String? = location
    override fun getSnippet(): String? {
        val sb = StringBuilder()
        if (!date.isNullOrBlank()) sb.append("Date & Time: ").append(date +" "+ time).append("\n")
        return sb.toString().trimEnd()
    }
}
