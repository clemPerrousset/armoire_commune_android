package fr.larmoirecommune.app.ui.objects

import fr.larmoirecommune.app.model.Reservation
import fr.larmoirecommune.app.model.ReservationStatus
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.larmoirecommune.app.databinding.ItemReservationBinding

class ReservationAdapter(
    private val onItemClick: (Reservation) -> Unit
) : ListAdapter<Reservation, ReservationAdapter.ReservationViewHolder>(ReservationDiffCallback()) {

    class ReservationDiffCallback : DiffUtil.ItemCallback<Reservation>() {
        override fun areItemsTheSame(oldItem: Reservation, newItem: Reservation) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Reservation, newItem: Reservation) = oldItem == newItem
    }

    inner class ReservationViewHolder(val binding: ItemReservationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val binding = ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReservationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.resObjectName.text = item.objet?.nom ?: "Objet #${item.objetId}"
        holder.binding.resDates.text = "Du ${formatDate(item.dateDebut)} au ${formatDate(item.dateFin)}"
        holder.binding.resStatus.text = ReservationStatus.label(item.status)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    /** ISO 8601 → JJ/MM/AAAA */
    private fun formatDate(iso: String): String {
        return try {
            // Format attendu : "2025-01-09T10:00:00" ou "2025-01-09"
            val date = iso.substringBefore("T")
            val parts = date.split("-")
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } catch (e: Exception) {
            iso
        }
    }

}
