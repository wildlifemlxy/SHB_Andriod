package com.ecss.shb_andriod.view

import android.util.Log
import com.ecss.shb_andriod.model.Survey
import com.ecss.shb_andriod.api.SurveyApi
import com.ecss.shb_andriod.model.PurposeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FullView {

    /**
     * Inflates item_survey.xml for each Survey and adds it to the parent LinearLayout.
     * @param context The context to use for inflating views.
     * @param parent The LinearLayout to add survey cards to.
     * @param surveys The list of Survey objects to display.
If      * @param startIndex The index to start numbering observations from.
     */
    fun displaySurveysAsCards(context: android.content.Context, parent: android.widget.LinearLayout, surveys: List<Survey>, startIndex: Int = 0) {
        parent.removeAllViews()
        val adapter = com.ecss.shb_andriod.adapter.SurveyAdapter(surveys, startIndex)
        val inflater = android.view.LayoutInflater.from(context)
        parent.removeAllViews()
        var expandedCardIndex: Int? = null
        val cardViews = mutableListOf<android.view.View>()
        surveys.forEachIndexed { idx, survey ->
            val cardView = inflater.inflate(com.ecss.shb_andriod.R.layout.item_survey, parent, false)
            // Set values for each field
            cardView.findViewById<android.widget.TextView>(com.ecss.shb_andriod.R.id.tvIndex)?.text = "Observation #${startIndex + idx + 1}"
            cardView.findViewById<android.widget.TextView>(com.ecss.shb_andriod.R.id.tvObserverName)?.text = survey.observerName ?: "-"
            cardView.findViewById<android.widget.TextView>(com.ecss.shb_andriod.R.id.tvShbIndividualId)?.text = survey.shbIndividualId ?: "-"
            cardView.findViewById<android.widget.TextView>(com.ecss.shb_andriod.R.id.tvNumberOfBirds)?.text = survey.numberOfBirds?.toString() ?: "-"
            cardView.findViewById<android.widget.TextView>(com.ecss.shb_andriod.R.id.tvLocation)?.text = survey.location ?: "-"
            cardView.findViewById<android.widget.TextView>(com.ecss.shb_andriod.R.id.tvDate)?.text = survey.date ?: "-"
            cardView.findViewById<android.widget.TextView>(com.ecss.shb_andriod.R.id.tvTime)?.text = survey.time ?: "-"
            cardView.findViewById<android.widget.TextView>(com.ecss.shb_andriod.R.id.tvHeightOfTree)?.text = survey.heightOfTree?.toString() ?: "-"
            cardView.findViewById<android.widget.TextView>(com.ecss.shb_andriod.R.id.tvHeightOfBird)?.text = survey.heightOfBird?.toString() ?: "-"
            cardView.findViewById<android.widget.TextView>(com.ecss.shb_andriod.R.id.tvActivityType)?.text = survey.activityType ?: "-"
            cardView.findViewById<android.widget.TextView>(com.ecss.shb_andriod.R.id.tvSeenHeard)?.text = survey.seenHeard ?: "-"
            cardView.findViewById<android.widget.TextView>(com.ecss.shb_andriod.R.id.tvActivityDetails)?.text = survey.activityDetails ?: "-"
            cardView.findViewById<android.widget.TextView>(com.ecss.shb_andriod.R.id.tvActivity)?.text = survey.activity ?: "-"
            // Collapse details by default
            val detailsContainer = cardView.findViewById<android.view.View>(com.ecss.shb_andriod.R.id.detailsContainer)
            detailsContainer.visibility = android.view.View.GONE
            val expandIcon = cardView.findViewById<android.widget.ImageView>(com.ecss.shb_andriod.R.id.ivExpand)
            expandIcon.setImageResource(android.R.drawable.arrow_down_float)
                cardView.setOnClickListener {
                // Collapse all other cards
                cardViews.forEachIndexed { i, v ->
                    val dc = v.findViewById<android.view.View>(com.ecss.shb_andriod.R.id.detailsContainer)
                    val ei = v.findViewById<android.widget.ImageView>(com.ecss.shb_andriod.R.id.ivExpand)
                    if (i == idx) {
                        val isCollapsed = dc.visibility == android.view.View.GONE
                        dc.visibility = if (isCollapsed) android.view.View.VISIBLE else android.view.View.GONE
                        ei.setImageResource(if (isCollapsed) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
                    } else {
                        dc.visibility = android.view.View.GONE
                        ei.setImageResource(android.R.drawable.arrow_down_float)
                    }
                }
            }
            cardViews.add(cardView)
        }
        cardViews.forEach { parent.addView(it) }
        // Make parent scrollable
        if (parent.parent is android.widget.ScrollView) {
            (parent.parent as android.widget.ScrollView).isFillViewport = true
        }
    }
}