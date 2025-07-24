package com.ecss.shb_andriod.view

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.ecss.shb_andriod.R

class LoadingView(private val context: Context) {
    private var dialog: Dialog? = null

    fun show() {
        if (dialog == null) {
            dialog = Dialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.popup, null)
            // Set up spinner and text programmatically if not present in XML
            val linearLayout = view.findViewById<android.widget.LinearLayout>(R.id.popup_content_root)
            if (linearLayout != null) {
                // Add ProgressBar (spinner)
                val progressBar = android.widget.ProgressBar(context)
                val pbParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                pbParams.gravity = android.view.Gravity.CENTER
                progressBar.layoutParams = pbParams
                linearLayout.addView(progressBar)

                // Add TextView for loading text
                val textView = android.widget.TextView(context)
                textView.text = "Now Loading...."
                textView.textSize = 18f
                textView.setPadding(0, 24, 0, 0)
                textView.gravity = android.view.Gravity.CENTER
                linearLayout.addView(textView)
            }
            dialog?.setContentView(view)
            dialog?.setCancelable(false)
        }
        dialog?.show()
    }

    fun hide() {
        dialog?.dismiss()
    }
}