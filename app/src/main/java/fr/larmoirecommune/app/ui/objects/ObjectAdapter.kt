package fr.larmoirecommune.app.ui.objects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.larmoirecommune.app.databinding.ItemObjectBinding
import fr.larmoirecommune.app.model.Objet

class ObjectAdapter(
    private val items: List<Objet>,
    private val onItemClick: (Objet) -> Unit
) : RecyclerView.Adapter<ObjectAdapter.ObjectViewHolder>() {

    inner class ObjectViewHolder(val binding: ItemObjectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectViewHolder {
        val binding = ItemObjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ObjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ObjectViewHolder, position: Int) {
        val item = items[position]
        holder.binding.objectName.text = item.nom
        holder.binding.objectDesc.text = item.description
        holder.binding.objectStatus.text = if (item.disponibilite_globale) "Disponible" else "Indisponible"

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
