package com.ecss.shb_andriod.pages

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ecss.shb_andriod.R

class ObservationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create layout programmatically
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 120, 48, 48)
        }
        val btnDataVisualization = android.widget.Button(this).apply {
            text = "Data Visualization"
            setOnClickListener {
                val intent = android.content.Intent(this@ObservationActivity, DataVisualizationActivity::class.java)
                startActivity(intent)
            }
        }
        val btnSurvey = android.widget.Button(this).apply {
            text = "Survey"
            setOnClickListener {
                val intent = android.content.Intent(this@ObservationActivity, SurveyActivity::class.java)
                startActivity(intent)
            }
        }
        val btnMap = android.widget.Button(this).apply {
            text = "Map"
            setOnClickListener {
                val intent = android.content.Intent(this@ObservationActivity, MapActivity::class.java)
                startActivity(intent)
            }
        }
        layout.addView(btnDataVisualization)
        layout.addView(btnSurvey)
        layout.addView(btnMap)
        setContentView(layout)
    }
}