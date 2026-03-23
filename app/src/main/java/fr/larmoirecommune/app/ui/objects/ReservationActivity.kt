package fr.larmoirecommune.app.ui.objects

import fr.larmoirecommune.app.model.Reservation
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
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.overlay.Marker
import fr.larmoirecommune.app.model.Lieu
import android.app.AlertDialog
import java.util.Calendar

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

        setupDatePicker()
        showInfoPopup()


        viewModel.lieux.observe(this) { lieux ->
            updateMapMarkers(lieux)
        }
        viewModel.loadLieux()

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


    private fun setupDatePicker() {
        // La réservation commence toujours le prochain Mercredi
        val calendar = Calendar.getInstance()

        // Si on est déjà mercredi, mais on veut peut-être le mercredi suivant selon l'heure
        // Pour faire simple, on trouve le prochain mercredi
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Fixer le minDate au prochain mercredi
        binding.datePicker.minDate = calendar.timeInMillis

        // Pré-selectionner cette date
        binding.datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }

    private fun showInfoPopup() {
        AlertDialog.Builder(this)
            .setTitle("Information sur la réservation")
            .setMessage("Les réservations fonctionnent par cycle d'une semaine :\n\n• Les retraits s'effectuent le mercredi soir (lors de notre tournée).\n• Les retours doivent être effectués au plus tard le mardi soir de la semaine suivante.\n\nLa date de début sélectionnée correspond au prochain mercredi disponible.")
            .setPositiveButton("J'ai compris", null)
            .show()
    }

    private fun setupMap() {
        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.setMultiTouchControls(true)
        binding.map.controller.setZoom(15.0)
        // Default center until we load points
        val startPoint = GeoPoint(47.3220, 5.0415)
        binding.map.controller.setCenter(startPoint)
    }

    private fun updateMapMarkers(lieux: List<Lieu>) {
        binding.map.overlays.clear()

        if (lieux.isEmpty()) return

        var minLat = Double.MAX_VALUE
        var maxLat = -Double.MAX_VALUE
        var minLon = Double.MAX_VALUE
        var maxLon = -Double.MAX_VALUE

        for (lieu in lieux) {
            val point = GeoPoint(lieu.lat, lieu.long)

            if (lieu.lat < minLat) minLat = lieu.lat
            if (lieu.lat > maxLat) maxLat = lieu.lat
            if (lieu.long < minLon) minLon = lieu.long
            if (lieu.long > maxLon) maxLon = lieu.long

            val marker = Marker(binding.map)
            marker.position = point
            marker.title = lieu.nom
            marker.subDescription = lieu.adresse

            marker.setOnMarkerClickListener { m, _ ->
                lieu.id?.let { id -> selectedLieuId = id }
                m.showInfoWindow()
                Toast.makeText(this@ReservationActivity, "Lieu sélectionné: ${lieu.nom}", Toast.LENGTH_SHORT).show()
                true
            }
            binding.map.overlays.add(marker)
        }

        binding.map.invalidate()

        // Center map to show all markers
        if (lieux.size > 1) {
            val boundingBox = BoundingBox(maxLat, maxLon, minLat, minLon)
            // Add slight padding to the bounding box if needed, osmdroid handles it
            binding.map.post {
                binding.map.zoomToBoundingBox(boundingBox, true)
            }
        } else if (lieux.size == 1) {
            binding.map.controller.setCenter(GeoPoint(lieux[0].lat, lieux[0].long))
            binding.map.controller.setZoom(15.0)
        }
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
