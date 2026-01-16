package fr.larmoirecommune.app.ui.objects

import android.content.Intent // Nécessaire pour la navigation
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import fr.larmoirecommune.app.databinding.ActivityReservationListBinding
import fr.larmoirecommune.app.viewmodel.ReservationListViewModel

class ReservationListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReservationListBinding
    private val viewModel: ReservationListViewModel by viewModels()

    private val adapter = ReservationAdapter { reservation ->
        // Création de l'intention pour aller vers la page de détail
        val intent = Intent(this, ReservationDetailActivity::class.java)

        // On passe l'ID de la réservation pour que la page suivante sache quoi charger
        intent.putExtra("RESERVATION_ID", reservation.id)

        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.reservationRecycler.layoutManager = LinearLayoutManager(this)
        binding.reservationRecycler.adapter = adapter

        viewModel.reservations.observe(this) { list ->
            adapter.submitList(list)
        }

        viewModel.loadReservations()
    }
}