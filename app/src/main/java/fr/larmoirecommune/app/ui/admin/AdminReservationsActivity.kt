package fr.larmoirecommune.app.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import fr.larmoirecommune.app.model.ReservationStatus
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.larmoirecommune.app.databinding.ActivityAdminReservationsBinding
import fr.larmoirecommune.app.databinding.ItemReservationBinding
import fr.larmoirecommune.app.model.Reservation
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.ui.objects.ObjectDetailActivity
import kotlinx.coroutines.launch

class AdminReservationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminReservationsBinding
    private val repository = AdminRepository()
    private var activeFilter: String? = null

    private val nextStatus = mapOf(
        "en_preparation" to "mis_a_disposition",
        "mis_a_disposition" to "retire",
        "retire" to "restitue",
        "restitue" to "en_verification"
    )

    private val adapter = AdminReservationAdapter { reservation ->
        val next = nextStatus[reservation.status]
        val builder = AlertDialog.Builder(this)
            .setTitle(reservation.objet?.nom ?: "Objet #${reservation.objetId}")
            .setNeutralButton("Voir l'objet") { _, _ ->
                startActivity(Intent(this, ObjectDetailActivity::class.java).apply {
                    putExtra("OBJECT_ID", reservation.objetId)
                })
            }
            .setNegativeButton("Fermer", null)
        if (next != null) {
            builder.setMessage("Statut : ${ReservationStatus.label(reservation.status)}")
            builder.setPositiveButton("Passer à « ${ReservationStatus.label(next)} »") { _, _ ->
                lifecycleScope.launch {
                    val ok = repository.avancerReservation(reservation.id ?: return@launch)
                    Toast.makeText(this@AdminReservationsActivity, if (ok) "Statut mis à jour" else "Erreur", Toast.LENGTH_SHORT).show()
                    loadReservations()
                }
            }
        } else {
            builder.setMessage("Statut : ${ReservationStatus.label(reservation.status)}")
        }
        builder.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminReservationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.reservationRecycler.layoutManager = LinearLayoutManager(this)
        binding.reservationRecycler.adapter = adapter

        setupFilters()
        loadReservations()
    }

    private fun setupFilters() {
        // Chip "Toutes" sélectionné par défaut
        binding.chipAll.isChecked = true

        val chips = listOf(
            binding.chipAll to null,
            binding.chipActive to "en_preparation",
            binding.chipMisADispo to "mis_a_disposition",
            binding.chipEnCours to "retire",
            binding.chipRestitue to "restitue",
            binding.chipVerification to "en_verification",
            binding.chipTerminee to "terminee",
            binding.chipAnnulee to "annulee"
        )

        chips.forEach { (chip, status) ->
            chip.setOnClickListener {
                chips.forEach { (c, _) -> c.isChecked = false }
                chip.isChecked = true
                activeFilter = status
                loadReservations()
            }
        }
    }

    private fun loadReservations() {
        lifecycleScope.launch {
            val list = repository.getAllReservations(activeFilter)
            adapter.submitList(list)
        }
    }
}

class AdminReservationAdapter(
    private val onItemClick: (Reservation) -> Unit
) : RecyclerView.Adapter<AdminReservationAdapter.ViewHolder>() {
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
        holder.binding.resObjectName.text = item.objet?.nom ?: "Objet #${item.objetId}"
        holder.binding.resDates.text = "Du ${formatDate(item.dateDebut)} au ${formatDate(item.dateFin)}"
        holder.binding.resLocation.text = item.lieu?.let { "À déposer : ${it.nom} — ${it.adresse}" }
            ?: "Lieu non précisé"
        holder.binding.resStatus.text = statusLabel(item.status)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = list.size

    private fun formatDate(iso: String): String {
        return try {
            val date = iso.substringBefore("T")
            val parts = date.split("-")
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } catch (e: Exception) { iso }
    }

    private fun statusLabel(status: String) = fr.larmoirecommune.app.model.ReservationStatus.label(status)
}
