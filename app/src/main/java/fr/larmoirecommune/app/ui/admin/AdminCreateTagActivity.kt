package fr.larmoirecommune.app.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.larmoirecommune.app.databinding.ActivityAdminCreateTagBinding
import fr.larmoirecommune.app.repository.AdminRepository
import kotlinx.coroutines.launch

class AdminCreateTagActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminCreateTagBinding
    private val repository = AdminRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCreateTagBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.validateButton.setOnClickListener {
            val nom = binding.tagName.text.toString().trim()
            if (nom.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un nom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val success = repository.createTag(nom)
                if (success) {
                    Toast.makeText(this@AdminCreateTagActivity, "Tag créé", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AdminCreateTagActivity, "Erreur", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
