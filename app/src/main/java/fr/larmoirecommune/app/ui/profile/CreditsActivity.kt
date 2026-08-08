package fr.larmoirecommune.app.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.larmoirecommune.app.databinding.ActivityCreditsBinding
import fr.larmoirecommune.app.network.ApiClient

class CreditsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreditsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreditsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener { finish() }
        binding.tvCreditsRemaining.text = "${ApiClient.currentUserCredits}"
    }
}
