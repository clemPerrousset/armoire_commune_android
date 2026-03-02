package fr.larmoirecommune.app.ui.objects

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.larmoirecommune.app.R
import fr.larmoirecommune.app.model.Tag
import kotlin.random.Random

class TagAdapter(private val onTagClick: (Tag?) -> Unit) :
    ListAdapter<Tag, TagAdapter.TagViewHolder>(TagDiffCallback()) {

    // 5 Pastel colors
    private val pastelColors = listOf(
        "#FFB3BA", // Pastel Pink
        "#FFDFBA", // Pastel Orange
        "#FFFFBA", // Pastel Yellow
        "#BAFFC9", // Pastel Green
        "#BAE1FF"  // Pastel Blue
    )

    // Store assigned colors so they don't change on scroll
    private val colorMap = mutableMapOf<Int, Int>()

    var selectedTagId: Int? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = getItem(position)
        holder.bind(tag)
    }

    inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tagText: TextView = itemView.findViewById(R.id.tagText)

        fun bind(tag: Tag) {
            tagText.text = tag.nom

            // Assign color if not assigned yet
            val tagId = tag.id ?: 0
            if (!colorMap.containsKey(tagId)) {
                val colorHex = pastelColors[Random.nextInt(pastelColors.size)]
                colorMap[tagId] = Color.parseColor(colorHex)
            }

            val bgColor = colorMap[tagId] ?: Color.WHITE

            // Handle selection visual state
            if (selectedTagId == tag.id) {
                // Selected: apply the color fully, maybe make text bolder or black
                tagText.backgroundTintList = ColorStateList.valueOf(bgColor)
                tagText.alpha = 1.0f
                tagText.setTextColor(Color.BLACK)
                tagText.setPadding(32, 8, 32, 8) // slightly bigger
            } else {
                // Not selected: slightly transparent or less vibrant
                tagText.backgroundTintList = ColorStateList.valueOf(bgColor)
                tagText.alpha = 0.6f
                tagText.setTextColor(Color.DKGRAY)
                tagText.setPadding(16, 8, 16, 8)
            }

            itemView.setOnClickListener {
                if (selectedTagId == tag.id) {
                    // Deselect
                    selectedTagId = null
                    onTagClick(null)
                } else {
                    // Select
                    selectedTagId = tag.id
                    onTagClick(tag)
                }
                notifyDataSetChanged()
            }
        }
    }

    class TagDiffCallback : DiffUtil.ItemCallback<Tag>() {
        override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
            return oldItem == newItem
        }
    }
}
