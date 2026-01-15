package fr.larmoirecommune.app.ui.admin

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.larmoirecommune.app.databinding.ActivityAdminCreateLieuBinding
import android.text.Editable
import android.text.TextWatcher
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.utils.GeoUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class AdminCreateLieuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminCreateLieuBinding
    private val repository = AdminRepository()
    private var selectedPoint: GeoPoint? = null
    private var searchJob: Job? = null
    private var isUpdatingAddress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, applicationContext.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        binding = ActivityAdminCreateLieuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMap()
        setupAddressSearch()

        binding.validateButton.setOnClickListener {
            val nom = binding.lieuName.text.toString()
            val adresse = binding.lieuAddress.text.toString()

            if (selectedPoint == null) {
                Toast.makeText(this, "Sélectionnez un point sur la carte", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val success = repository.createLieu(nom, selectedPoint!!.latitude, selectedPoint!!.longitude, adresse)
                if (success) {
                    Toast.makeText(this@AdminCreateLieuActivity, "Lieu créé", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AdminCreateLieuActivity, "Erreur", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupMap() {
        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.controller.setZoom(15.0)
        val startPoint = GeoPoint(47.3220, 5.0415)
        binding.map.controller.setCenter(startPoint)

        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                // Just move marker, don't reverse geocode on single tap (UX choice)
                p?.let {
                    selectedPoint = it
                    updateMarker(it)
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                p?.let {
                    selectedPoint = it
                    updateMarker(it)
                    // Reverse Geocode
                    lifecycleScope.launch {
                        val address = GeoUtils.reverseGeocode(it.latitude, it.longitude)
                        if (address != null) {
                            isUpdatingAddress = true
                            binding.lieuAddress.setText(address)
                            isUpdatingAddress = false
                        }
                    }
                }
                return true
            }
        })
        binding.map.overlays.add(0, mapEventsOverlay)
    }

    private fun setupAddressSearch() {
        binding.lieuAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingAddress) return

                val query = s.toString()
                if (query.length > 5) {
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
                        delay(1000) // Debounce
                        val point = GeoUtils.searchAddress(query)
                        if (point != null) {
                            selectedPoint = point
                            updateMarker(point)
                            binding.map.controller.animateTo(point)
                        }
                    }
                }
            }
        })
    }

    private fun updateMarker(p: GeoPoint) {
        // Remove previous markers
        binding.map.overlays.removeAll { it is Marker }

        val marker = Marker(binding.map)
        marker.position = p
        marker.title = "Nouveau Lieu"
        binding.map.overlays.add(marker)
        binding.map.invalidate()
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
