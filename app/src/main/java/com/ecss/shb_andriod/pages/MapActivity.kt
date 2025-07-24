package com.ecss.shb_andriod.pages

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ecss.shb_andriod.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Set the Google Map to hybrid mode
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        // Singapore extreme boundary points (northwest, northeast, southwest, southeast)
        val northwest = LatLng(1.470556, 103.6369914) // Sembawang (N) & Tuas Link MRT (W)
        val northeast = LatLng(1.470556, 104.4061449) // Sembawang (N) & Pedra Branca (E)
        val southwest = LatLng(1.1594868, 103.6369914) // Pulau Satumu (S) & Tuas Link MRT (W)
        val southeast = LatLng(1.1594868, 104.4061449) // Pulau Satumu (S) & Pedra Branca (E)
        // Create LatLngBounds using southwest and northeast
        val singaporeBounds = com.google.android.gms.maps.model.LatLngBounds(southwest, northeast)
        // Move camera to fit Singapore's true extreme bounds so west is at left and east is at right
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val padding = (height * 0.08).toInt() // Add only vertical padding, no horizontal padding
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                singaporeBounds,
                width,
                height,
                padding
            )
        )
        // After the bounds fit, animate a zoom in (0.5f) and shift the center slightly west for a left bias
        googleMap.setOnMapLoadedCallback {
            val currentZoom = googleMap.cameraPosition.zoom
            // Shift center longitude slightly west (move map left)
            val centerLat = (northwest.latitude + southeast.latitude) / 2
            val centerLng = ((northwest.longitude + southeast.longitude) / 2) - 0.08 // tweak this value for more/less shift
            val singaporeCenter = LatLng(centerLat, centerLng)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(singaporeCenter, currentZoom + 0.5f))
        }
    }
}
