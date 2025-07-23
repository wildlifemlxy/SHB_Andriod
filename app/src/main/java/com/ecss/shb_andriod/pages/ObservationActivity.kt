package com.ecss.shb_andriod.pages

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ecss.shb_andriod.pages.DataVisualizationActivity

class ObservationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, DataVisualizationActivity::class.java)
        startActivity(intent)
        finish()
    }
}