package fr.larmoirecommune.app.ui.objects

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import fr.larmoirecommune.app.databinding.ActivityReservationBinding
import fr.larmoirecommune.app.viewmodel.ReservationViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class ReservationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReservationBinding
    private val viewModel: ReservationViewModel by viewModels()
    private var objectId: Int = -1
    private var selectedLieuId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, applicationContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        objectId = intent.getIntExtra("OBJECT_ID", -1)

        setupMap()

        viewModel.reservationResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Réservation confirmée", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show()
            }
        }

        binding.confirmButton.setOnClickListener {
            val day = binding.datePicker.dayOfMonth
            val month = binding.datePicker.month
            val year = binding.datePicker.year
            val dateStr = String.format("%04d-%02d-%02dT10:00:00", year, month + 1, day)

            viewModel.createReservation(objectId, selectedLieuId, dateStr)
        }
    }

    private fun setupMap() {
        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.controller.setZoom(15.0)
        val startPoint = GeoPoint(47.3220, 5.0415)
        binding.map.controller.setCenter(startPoint)

        val marker = Marker(binding.map)
        marker.position = startPoint
        marker.title = "La Ressourcerie"
        marker.setOnMarkerClickListener { m, _ ->
            selectedLieuId = 1
            Toast.makeText(this, "Lieu sélectionné: ${m.title}", Toast.LENGTH_SHORT).show()
            true
        }
        binding.map.overlays.add(marker)
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }
}
