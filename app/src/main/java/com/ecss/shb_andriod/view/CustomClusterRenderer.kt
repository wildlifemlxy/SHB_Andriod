package com.ecss.shb_andriod.view

import android.content.Context
import android.graphics.Color
import com.ecss.shb_andriod.model.SurveyClusterItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class CustomClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<SurveyClusterItem>
) : DefaultClusterRenderer<SurveyClusterItem>(context, map, clusterManager) {
    private var currentZoom: Float = 0f

    fun setCurrentZoom(zoom: Float) {
        currentZoom = zoom
    }

    override fun getClusterText(bucket: Int): String {
        // Update to show 'N+' for clusters with 20 or more items
        return if (bucket >= 20) {
            "$bucket+"
        } else {
            "$bucket+"
        }
    }


    override fun onBeforeClusterItemRendered(
        item: SurveyClusterItem,
        markerOptions: MarkerOptions
    ) {
        when (item.seenHeard?.lowercase()) {
            "seen" -> {
                val color = android.graphics.Color.parseColor("#1976D2") // blue
                val hue = getHueFromColor(color)
                markerOptions.icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(hue))
                markerOptions.alpha(1.0f)
                markerOptions.title(item.seenHeard)
            }
            "heard" -> {
                val color = android.graphics.Color.parseColor("#388E3C") // green
                val hue = getHueFromColor(color)
                markerOptions.icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(hue))
                markerOptions.alpha(1.0f)
                markerOptions.title(item.seenHeard)
            }
            "not found" -> {
                val color = android.graphics.Color.parseColor("#D32F2F") // red
                val hue = getHueFromColor(color)
                markerOptions.icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(hue))
                markerOptions.alpha(1.0f)
                markerOptions.title(item.seenHeard)
            }
            else -> {
                markerOptions.visible(false)
            }
        }
    }

    private fun getHueFromColor(color: Int): Float {
        val r = Color.red(color) / 255.0f
        val g = Color.green(color) / 255.0f
        val b = Color.blue(color) / 255.0f
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        var hue = 0f
        when (max) {
            r -> hue = ((g - b) / delta) % 6
            g -> hue = ((b - r) / delta) + 2
            b -> hue = ((r - g) / delta) + 4
        }
        hue *= 60f
        if (hue < 0) hue += 360f
        return hue
    }

    override fun shouldRenderAsCluster(cluster: Cluster<SurveyClusterItem>): Boolean {
        // Show individual markers when zoomed in
        return currentZoom < 18f && cluster.size > 1
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<SurveyClusterItem>,
        markerOptions: MarkerOptions
    ) {
        markerOptions.alpha(1.0f)
    }
}