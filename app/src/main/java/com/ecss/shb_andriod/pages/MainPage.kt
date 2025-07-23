package com.ecss.shb_andriod.pages

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ecss.shb_andriod.R

class MainPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        val btnSettings = findViewById<Button>(R.id.btnSettings)
        val btnSurveys = findViewById<Button>(R.id.btnSurveys)
        val btnEvents = findViewById<Button>(R.id.btnEvents)

        btnSurveys.setOnClickListener {
            val intent = android.content.Intent(this, ObservationActivity::class.java)
            startActivity(intent)
        }

        btnEvents.setOnClickListener {
            // TODO: Navigate to Events page
            Toast.makeText(this, "Events clicked", Toast.LENGTH_SHORT).show()
        }
        btnSettings.setOnClickListener {
            // TODO: Navigate to Settings page
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
