package fr.larmoirecommune.app.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import fr.larmoirecommune.app.R
import fr.larmoirecommune.app.databinding.ItemObjectDeleteBinding
import fr.larmoirecommune.app.model.Objet
import fr.larmoirecommune.app.network.ApiClient

class AdminDeleteObjectsAdapter(
    private val onDeleteClick: (Objet) -> Unit
) : ListAdapter<Objet, AdminDeleteObjectsAdapter.ViewHolder>(ObjetDiffCallback()) {

    class ObjetDiffCallback : DiffUtil.ItemCallback<Objet>() {
        override fun areItemsTheSame(oldItem: Objet, newItem: Objet) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Objet, newItem: Objet) = oldItem == newItem
    }

    inner class ViewHolder(val binding: ItemObjectDeleteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemObjectDeleteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.objectName.text = item.nom
        holder.binding.objectDesc.text = item.description

        if (!item.image.isNullOrBlank()) {
            holder.binding.objectImage.setPadding(0, 0, 0, 0)
            holder.binding.objectImage.load(ApiClient.getUrl(item.image)) {
                crossfade(true)
                placeholder(R.drawable.ic_objects)
                error(R.drawable.ic_objects)
            }
        } else {
            holder.binding.objectImage.setImageResource(R.drawable.ic_objects)
        }

        holder.binding.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    /** Objet actuellement affiché à la position donnée (pour le swipe-to-delete). */
    fun objetAt(position: Int): Objet = getItem(position)
}
