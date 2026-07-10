package fr.larmoirecommune.app.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import fr.larmoirecommune.app.databinding.ActivityReservationListBinding
import fr.larmoirecommune.app.repository.ObjectRepository
import fr.larmoirecommune.app.ui.objects.ReservationAdapter
import fr.larmoirecommune.app.ui.objects.ReservationDetailActivity
import kotlinx.coroutines.launch

class HistoryListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReservationListBinding
    private val repository = ObjectRepository()

    private val adapter = ReservationAdapter { reservation ->
        val intent = Intent(this, ReservationDetailActivity::class.java)
        intent.putExtra("RESERVATION_ID", reservation.id)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.reservationRecycler.layoutManager = LinearLayoutManager(this)
        binding.reservationRecycler.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            val history = repository.getReservationHistorique()
            adapter.submitList(history)
        }
    }
}
