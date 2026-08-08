package fr.larmoirecommune.app.ui.objects

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
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
    private var selectedThursday: Calendar = firstUpcomingThursday()
    private val thursdayChipRefs = mutableListOf<Pair<Chip, Calendar>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            applicationContext,
            applicationContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )

        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        objectId = intent.getIntExtra("OBJECT_ID", -1)

        setupMap()
        setupWeekSelector()

        viewModel.lieux.observe(this) { lieux -> updateMapMarkers(lieux) }
        viewModel.loadLieux()

        viewModel.bookedRanges.observe(this) { _ -> buildThursdayChips() }
        viewModel.loadReservationsForObjet(objectId)

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
                selectedThursday.get(Calendar.YEAR),
                selectedThursday.get(Calendar.MONTH) + 1,
                selectedThursday.get(Calendar.DAY_OF_MONTH)
            )
            viewModel.createReservation(objectId, selectedLieuId, dateStr, nbSemaines)
        }
    }

    private fun buildThursdayChips() {
        val bookedRanges = viewModel.bookedRanges.value ?: emptyList()
        thursdayChipRefs.clear()
        binding.thursdayChipGroup.removeAllViews()

        val cal = firstUpcomingThursday()
        var firstAvailableSet = false

        repeat(10) {
            val thursday = cal.clone() as Calendar
            val isBooked = isThursdayBooked(thursday, bookedRanges)

            val chip = Chip(this).apply {
                text = formatChipDate(thursday)
                isCheckable = true
                isEnabled = !isBooked
                alpha = if (isBooked) 0.4f else 1.0f
            }
            chip.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    selectedThursday = thursday.clone() as Calendar
                    updateDateSummary()
                }
            }
            binding.thursdayChipGroup.addView(chip)
            thursdayChipRefs.add(Pair(chip, thursday.clone() as Calendar))

            if (!isBooked && !firstAvailableSet) {
                chip.isChecked = true
                selectedThursday = thursday.clone() as Calendar
                firstAvailableSet = true
            }

            cal.add(Calendar.WEEK_OF_YEAR, 1)
        }

        updateDateSummary()
    }

    private fun isThursdayBooked(thursday: Calendar, bookedRanges: List<Pair<String, String>>): Boolean {
        val newStart = thursday.timeInMillis
        val newEndCal = thursday.clone() as Calendar
        newEndCal.add(Calendar.DAY_OF_YEAR, 7 * nbSemaines - 1)
        val newEnd = newEndCal.timeInMillis

        return bookedRanges.any { (startStr, endStr) ->
            val exStart = parseIsoToMillis(startStr)
            val exEnd = parseIsoToMillis(endStr)
            newStart <= exEnd && newEnd >= exStart
        }
    }

    private fun parseIsoToMillis(iso: String): Long {
        return try {
            val date = iso.substringBefore("T")
            val (y, m, d) = date.split("-").map { it.toInt() }
            val cal = Calendar.getInstance()
            cal.set(y, m - 1, d, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        } catch (e: Exception) { 0L }
    }

    private fun formatChipDate(cal: Calendar): String {
        return String.format(
            "%02d/%02d/%04d",
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR)
        )
    }

    private fun setupWeekSelector() {
        binding.rgWeeks.setOnCheckedChangeListener { _, checkedId ->
            nbSemaines = when (checkedId) {
                R.id.rb2weeks -> 2
                R.id.rb3weeks -> 3
                else -> 1
            }
            buildThursdayChips()
        }
        binding.rb1week.isChecked = true
    }

    private fun updateDateSummary() {
        val endCal = selectedThursday.clone() as Calendar
        endCal.add(Calendar.DAY_OF_YEAR, 6 + 7 * (nbSemaines - 1))

        val startStr = String.format(
            "%02d/%02d/%04d",
            selectedThursday.get(Calendar.DAY_OF_MONTH),
            selectedThursday.get(Calendar.MONTH) + 1,
            selectedThursday.get(Calendar.YEAR)
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

    companion object {
        fun firstUpcomingThursday(): Calendar {
            val cal = Calendar.getInstance()
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return cal
        }
    }
}
