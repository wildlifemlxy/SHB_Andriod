package com.ecss.shb_andriod.base

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.ecss.shb_andriod.R
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Show the splash screen on Android 12+
        super.onCreate(savedInstanceState)
        // Hide system UI for fullscreen (top and bottom nav bars)
        // No need to set delegate.localNightMode; use your own colors in colors.xml and values-night/colors.xml
    }

    protected fun setupDrawerAndToolbar(layoutResId: Int) {
        setContentView(layoutResId)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Setup custom RecyclerView drawer
        val drawerRecyclerView = findViewById<RecyclerView>(R.id.drawerRecyclerView)
        drawerRecyclerView.layoutManager = LinearLayoutManager(this)

        // Sample drawer items (matching your original menu)
        val drawerItems = mutableListOf<DrawerItem>(
            DrawerItem.Group(
                title = "Surveys",
                iconRes = android.R.drawable.ic_menu_agenda,
                children = listOf(
                    DrawerItem.Child("Data Visualization", android.R.drawable.ic_menu_view, id = 1),
                    DrawerItem.Child("Survey", android.R.drawable.ic_menu_edit, id = 2),
                    DrawerItem.Child("Map", android.R.drawable.ic_menu_mapmode, id = 3)
                )
            ),
            DrawerItem.Simple("Events", android.R.drawable.ic_menu_my_calendar, id = 4),
            DrawerItem.Simple("Settings", android.R.drawable.ic_menu_preferences, id = 5)
        )

        val adapter = DrawerAdapter(drawerItems) { item ->
            // Handle item clicks here
            when (item) {
                is DrawerItem.Child -> {
                    when (item.id) {
                        1 -> startActivity(android.content.Intent(this, com.ecss.shb_andriod.pages.DataVisualizationActivity::class.java))
                        2 -> startActivity(android.content.Intent(this, com.ecss.shb_andriod.pages.SurveyActivity::class.java))
                        3 -> startActivity(android.content.Intent(this, com.ecss.shb_andriod.pages.MapActivity::class.java))
                        else -> android.widget.Toast.makeText(this, "Clicked: ${item.title}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                is DrawerItem.Simple -> android.widget.Toast.makeText(this, "Clicked: ${item.title}", android.widget.Toast.LENGTH_SHORT).show()
                else -> {}
            }
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
        }
        drawerRecyclerView.adapter = adapter

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: android.view.View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: android.view.View) {}
            override fun onDrawerClosed(drawerView: android.view.View) {
                adapter.collapseAllGroupsAndNotify()
            }
            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
