package com.ecss.shb_andriod.pages

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import com.ecss.shb_andriod.R
import com.ecss.shb_andriod.base.BaseActivity

class MainPage : BaseActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        // Show the splash screen on Android 12+
        super.onCreate(savedInstanceState)
        // Hide system UI for fullscreen (top and bottom nav bars)
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
        // Use the shared setup for drawer and toolbar
        setupDrawerAndToolbar(R.layout.activity_main_page)
    }
}
