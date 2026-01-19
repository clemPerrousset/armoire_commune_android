package fr.larmoirecommune.app.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.larmoirecommune.app.databinding.ActivityAdminReservationsBinding
import fr.larmoirecommune.app.databinding.ItemReservationBinding
import fr.larmoirecommune.app.model.Reservation // <--- C'EST LUI QUI MANQUAIT
import fr.larmoirecommune.app.repository.AdminRepository
import kotlinx.coroutines.launch

class AdminReservationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminReservationsBinding
    private val repository = AdminRepository()

    // Le lambda attend un objet Reservation, donc l'import est obligatoire ici aussi
    private val adapter = AdminReservationAdapter { reservation ->
        lifecycleScope.launch {
            reservation.id?.let { id ->
                val success = repository.returnObject(id)
                if (success) {
                    Toast.makeText(this@AdminReservationsActivity, "Objet retourné", Toast.LENGTH_SHORT).show()
                    loadReservations()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminReservationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.reservationRecycler.layoutManager = LinearLayoutManager(this)
        binding.reservationRecycler.adapter = adapter

        loadReservations()
    }

    private fun loadReservations() {
        lifecycleScope.launch {
            val list = repository.getAllReservations()
            adapter.submitList(list)
        }
    }
}

class AdminReservationAdapter(private val onReturnClick: (Reservation) -> Unit) : RecyclerView.Adapter<AdminReservationAdapter.ViewHolder>() {
    private var list: List<Reservation> = emptyList()

    fun submitList(newList: List<Reservation>) {
        list = newList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemReservationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // J'ai mis à jour ici pour correspondre à ton nouveau modèle Models.kt
        // Note: item.objetId (camelCase) et item.dateDebut (camelCase)
        holder.binding.resObjectName.text = item.objet?.nom ?: "Objet #${item.objetId}"
        holder.binding.resDates.text = "Du ${item.dateDebut} au ${item.dateFin}"
        holder.binding.resStatus.text = item.status

        holder.itemView.setOnClickListener {
            if (item.status == "active") {
                onReturnClick(item)
            }
        }
    }

    override fun getItemCount() = list.size
}