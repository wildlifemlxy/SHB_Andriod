package com.ecss.shb_andriod.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.ecss.shb_andriod.R

class footer_pages_view @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    val btnFirstPage: Button
    val btnPrevPage: Button
    val btnNextPage: Button
    val btnLastPage: Button

    var onFirstPageClick: (() -> Unit)? = null
    var onPrevPageClick: (() -> Unit)? = null
    var onNextPageClick: (() -> Unit)? = null
    var onLastPageClick: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_footer, this, true)
        btnFirstPage = findViewById(R.id.btnFirstPage)
        btnPrevPage = findViewById(R.id.btnPrevPage)
        btnNextPage = findViewById(R.id.btnNextPage)
        btnLastPage = findViewById(R.id.btnLastPage)

        btnFirstPage.setOnClickListener { onFirstPageClick?.invoke() }
        btnPrevPage.setOnClickListener { onPrevPageClick?.invoke() }
        btnNextPage.setOnClickListener { onNextPageClick?.invoke() }
        btnLastPage.setOnClickListener { onLastPageClick?.invoke() }
    }
}