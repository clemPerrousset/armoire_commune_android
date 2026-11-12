package fr.larmoirecommune.app.ui.admin

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import fr.larmoirecommune.app.databinding.ActivityAdminCreateConsommableBinding
import fr.larmoirecommune.app.model.CreateConsommableRequest
import fr.larmoirecommune.app.model.UpdateConsommableRequest
import fr.larmoirecommune.app.network.ApiClient
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.ui.common.PhotoPicker
import kotlinx.coroutines.launch

/** Création d'un consommable : photo, intitulé, description, prix et lien d'achat (Yapla pour l'instant). */
class AdminCreateConsommableActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminCreateConsommableBinding
    private val repository = AdminRepository()
    private var imageUri: Uri? = null
    private var editId: Int? = null

    private val photoPicker = PhotoPicker(this) { uri ->
        imageUri = uri
        binding.imagePreview.visibility = View.VISIBLE
        binding.imagePreview.load(uri) { crossfade(true) }
        binding.btnPickImage.text = "Changer la photo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCreateConsommableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.header.screenTitle.text = "Créer un consommable"
        binding.header.btnBack.setOnClickListener { finish() }
        binding.btnPickImage.setOnClickListener { photoPicker.show() }
        binding.createButton.setOnClickListener { save() }

        editId = intent.getIntExtra("EDIT_CONSO_ID", -1).takeIf { it != -1 }
        editId?.let { id ->
            binding.header.screenTitle.text = "Modifier un consommable"
            binding.createButton.text = "Enregistrer"
            binding.btnPickImage.text = "Changer la photo"
            loadExisting(id)
        }
    }

    private fun loadExisting(id: Int) {
        lifecycleScope.launch {
            val conso = repository.getConsommables().firstOrNull { it.id == id }
            if (conso == null) {
                Toast.makeText(this@AdminCreateConsommableActivity, "Consommable introuvable", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            binding.consoNom.setText(conso.nom)
            binding.consoDesc.setText(conso.description.orEmpty())
            binding.consoPrix.setText(conso.prix.toString())
            binding.consoUrl.setText(conso.url.orEmpty())
            if (!conso.image.isNullOrBlank()) {
                binding.imagePreview.visibility = View.VISIBLE
                binding.imagePreview.load(ApiClient.getUrl(conso.image)) { crossfade(true) }
            }
        }
    }

    private fun updateExisting(id: Int, name: String, price: Double, link: String) {
        lifecycleScope.launch {
            val ok = repository.updateConsommable(
                id,
                UpdateConsommableRequest(
                    nom = name,
                    description = binding.consoDesc.text.toString().trim(),
                    prix = price,
                    url = link
                )
            )
            if (!ok) {
                binding.createButton.isEnabled = true
                Toast.makeText(this@AdminCreateConsommableActivity, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show()
                return@launch
            }
            imageUri?.let { uri ->
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && !repository.uploadConsommableImage(id, bytes, contentResolver.getType(uri) ?: "image/jpeg")) {
                    Toast.makeText(this@AdminCreateConsommableActivity, "Enregistré, mais la photo n'a pas pu être envoyée", Toast.LENGTH_LONG).show()
                }
            }
            Toast.makeText(this@AdminCreateConsommableActivity, "Consommable modifié", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun save() {
        val name = binding.consoNom.text.toString().trim()
        val price = binding.consoPrix.text.toString().replace(',', '.').toDoubleOrNull()
        if (name.isBlank() || price == null) {
            Toast.makeText(this, "Intitulé et prix sont obligatoires", Toast.LENGTH_SHORT).show()
            return
        }
        val link = binding.consoUrl.text.toString().trim()
        if (link.isNotEmpty() && !link.startsWith("http://") && !link.startsWith("https://")) {
            Toast.makeText(this, "Le lien doit commencer par http:// ou https://", Toast.LENGTH_SHORT).show()
            return
        }
        binding.createButton.isEnabled = false
        val existingId = editId
        if (existingId != null) {
            updateExisting(existingId, name, price, link)
            return
        }
        lifecycleScope.launch {
            val created = repository.createConsommable(
                CreateConsommableRequest(
                    nom = name,
                    description = binding.consoDesc.text.toString().trim().ifBlank { null },
                    prix = price,
                    url = link.ifBlank { null }
                )
            )
            val id = created?.id
            if (id == null) {
                binding.createButton.isEnabled = true
                Toast.makeText(this@AdminCreateConsommableActivity, "Erreur lors de la création", Toast.LENGTH_SHORT).show()
                return@launch
            }
            imageUri?.let { uri ->
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) repository.uploadConsommableImage(id, bytes, contentResolver.getType(uri) ?: "image/jpeg")
            }
            Toast.makeText(this@AdminCreateConsommableActivity, "Consommable créé", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
