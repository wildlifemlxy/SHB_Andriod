package com.ecss.shb_andriod.view

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.ecss.shb_andriod.R
import com.ecss.shb_andriod.model.Survey
import com.ecss.shb_andriod.model.SurveyClusterItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager


class MapViewContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), OnMapReadyCallback {
    private val mapView: MapView
    private var googleMap: GoogleMap? = null
    private var clusterManager: ClusterManager<SurveyClusterItem>? = null
    private var markerPopupView: View? = null
    private val markerItemMap = mutableMapOf<LatLng, SurveyClusterItem>()
    private var surveys: List<Survey> = emptyList()

    init {
        LayoutInflater.from(context).inflate(R.layout.view_map, this, true)
        mapView = findViewById(R.id.mapView)
    }

    fun onCreate(savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        // getAllSurveys() removed, as it does not exist in this class
    }

    fun onResume() = mapView.onResume()
    fun onPause() = mapView.onPause()
    fun onDestroy() = mapView.onDestroy()
    fun onLowMemory() = mapView.onLowMemory()
     fun onSaveInstanceState(outState: Bundle) = mapView.onSaveInstanceState(outState)

    // Add a callback interface for zoom updates
    interface OnZoomLevelChangeListener {
        fun onZoomLevelChanged(zoom: Float)
    }
    private var zoomLevelChangeListener: OnZoomLevelChangeListener? = null
    fun setOnZoomLevelChangeListener(listener: OnZoomLevelChangeListener) {
        zoomLevelChangeListener = listener
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isCompassEnabled = false
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: com.google.android.gms.maps.model.Marker): View? = null
            override fun getInfoContents(marker: com.google.android.gms.maps.model.Marker): View? {
                val item = markerItemMap[marker.position]
                if (item != null) {
                    val view = LayoutInflater.from(context).inflate(R.layout.custom_marker_view, null)
                    val tvLabel = view.findViewById<TextView>(R.id.tvLabel)
                    val tvValue = view.findViewById<TextView>(R.id.tvValue)
                    tvLabel.text = item.getSnippet()
                    tvValue.text = ""
                    return view
                }
                return null
            }
        })

        val northwest = LatLng(1.470556, 103.6369914)
        val northeast = LatLng(1.470556, 104.4061449)
        val southwest = LatLng(1.1594868, 103.6369914)
        val southeast = LatLng(1.1594868, 104.4061449)
        val singaporeBounds = com.google.android.gms.maps.model.LatLngBounds(southwest, northeast)
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val padding = (height * 0.08).toInt()
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                singaporeBounds,
                width,
                height,
                padding
            )
        )
        googleMap.setOnMapLoadedCallback {
            val currentZoom = googleMap.cameraPosition.zoom
            val centerLat = (northwest.latitude + southeast.latitude) / 2
            val centerLng = (northwest.longitude + southeast.longitude) / 2
            val westLng = northwest.longitude
            val shiftedLng = centerLng + (westLng - centerLng) * 0.5
            val shiftedCenter = LatLng(centerLat, shiftedLng)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(shiftedCenter, currentZoom))
        }
        googleMap.setLatLngBoundsForCameraTarget(singaporeBounds)
        googleMap.setMinZoomPreference(10f)
        googleMap.setPadding(0, 0, 0, -80)
        Log.d("MapViewContainer", "Google Map is ready")

        clusterManager = ClusterManager(context, googleMap)
        val algorithm = com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm<SurveyClusterItem>()
        algorithm.setMaxDistanceBetweenClusteredItems(120)
        clusterManager?.algorithm = algorithm
        // Set custom renderer for colored markers, keep default clusters
        clusterManager?.renderer = object : com.google.maps.android.clustering.view.DefaultClusterRenderer<SurveyClusterItem>(context, googleMap, clusterManager) {
            override fun onBeforeClusterItemRendered(item: SurveyClusterItem, markerOptions: com.google.android.gms.maps.model.MarkerOptions) {
                // Set marker color based on seenHeard
                val color = when (item.seenHeard?.lowercase()) {
                    "seen" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) // blue
                    "heard" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN) // green
                    "not found" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED) // red
                    else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                }
                markerOptions.icon(color)
            }
        }
        googleMap.setOnCameraIdleListener(clusterManager)
        renderSurveyMarkers()
        clusterManager?.cluster()

        // --- Handle marker and cluster item clicks ---
        clusterManager?.setOnClusterItemClickListener { item ->
            Log.d("MapViewContainer", "Cluster item clicked: $item")
            // Update More Information section in the parent activity
            val activity = context as? android.app.Activity
            activity?.runOnUiThread {
                activity.findViewById<TextView>(R.id.tvMoreInfoContent)?.text = buildString {
                    append(item.location).append("\n")
                    append("Observer: ").append(item.observerName)
                    append("Date: ").append(item.date).append("\n")
                    append("Time: ").append(item.time).append("\n")
                    append("Seen/Heard: ").append(item.seenHeard).append("\n")
                    append("Activity: ").append(
                        listOfNotNull(item.activity)
                            .filter { it.isNotBlank() }
                            .joinToString(", ")
                            .ifEmpty { "-" }
                    ).append("\n")
                }
            }
            true
        }
        clusterManager?.setOnClusterClickListener { cluster ->
            // Optionally, zoom in on cluster click
            val position = cluster.position
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, googleMap.cameraPosition.zoom + 2))
            true
        }

        googleMap.setOnCameraIdleListener {
            clusterManager?.onCameraIdle()
            val zoom = googleMap.cameraPosition.zoom
            (clusterManager?.renderer as? CustomClusterRenderer)?.setCurrentZoom(zoom)
            clusterManager?.cluster()
            // Notify zoom level change
            zoomLevelChangeListener?.onZoomLevelChanged(zoom)
        }

        googleMap.setOnCameraMoveListener {
            val zoom = googleMap.cameraPosition.zoom
            zoomLevelChangeListener?.onZoomLevelChanged(zoom)
        }
    }

    private fun showCustomMarkerPopup(item: SurveyClusterItem) {
        // Update the More Information section with detailed info
        val activity = context as? android.app.Activity
        activity?.findViewById<TextView>(R.id.tvMoreInfoContent)?.text = buildString {
            append("Location: ").append(item.location ?: "-").append("\n")
            append("Date: ").append(item.date ?: "-").append("\n")
            append("Time: ").append(item.time ?: "-").append("\n")
            append("Seen/Heard: ").append(item.seenHeard ?: "-").append("\n")
            append("Activity: ").append(item.activity ?: "-").append("\n")
            append("Observer: ").append(item.observerName ?: "-")
        }
        val rootView = rootView as? View
        markerPopupView?.let { (rootView as? android.view.ViewGroup)?.removeView(it) }
        val inflater = LayoutInflater.from(context)
        val popup = inflater.inflate(R.layout.custom_marker_view, null)
        val tvLabel = popup.findViewById<TextView>(R.id.tvLabel)
        val tvValue = popup.findViewById<TextView>(R.id.tvValue)
        val sb = StringBuilder()
        sb.append("Location: ").append(item.location ?: "-").append("\n")
        sb.append("-------------------\n")
        sb.append("Seen/Heard: ").append(item.seenHeard ?: "-")
        tvLabel.text = sb.toString()
        tvValue.text = ""
        (rootView as? android.view.ViewGroup)?.addView(popup)
        markerPopupView = popup
        popup.setOnClickListener {
            (rootView as? android.view.ViewGroup)?.removeView(popup)
            markerPopupView = null
        }
    }

    private fun renderSurveyMarkers() {
        Log.d("MapViewContainer", "renderSurveyMarkers")
        clusterManager?.clearItems()
        markerItemMap.clear()
        val validSurveys = surveys.filter { it.lat != null && it.long != null }
        Log.d("MapViewContainer", "renderSurveyMarkers: Adding ${validSurveys.size} items to clusterManager")
        val grouped = validSurveys.groupBy { Pair(it.lat, it.long) }
        grouped.forEach { (pos, entries) ->
            val radius = 0.0002
            val angleStep = 360.0 / entries.size
            entries.forEachIndexed { idx, survey ->
                val offsetLatLng = if (entries.size > 1) {
                    val angle = Math.toRadians(angleStep * idx)
                    LatLng((pos.first ?: 0.0) + radius * Math.cos(angle), (pos.second ?: 0.0) + radius * Math.sin(angle))
                } else {
                    LatLng(survey.lat!!, survey.long!!)
                }
                val item = SurveyClusterItem(
                    offsetLatLng.latitude,
                    offsetLatLng.longitude,
                    survey.seenHeard,
                    survey.location,
                    survey.date,
                    survey.time,
                    survey.activity,
                    survey.observerName
                )
                markerItemMap[offsetLatLng] = item
                clusterManager?.addItem(item)
            }
        }
        clusterManager?.cluster()
    }

    // Add a method to allow MapActivity to set surveys and trigger marker rendering
    fun setSurveys(surveys: List<Survey>) {
        this.surveys = surveys
        renderSurveyMarkers()
    }
}
