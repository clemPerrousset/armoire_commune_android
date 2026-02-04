package fr.larmoirecommune.app.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.larmoirecommune.app.R
import fr.larmoirecommune.app.databinding.ActivityProfileBinding
import fr.larmoirecommune.app.network.ApiClient
import fr.larmoirecommune.app.ui.auth.LoginActivity
import fr.larmoirecommune.app.ui.objects.ReservationListActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupMenuOptions()
    }

    private fun setupHeader() {
        binding.btnBack.setOnClickListener { finish() }

        // Setup User Info
        binding.profileName.text = "Jean Dupont" // Mock name or fetch from user
        binding.profileEmail.text = ApiClient.currentUserEmail ?: "Membre"
    }

    private fun setupMenuOptions() {
        // Mes Emprunts
        with(binding.btnMyReservations) {
            optionIcon.setImageResource(R.drawable.ic_reservations)
            optionTitle.text = "Mes emprunts"
            root.setOnClickListener {
                startActivity(Intent(this@ProfileActivity, ReservationListActivity::class.java))
            }
        }

        // Historique (Mock)
        with(binding.btnHistory) {
            optionIcon.setImageResource(R.drawable.ic_history)
            optionTitle.text = "Historique"
        }

        // Favoris (Mock)
        with(binding.btnFavorites) {
            optionIcon.setImageResource(R.drawable.ic_heart)
            optionTitle.text = "Favoris"
        }

        // FAQ (Mock)
        with(binding.btnFaq) {
            optionIcon.setImageResource(R.drawable.ic_help)
            optionTitle.text = "FAQ"
        }

        // Privacy (Mock)
        with(binding.btnPrivacy) {
            optionIcon.setImageResource(R.drawable.ic_admin)
            optionTitle.text = "Politique de confidentialité"
        }

        // Logout
        binding.logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        ApiClient.token = null
        ApiClient.currentUserIsAdmin = false
        ApiClient.currentUserEmail = null

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
