package fr.larmoirecommune.app.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import fr.larmoirecommune.app.databinding.ActivityAdminAlertObjectsBinding
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.ui.objects.ObjectAdapter
import kotlinx.coroutines.launch

class AdminAlertObjectsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminAlertObjectsBinding
    private val adminRepository = AdminRepository()

    private val adapter = ObjectAdapter { objet ->
        AlertDialog.Builder(this)
            .setTitle("Retirer l'alerte ?")
            .setMessage("Voulez-vous retirer l'alerte sur l'objet ${objet.nom} ?")
            .setPositiveButton("Oui") { _, _ ->
                objet.id?.let { clearAlert(it) }
            }
            .setNegativeButton("Non", null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAlertObjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.objectRecycler.layoutManager = LinearLayoutManager(this)
        binding.objectRecycler.adapter = adapter

        loadAlertObjects()
    }

    private fun loadAlertObjects() {
        lifecycleScope.launch {
            val objets = adminRepository.getAlertObjects()
            adapter.submitList(objets)
        }
    }

    private fun clearAlert(id: Int) {
        lifecycleScope.launch {
            val success = adminRepository.clearAlert(id)
            if (success) {
                Toast.makeText(this@AdminAlertObjectsActivity, "Alerte retirée", Toast.LENGTH_SHORT).show()
                loadAlertObjects()
            } else {
                Toast.makeText(this@AdminAlertObjectsActivity, "Erreur", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
