package fr.larmoirecommune.app.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.larmoirecommune.app.databinding.ActivityProfileBinding
import fr.larmoirecommune.app.network.ApiClient
import fr.larmoirecommune.app.ui.auth.LoginActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup data
        binding.profileName.text = "Utilisateur"
        binding.profileEmail.text = ApiClient.currentUserEmail ?: "Email non disponible"

        binding.logoutButton.setOnClickListener {
            // Clear token
            ApiClient.token = null
            ApiClient.currentUserIsAdmin = false
            ApiClient.currentUserEmail = null

            // Go to Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}