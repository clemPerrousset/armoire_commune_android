package fr.larmoirecommune.app.ui.objects

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import fr.larmoirecommune.app.databinding.ActivityObjectDetailBinding
import fr.larmoirecommune.app.network.ApiClient
import fr.larmoirecommune.app.repository.ObjectRepository
import fr.larmoirecommune.app.repository.UserRepository
import fr.larmoirecommune.app.ui.auth.LoginActivity
import fr.larmoirecommune.app.viewmodel.ObjectDetailViewModel
import kotlinx.coroutines.launch

class ObjectDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObjectDetailBinding
    private val viewModel: ObjectDetailViewModel by viewModels()
    private var objectId: Int = -1
    private val userRepository = UserRepository()
    private val objectRepository = ObjectRepository()
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        objectId = intent.getIntExtra("OBJECT_ID", -1)
        if (objectId == -1) { finish(); return }

        viewModel.objectDetail.observe(this) { obj ->
            if (obj == null) return@observe
            binding.detailName.text = obj.nom
            binding.detailDesc.text = obj.description
            binding.reserveButton.isEnabled = obj.disponibiliteGlobale

            if (!obj.image.isNullOrBlank()) {
                binding.objectImage.load(ApiClient.getUrl(obj.image)) {
                    crossfade(true)
                    error(android.R.drawable.ic_menu_gallery)
                }
            }
        }

        viewModel.loadObject(objectId)

        // Masquer le bouton QR code (inutile côté utilisateur)
        binding.btnQrCode.visibility = View.GONE

        // Initialiser l'état du bouton favoris avant tout clic
        if (ApiClient.token != null) {
            lifecycleScope.launch { initFavoriteState() }
        } else {
            // Non connecté : bouton favoris vide (non rempli)
            binding.btnFavorite.alpha = 0.4f
        }

        binding.btnFavorite.setOnClickListener {
            if (ApiClient.token == null) {
                Toast.makeText(this, "Connectez-vous pour ajouter aux favoris", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                if (isFavorite) {
                    if (userRepository.removeFavori(objectId)) {
                        isFavorite = false
                        binding.btnFavorite.alpha = 0.4f
                        Toast.makeText(this@ObjectDetailActivity, "Retiré des favoris", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (userRepository.addFavori(objectId)) {
                        isFavorite = true
                        binding.btnFavorite.alpha = 1.0f
                        Toast.makeText(this@ObjectDetailActivity, "Ajouté aux favoris", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Calendrier : affichage des semaines réservées
        loadReservationCalendar()

        binding.reserveButton.setOnClickListener {
            if (ApiClient.token == null) {
                val loginIntent = Intent(this, LoginActivity::class.java)
                loginIntent.putExtra("FINISH_ONLY", true)
                startActivity(loginIntent)
            } else {
                startActivity(Intent(this, ReservationActivity::class.java).apply {
                    putExtra("OBJECT_ID", objectId)
                })
            }
        }
    }

    private suspend fun initFavoriteState() {
        val favoris = userRepository.getFavoris()
        isFavorite = favoris.any { it.id == objectId }
        binding.btnFavorite.alpha = if (isFavorite) 1.0f else 0.4f
    }

    private fun loadReservationCalendar() {
        lifecycleScope.launch {
            val reservations = objectRepository.getReservationsForObjet(objectId)
            if (reservations.isEmpty()) {
                binding.tvCalendar.text = "Aucune réservation en cours"
                return@launch
            }
            val lines = reservations.joinToString("\n") { res ->
                "• Du ${formatDate(res.dateDebut)} au ${formatDate(res.dateFin)} (${statusLabel(res.status)})"
            }
            binding.tvCalendar.text = "Semaines réservées :\n$lines"
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
        "active"   -> "réservé"
        "en_cours" -> "en cours d'emprunt"
        else       -> status
    }
}
