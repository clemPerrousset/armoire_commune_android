package fr.larmoirecommune.app.ui.objects

import android.content.Context
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import fr.larmoirecommune.app.R
import fr.larmoirecommune.app.databinding.ActivityReservationBinding
import fr.larmoirecommune.app.model.Lieu
import fr.larmoirecommune.app.viewmodel.ReservationViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.util.Calendar

class ReservationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReservationBinding
    private val viewModel: ReservationViewModel by viewModels()
    private var objectId: Int = -1
    private var selectedLieuId: Int = -1
    private var nbSemaines: Int = 1

    // Date de début = prochain jeudi (calculée une seule fois, non modifiable)
    private lateinit var nextThursday: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            applicationContext,
            applicationContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )

        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        objectId = intent.getIntExtra("OBJECT_ID", -1)

        nextThursday = nextThursdayCalendar()

        setupMap()
        setupWeekSelector()
        updateDateSummary()

        viewModel.lieux.observe(this) { lieux -> updateMapMarkers(lieux) }
        viewModel.loadLieux()

        viewModel.reservationResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Réservation confirmée !", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Erreur : objet déjà réservé sur cette période", Toast.LENGTH_LONG).show()
            }
        }

        binding.confirmButton.setOnClickListener {
            if (selectedLieuId == -1) {
                Toast.makeText(this, "Sélectionnez un point de retrait sur la carte", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dateStr = String.format(
                "%04d-%02d-%02dT10:00:00",
                nextThursday.get(Calendar.YEAR),
                nextThursday.get(Calendar.MONTH) + 1,
                nextThursday.get(Calendar.DAY_OF_MONTH)
            )
            viewModel.createReservation(objectId, selectedLieuId, dateStr, nbSemaines)
        }
    }

    private fun nextThursdayCalendar(): Calendar {
        val cal = Calendar.getInstance()
        // Avancer jusqu'au prochain jeudi (inclus si aujourd'hui c'est jeudi)
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal
    }

    private fun setupWeekSelector() {
        // Le RadioGroup rb_weeks doit exister dans le layout avec 3 RadioButtons
        // rb_1week (1 semaine), rb_2weeks (2 semaines), rb_3weeks (3 semaines)
        binding.rgWeeks.setOnCheckedChangeListener { _, checkedId ->
            nbSemaines = when (checkedId) {
                R.id.rb2weeks -> 2
                R.id.rb3weeks -> 3
                else -> 1
            }
            updateDateSummary()
        }
        // Sélection par défaut : 1 semaine
        binding.rb1week.isChecked = true
    }

    private fun updateDateSummary() {
        val endCal = nextThursdayCalendar()
        endCal.add(Calendar.DAY_OF_YEAR, 6 + 7 * (nbSemaines - 1))

        val startStr = String.format(
            "%02d/%02d/%04d",
            nextThursday.get(Calendar.DAY_OF_MONTH),
            nextThursday.get(Calendar.MONTH) + 1,
            nextThursday.get(Calendar.YEAR)
        )
        val endStr = String.format(
            "%02d/%02d/%04d",
            endCal.get(Calendar.DAY_OF_MONTH),
            endCal.get(Calendar.MONTH) + 1,
            endCal.get(Calendar.YEAR)
        )

        binding.tvDateSummary.text = "Retrait : $startStr\nRetour avant : $endStr"
    }

    private fun setupMap() {
        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.setMultiTouchControls(true)
        binding.map.controller.setZoom(13.0)
        binding.map.controller.setCenter(GeoPoint(47.3220, 5.0415))
    }

    private fun updateMapMarkers(lieux: List<Lieu>) {
        binding.map.overlays.clear()

        if (lieux.isEmpty()) return

        var minLat = Double.MAX_VALUE; var maxLat = -Double.MAX_VALUE
        var minLon = Double.MAX_VALUE; var maxLon = -Double.MAX_VALUE

        for (lieu in lieux) {
            if (lieu.lat < minLat) minLat = lieu.lat
            if (lieu.lat > maxLat) maxLat = lieu.lat
            if (lieu.long < minLon) minLon = lieu.long
            if (lieu.long > maxLon) maxLon = lieu.long

            val marker = Marker(binding.map)
            marker.position = GeoPoint(lieu.lat, lieu.long)
            marker.title = lieu.nom
            // Afficher les horaires dans la sous-description si disponibles
            marker.subDescription = buildString {
                append(lieu.adresse)
                lieu.description?.let { append("\n$it") }
            }

            marker.setOnMarkerClickListener { m, _ ->
                lieu.id?.let { id -> selectedLieuId = id }
                m.showInfoWindow()
                Toast.makeText(this, "Lieu sélectionné : ${lieu.nom}", Toast.LENGTH_SHORT).show()
                true
            }
            binding.map.overlays.add(marker)
        }

        binding.map.invalidate()

        if (lieux.size > 1) {
            binding.map.post {
                binding.map.zoomToBoundingBox(BoundingBox(maxLat, maxLon, minLat, minLon), true)
            }
        } else if (lieux.size == 1) {
            binding.map.controller.setCenter(GeoPoint(lieux[0].lat, lieux[0].long))
        }
    }

    override fun onResume() { super.onResume(); binding.map.onResume() }
    override fun onPause() { super.onPause(); binding.map.onPause() }
}
