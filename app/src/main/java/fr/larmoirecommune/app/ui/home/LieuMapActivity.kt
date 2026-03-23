package fr.larmoirecommune.app.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.larmoirecommune.app.databinding.ActivityLieuMapBinding
import fr.larmoirecommune.app.model.Lieu
import fr.larmoirecommune.app.repository.ObjectRepository
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class LieuMapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLieuMapBinding
    private val repository = ObjectRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configuration d'OSMDroid avant d'inflater le layout
        Configuration.getInstance().userAgentValue = packageName

        binding = ActivityLieuMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        setupMap()
        loadLieux()
    }

    private fun setupMap() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        val mapController = binding.mapView.controller
        mapController.setZoom(13.0)
        // Centrer sur Dijon par défaut
        val startPoint = GeoPoint(47.3220, 5.0415)
        mapController.setCenter(startPoint)
    }

    private fun loadLieux() {
        lifecycleScope.launch {
            val lieux = repository.getLieux()
            if (lieux.isEmpty()) {
                Toast.makeText(this@LieuMapActivity, "Aucun point de retrait trouvé.", Toast.LENGTH_SHORT).show()
            } else {
                addMarkers(lieux)
            }
        }
    }

    private fun addMarkers(lieux: List<Lieu>) {
        for (lieu in lieux) {
            val marker = Marker(binding.mapView)
            marker.position = GeoPoint(lieu.lat, lieu.long)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = lieu.nom
            marker.snippet = lieu.adresse
            binding.mapView.overlays.add(marker)
        }
        binding.mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }
}
