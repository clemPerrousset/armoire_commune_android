package fr.larmoirecommune.app.ui.objects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.larmoirecommune.app.databinding.ItemReservationBinding
import fr.larmoirecommune.app.model.Reservation
import java.text.SimpleDateFormat
import java.util.Locale

class ReservationAdapter(
    private val items: List<Reservation>,
    private val onItemClick: (Reservation) -> Unit
) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {

    inner class ReservationViewHolder(val binding: ItemReservationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val binding = ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReservationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val item = items[position]
        // Assuming dates are formatted strings or can be formatted.
        // Based on model, they might be Strings or Date objects depending on serialization.
        // The API returns ISO strings, Ktor serialization might have parsed them or left as strings.
        // Let's assume they are Strings as per typical JSON handling if not using custom serializers

        holder.binding.resObjectName.text = "Objet #${item.objet_id}" // API doesn't nest full object by default usually, unless expanded
        // But the model had: objet: Optional[Objet] = Relationship(...)
        // If Ktor populated it:
        item.objet?.let {
            holder.binding.resObjectName.text = it.nom
        }

        holder.binding.resDates.text = "Du ${item.date_debut} au ${item.date_fin}"
        holder.binding.resStatus.text = item.status

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
