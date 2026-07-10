package fr.larmoirecommune.app.ui.objects

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import fr.larmoirecommune.app.databinding.ActivityReservationDetailBinding
import fr.larmoirecommune.app.viewmodel.ReservationDetailViewModel

class ReservationDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReservationDetailBinding
    private val viewModel: ReservationDetailViewModel by viewModels()
    private var reservationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reservationId = intent.getIntExtra("RESERVATION_ID", -1)
        if (reservationId == -1) {
            Toast.makeText(this, "Réservation introuvable", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.reservation.observe(this) { res ->
            if (res == null) return@observe

            binding.resStatus.text = "Statut : ${statusLabel(res.status)}"
            binding.resDates.text = "Du ${formatDate(res.dateDebut)}\nau ${formatDate(res.dateFin)}"
            binding.resObjectName.text = res.objet?.nom ?: "Objet #${res.objetId}"
            binding.resLocation.text = res.lieu?.let { "${it.nom} — ${it.adresse}" } ?: "Lieu non précisé"

            // Le bouton annuler n'est actif que si la réservation est encore "active"
            val canCancel = res.status == "active"
            binding.cancelButton.isEnabled = canCancel
            binding.cancelButton.alpha = if (canCancel) 1f else 0.4f
        }

        viewModel.cancelResult.observe(this) { success ->
            when (success) {
                true  -> {
                    Toast.makeText(this, "Réservation annulée", Toast.LENGTH_SHORT).show()
                    finish()
                }
                false -> Toast.makeText(this, "Impossible d'annuler", Toast.LENGTH_SHORT).show()
                null  -> Unit
            }
        }

        viewModel.loadReservation(reservationId)

        binding.cancelButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Annuler la réservation")
                .setMessage("Voulez-vous vraiment annuler cette réservation ?")
                .setPositiveButton("Oui") { _, _ -> viewModel.cancelReservation(reservationId) }
                .setNegativeButton("Non", null)
                .show()
        }
    }

    private fun formatDate(iso: String): String {
        return try {
            val date = iso.substringBefore("T")
            val parts = date.split("-")
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } catch (e: Exception) { iso }
    }

    private fun statusLabel(status: String) = when (status) {
        "active"   -> "En attente de retrait"
        "en_cours" -> "En cours"
        "terminee" -> "Terminée"
        "annulee"  -> "Annulée"
        else       -> status
    }
}
