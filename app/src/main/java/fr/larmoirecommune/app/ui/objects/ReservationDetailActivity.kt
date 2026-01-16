package fr.larmoirecommune.app.ui.objects

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import fr.larmoirecommune.app.databinding.ActivityReservationDetailBinding
import fr.larmoirecommune.app.viewmodel.ReservationDetailViewModel

class ReservationDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReservationDetailBinding

    // On suppose l'existence d'un ViewModel dédié ou partagé
    private val viewModel: ReservationDetailViewModel by viewModels()
    private var reservationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Récupération de l'ID passé par l'intent
        reservationId = intent.getIntExtra("RESERVATION_ID", -1)
        if (reservationId == -1) {
            Toast.makeText(this, "Erreur: Réservation introuvable", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. Observer les données
        viewModel.reservation.observe(this) { res ->
            if (res != null) {
                // Statut
                binding.resStatus.text = "Statut : ${res.status}"

                // Dates
                binding.resDates.text = "Du ${res.dateDebut}\nau ${res.dateFin}"

                // Infos de l'objet (Relation imbriquée)
                binding.resObjectName.text = res.objet?.nom ?: "Objet inconnu (ID: ${res.objetId})"

                // Infos du lieu (Relation imbriquée)
                binding.resLocation.text = res.lieu?.nom ?: "Lieu non précisé"

                // Gestion du bouton Annuler selon le statut
                binding.cancelButton.isEnabled = (res.status == "active" || res.status == "confirmee")
            }
        }

        // 3. Charger les données
        viewModel.loadReservation(reservationId)

        // 4. Action du bouton
        binding.cancelButton.setOnClickListener {
            // Logique d'annulation (appel API via ViewModel)
            // viewModel.cancelReservation(reservationId)
            Toast.makeText(this, "Fonctionnalité d'annulation à venir", Toast.LENGTH_SHORT).show()
        }
    }
}