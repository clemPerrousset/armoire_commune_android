package fr.larmoirecommune.app.ui.objects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.larmoirecommune.app.databinding.ItemReservationBinding

class ReservationAdapter(
    private val onItemClick: (Reservation) -> Unit
) : ListAdapter<Reservation, ReservationAdapter.ReservationViewHolder>(ReservationDiffCallback()) {

    // On définit comment comparer les réservations pour mettre à jour la liste intelligemment
    class ReservationDiffCallback : DiffUtil.ItemCallback<Reservation>() {
        override fun areItemsTheSame(oldItem: Reservation, newItem: Reservation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reservation, newItem: Reservation): Boolean {
            return oldItem == newItem
        }
    }

    inner class ReservationViewHolder(val binding: ItemReservationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val binding = ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReservationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        // Avec ListAdapter, on récupère l'objet via getItem(position)
        val item = getItem(position)

        // Gestion du nom de l'objet (par défaut l'ID, sinon le nom si l'objet est chargé)
        holder.binding.resObjectName.text = "Objet #${item.objetId}"
        item.objet?.let {
            holder.binding.resObjectName.text = it.nom
        }

        // Affichage des dates et du statut
        holder.binding.resDates.text = "Du ${item.dateDebut} au ${item.dateFin}"
        holder.binding.resStatus.text = item.status

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }
}