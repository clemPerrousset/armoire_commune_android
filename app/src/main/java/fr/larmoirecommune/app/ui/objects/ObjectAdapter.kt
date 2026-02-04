package fr.larmoirecommune.app.ui.objects

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.larmoirecommune.app.R
import fr.larmoirecommune.app.databinding.ItemObjectBinding
import fr.larmoirecommune.app.model.Objet

class ObjectAdapter(
    private val onItemClick: (Objet) -> Unit
) : ListAdapter<Objet, ObjectAdapter.ObjectViewHolder>(ObjectDiffCallback()) {

    class ObjectDiffCallback : DiffUtil.ItemCallback<Objet>() {
        override fun areItemsTheSame(oldItem: Objet, newItem: Objet): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Objet, newItem: Objet): Boolean = oldItem == newItem
    }

    inner class ObjectViewHolder(val binding: ItemObjectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectViewHolder {
        val binding = ItemObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ObjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ObjectViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context

        holder.binding.objectName.text = item.nom
        holder.binding.objectDesc.text = item.description

        // Status Logic
        if (item.disponibiliteGlobale) {
            holder.binding.objectStatus.text = "Disponible"
            holder.binding.objectStatus.setTextColor(ContextCompat.getColor(context, R.color.status_available))
            holder.binding.objectStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mint_light))
        } else {
            holder.binding.objectStatus.text = "Indisponible"
            holder.binding.objectStatus.setTextColor(ContextCompat.getColor(context, R.color.status_unavailable))
            holder.binding.objectStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEE2E2")) // Light Red
        }

        // Setup Buttons
        holder.binding.btnView.setOnClickListener { onItemClick(item) }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }
}
