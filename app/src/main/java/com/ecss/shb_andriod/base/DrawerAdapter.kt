package com.ecss.shb_andriod.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecss.shb_andriod.R

class DrawerAdapter(
    private val items: MutableList<DrawerItem>,
    private val onItemClick: (DrawerItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_GROUP = 0
        private const val TYPE_CHILD = 1
        private const val TYPE_SIMPLE = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is DrawerItem.Group -> TYPE_GROUP
            is DrawerItem.Child -> TYPE_CHILD
            is DrawerItem.Simple -> TYPE_SIMPLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_GROUP -> GroupViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.drawer_item_group, parent, false)
            )
            TYPE_CHILD -> ChildViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.drawer_item_child, parent, false)
            )
            TYPE_SIMPLE -> SimpleViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.drawer_item_simple, parent, false)
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DrawerItem.Group -> (holder as GroupViewHolder).bind(item, position)
            is DrawerItem.Child -> (holder as ChildViewHolder).bind(item)
            is DrawerItem.Simple -> (holder as SimpleViewHolder).bind(item)
        }
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val title: TextView = itemView.findViewById(R.id.title)
        fun bind(item: DrawerItem.Group, position: Int) {
            icon.setImageResource(item.iconRes)
            title.text = item.title
            itemView.setOnClickListener {
                if (!item.expanded) {
                    // Collapse all other groups first
                    collapseAllGroups()
                    // Expand this group
                    item.expanded = true
                    items.addAll(position + 1, item.children)
                    notifyItemRangeInserted(position + 1, item.children.size)
                    notifyItemChanged(position)
                } else {
                    // Collapse this group
                    item.expanded = false
                    items.subList(position + 1, position + 1 + item.children.size).clear()
                    notifyItemRangeRemoved(position + 1, item.children.size)
                    notifyItemChanged(position)
                }
            }
        }
    }

    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val title: TextView = itemView.findViewById(R.id.title)
        fun bind(item: DrawerItem.Child) {
            icon.setImageResource(item.iconRes)
            title.text = item.title
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    inner class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val title: TextView = itemView.findViewById(R.id.title)
        fun bind(item: DrawerItem.Simple) {
            icon.setImageResource(item.iconRes)
            title.text = item.title
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    private fun collapseAllGroups() {
        val groups = items.filterIsInstance<DrawerItem.Group>()
        for (group in groups) {
            if (group.expanded) {
                group.expanded = false
                val index = items.indexOf(group)
                items.subList(index + 1, index + 1 + group.children.size).clear()
                notifyItemRangeRemoved(index + 1, group.children.size)
                notifyItemChanged(index)
            }
        }
    }

    fun collapseAllGroupsAndNotify() {
        var i = 0
        while (i < items.size) {
            val item = items[i]
            if (item is DrawerItem.Group && item.expanded) {
                item.expanded = false
                items.subList(i + 1, i + 1 + item.children.size).clear()
                notifyItemRangeRemoved(i + 1, item.children.size)
                notifyItemChanged(i)
            }
            i++
        }
    }
}
