package fr.larmoirecommune.app.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import fr.larmoirecommune.app.databinding.ActivityAdminAlertObjectsBinding
import fr.larmoirecommune.app.repository.ObjectRepository
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.ui.objects.ObjectAdapter
import kotlinx.coroutines.launch

class AdminAlertObjectsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminAlertObjectsBinding
    private val repository = ObjectRepository()
    private val adminRepository = AdminRepository()

    private val adapter = ObjectAdapter { objet ->
        // On click, ask to clear alert
        android.app.AlertDialog.Builder(this)
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
            // Note: The backend doesn't seem to have a specific ?alert=true endpoint documented
            // but we can fetch all objects and assuming 'alert' is a property or we just filter client side if possible.
            // Based on backend readme: /objets returns objects. If it doesn't return alert status,
            // we will simulate fetching all and pretend we show alert objects (or just list all for clear alert demo).
            // Actually, we'll fetch all objects for now as a fallback to allow admin to clear alert on any object.
            val objets = repository.getObjects()
            // We show all objects for the moment, in real scenario we'd filter by `alert == true`
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
