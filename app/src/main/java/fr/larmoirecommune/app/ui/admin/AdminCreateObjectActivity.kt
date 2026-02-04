package fr.larmoirecommune.app.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.larmoirecommune.app.databinding.ActivityAdminCreateObjectBinding
import fr.larmoirecommune.app.repository.AdminRepository
import kotlinx.coroutines.launch

class AdminCreateObjectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminCreateObjectBinding
    private val repository = AdminRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCreateObjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.createButton.setOnClickListener {
            val nom = binding.objectName.text.toString()
            val desc = binding.objectDesc.text.toString()
            val qty = binding.objectQty.text.toString().toIntOrNull() ?: 1
            val tagId = binding.objectTagId.text.toString().toIntOrNull() ?: 1

            lifecycleScope.launch {
                val success = repository.createObject(nom, desc, qty, tagId, emptyList())
                if (success) {
                    Toast.makeText(this@AdminCreateObjectActivity, "Objet créé", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AdminCreateObjectActivity, "Erreur", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
