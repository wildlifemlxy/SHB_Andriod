package com.ecss.shb_andriod.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecss.shb_andriod.R
import com.ecss.shb_andriod.model.Survey

// Adapter to display survey data in a table-like RecyclerView
class SurveyAdapter(private val surveys: List<Survey>, private val startIndex: Int) : RecyclerView.Adapter<SurveyAdapter.SurveyViewHolder>() {
    private var expandedPosition: Int = -1

    class SurveyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIndex: TextView = itemView.findViewById(R.id.tvIndex)
        val ivExpand: android.widget.ImageView = itemView.findViewById(R.id.ivExpand)
        val detailsContainer: View = itemView.findViewById(R.id.detailsContainer)
        val tvObserverNameLabel: TextView = itemView.findViewById(R.id.tvObserverNameLabel)
        val tvObserverName: TextView = itemView.findViewById(R.id.tvObserverName)
        val tvShbIndividualIdLabel: TextView = itemView.findViewById(R.id.tvShbIndividualIdLabel)
        val tvShbIndividualId: TextView = itemView.findViewById(R.id.tvShbIndividualId)
        val tvNumberOfBirdsLabel: TextView = itemView.findViewById(R.id.tvNumberOfBirdsLabel)
        val tvNumberOfBirds: TextView = itemView.findViewById(R.id.tvNumberOfBirds)
        val tvLocationLabel: TextView = itemView.findViewById(R.id.tvLocationLabel)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvDateLabel: TextView = itemView.findViewById(R.id.tvDateLabel)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTimeLabel: TextView = itemView.findViewById(R.id.tvTimeLabel)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvHeightOfTreeLabel: TextView = itemView.findViewById(R.id.tvHeightOfTreeLabel)
        val tvHeightOfTree: TextView = itemView.findViewById(R.id.tvHeightOfTree)
        val tvHeightOfBirdLabel: TextView = itemView.findViewById(R.id.tvHeightOfBirdLabel)
        val tvHeightOfBird: TextView = itemView.findViewById(R.id.tvHeightOfBird)
        val tvActivityTypeLabel: TextView = itemView.findViewById(R.id.tvActivityTypeLabel)
        val tvActivityType: TextView = itemView.findViewById(R.id.tvActivityType)
        val tvSeenHeardLabel: TextView = itemView.findViewById(R.id.tvSeenHeardLabel)
        val tvSeenHeard: TextView = itemView.findViewById(R.id.tvSeenHeard)
        val tvActivityDetailsLabel: TextView = itemView.findViewById(R.id.tvActivityDetailsLabel)
        val tvActivityDetails: TextView = itemView.findViewById(R.id.tvActivityDetails)
        val tvActivityLabel: TextView = itemView.findViewById(R.id.tvActivityLabel)
        val tvActivity: TextView = itemView.findViewById(R.id.tvActivity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_survey, parent, false)
        return SurveyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurveyViewHolder, position: Int) {
        val survey = surveys[position]
        holder.tvIndex.text = "Observation ${startIndex + position + 1}"
        holder.tvObserverName.text = survey.observerName ?: "-"
        holder.tvShbIndividualId.text = survey.shbIndividualId ?: "-"
        holder.tvNumberOfBirds.text = survey.numberOfBirds ?: "-"
        holder.tvLocation.text = survey.location ?: "-"
        holder.tvDate.text = survey.date ?: "-"
        holder.tvTime.text = survey.time ?: "-"
        holder.tvHeightOfTree.text = survey.heightOfTree ?: "-"
        holder.tvHeightOfBird.text = survey.heightOfBird ?: "-"
        holder.tvActivityType.text = survey.activityType ?: "-"
        holder.tvSeenHeard.text = survey.seenHeard ?: "-"
        holder.tvActivityDetails.text = survey.activityDetails ?: "-"
        holder.tvActivity.text = survey.activity ?: "-"

        val isExpanded = position == expandedPosition
        holder.detailsContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.ivExpand.setImageResource(if (isExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)

        holder.itemView.setOnClickListener {
            val previousExpandedPosition = expandedPosition
            expandedPosition = if (isExpanded) -1 else position
            notifyItemChanged(previousExpandedPosition)
            notifyItemChanged(position)
        }
        holder.ivExpand.setOnClickListener {
            val previousExpandedPosition = expandedPosition
            expandedPosition = if (isExpanded) -1 else position
            notifyItemChanged(previousExpandedPosition)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = surveys.size
}
