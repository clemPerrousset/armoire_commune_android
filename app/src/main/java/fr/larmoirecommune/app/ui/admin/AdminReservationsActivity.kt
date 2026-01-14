package fr.larmoirecommune.app.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import fr.larmoirecommune.app.databinding.ActivityAdminReservationsBinding
import fr.larmoirecommune.app.databinding.ItemReservationBinding
import fr.larmoirecommune.app.model.Reservation
import fr.larmoirecommune.app.repository.AdminRepository
import kotlinx.coroutines.launch

class AdminReservationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminReservationsBinding
    private val repository = AdminRepository()
    private val adapter = AdminReservationAdapter { reservation ->
         lifecycleScope.launch {
             val success = repository.returnObject(reservation.id!!)
             if (success) {
                 Toast.makeText(this@AdminReservationsActivity, "Objet retourné", Toast.LENGTH_SHORT).show()
                 loadReservations()
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
        holder.binding.resObjectName.text = item.objet?.nom ?: "Objet #${item.objet_id}"
        holder.binding.resDates.text = "Du ${item.date_debut} au ${item.date_fin}"
        holder.binding.resStatus.text = item.status

        holder.itemView.setOnClickListener {
             if (item.status == "active") {
                 onReturnClick(item)
             }
        }
    }

    override fun getItemCount() = list.size
}
