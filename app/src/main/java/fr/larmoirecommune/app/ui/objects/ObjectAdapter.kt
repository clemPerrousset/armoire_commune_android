package fr.larmoirecommune.app.ui.objects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter // Import this
import androidx.recyclerview.widget.RecyclerView
import fr.larmoirecommune.app.databinding.ItemObjectBinding
import fr.larmoirecommune.app.model.Objet

class ObjectAdapter(
    private val onItemClick: (Objet) -> Unit
) : ListAdapter<Objet, ObjectAdapter.ObjectViewHolder>(ObjectDiffCallback()) {

    // Define how to detect changes between list items
    class ObjectDiffCallback : DiffUtil.ItemCallback<Objet>() {
        override fun areItemsTheSame(oldItem: Objet, newItem: Objet): Boolean {
            // Compare unique IDs (adjust 'id' to whatever your unique field is)
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Objet, newItem: Objet): Boolean {
            // Compare the content of the data class
            return oldItem == newItem
        }
    }

    inner class ObjectViewHolder(val binding: ItemObjectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectViewHolder {
        val binding = ItemObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ObjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ObjectViewHolder, position: Int) {
        // getItem(position) is provided by ListAdapter
        val item = getItem(position)

        holder.binding.objectName.text = item.nom
        holder.binding.objectDesc.text = item.description
        holder.binding.objectStatus.text = if (item.disponibiliteGlobale) "Disponible" else "Indisponible"

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

}