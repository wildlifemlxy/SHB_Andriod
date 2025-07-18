package com.ecss.shb_andriod.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout

class CardsPerPagesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val options = arrayOf("5", "10", "15", "20")
    private val autoCompleteTextView: AutoCompleteTextView

    init {
        orientation = VERTICAL // Ensure vertical stacking
        autoCompleteTextView = AutoCompleteTextView(context)
        autoCompleteTextView.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, options))
        autoCompleteTextView.threshold = 0 // show dropdown immediately
        autoCompleteTextView.inputType = android.text.InputType.TYPE_CLASS_NUMBER // Make it a number textbox
        autoCompleteTextView.setOnClickListener { autoCompleteTextView.showDropDown() }
        addView(autoCompleteTextView)
    }

    fun getSelectedValue(): String {
        return autoCompleteTextView.text.toString()
    }

    fun setSelectedValue(value: String) {
        autoCompleteTextView.setText(value, false)
    }

    fun setOnEditorActionListener(listener: (v: android.widget.TextView, actionId: Int, event: android.view.KeyEvent?) -> Boolean) {
        autoCompleteTextView.setOnEditorActionListener(listener)
    }

    fun setOnCardsPerPageChangedListener(listener: (String) -> Unit) {
        autoCompleteTextView.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                listener(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    fun setCardIndexContinuous(startIndex: Int, totalCards: Int) {
        val indices = (startIndex..totalCards).map { it.toString() }
        autoCompleteTextView.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, indices))
        // Only clear if the current value is not in the new range
        val currentValue = autoCompleteTextView.text.toString()
        if (currentValue.isNotEmpty() && (currentValue.toIntOrNull() ?: 0) !in startIndex..totalCards) {
            autoCompleteTextView.setText(indices.firstOrNull() ?: "", false)
        }
    }
}