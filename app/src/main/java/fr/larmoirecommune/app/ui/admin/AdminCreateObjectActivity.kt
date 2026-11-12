package fr.larmoirecommune.app.ui.admin

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import fr.larmoirecommune.app.databinding.ActivityAdminCreateObjectBinding
import fr.larmoirecommune.app.model.Consommable
import fr.larmoirecommune.app.model.Tag
import fr.larmoirecommune.app.model.UpdateObjectRequest
import fr.larmoirecommune.app.network.ApiClient
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.repository.ObjectRepository
import fr.larmoirecommune.app.ui.common.PhotoPicker
import kotlinx.coroutines.launch

/**
 * Création d'un objet — ou, avec l'extra EDIT_OBJECT_ID, modification d'un objet existant
 * (photo, description, catégories, consommables, stockage).
 */
class AdminCreateObjectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminCreateObjectBinding
    private val repository = AdminRepository()
    private val objectRepository = ObjectRepository()

    private var availableTags: List<Tag> = emptyList()
    private val selectedTagIds = linkedSetOf<Int>()
    private var availableConsommables: List<Consommable> = emptyList()
    private val selectedConsommableIds = linkedSetOf<Int>()

    private var selectedImageUri: Uri? = null
    private var editId: Int? = null

    private val photoPicker = PhotoPicker(this) { setSelectedImage(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCreateObjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editId = intent.getIntExtra("EDIT_OBJECT_ID", -1).takeIf { it != -1 }
        if (editId != null) {
            binding.screenTitle.text = "Modifier un objet"
            binding.createButton.text = "Enregistrer"
            binding.btnPickImage.text = "Changer la photo"
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.objectTagId.setOnClickListener { showTagPicker() }
        binding.objectTagId.isFocusable = false
        binding.objectConsommables.setOnClickListener { showConsommablePicker() }
        binding.objectConsommables.isFocusable = false

        binding.btnPickImage.setOnClickListener { photoPicker.show() }
        binding.createButton.setOnClickListener { save() }

        lifecycleScope.launch {
            availableTags = objectRepository.getTags()
            availableConsommables = repository.getConsommables()
            editId?.let { loadExisting(it) }
        }
    }

    private suspend fun loadExisting(id: Int) {
        val obj = repository.getObjetAdmin(id)
        if (obj == null) {
            Toast.makeText(this, "Objet introuvable", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.objectName.setText(obj.nom)
        binding.objectDesc.setText(obj.description)
        binding.objectStockage.setText(obj.stockage.orEmpty())
        selectedTagIds.clear()
        selectedTagIds.addAll(obj.allTags.mapNotNull { it.id })
        selectedConsommableIds.clear()
        selectedConsommableIds.addAll(obj.consommables.mapNotNull { it.id })
        refreshPickerTexts()
        if (!obj.image.isNullOrBlank()) {
            binding.imagePreview.visibility = View.VISIBLE
            binding.imagePreview.load(ApiClient.getUrl(obj.image)) { crossfade(true) }
        }
    }

    // --- Sélections multiples ---

    private fun refreshPickerTexts() {
        binding.objectTagId.setText(availableTags.filter { it.id in selectedTagIds }.joinToString(", ") { it.nom })
        binding.objectConsommables.setText(
            availableConsommables.filter { it.id in selectedConsommableIds }.joinToString(", ") { it.nom }
        )
    }

    private fun showTagPicker() {
        if (availableTags.isEmpty()) {
            Toast.makeText(this, "Aucune catégorie disponible", Toast.LENGTH_SHORT).show()
            return
        }
        multiPick("Choisir les catégories", availableTags.map { it.nom }, availableTags.map { it.id }, selectedTagIds)
    }

    private fun showConsommablePicker() {
        if (availableConsommables.isEmpty()) {
            Toast.makeText(this, "Aucun consommable : créez-en un depuis le menu admin", Toast.LENGTH_LONG).show()
            return
        }
        multiPick(
            "Consommables associés",
            availableConsommables.map { "${it.nom} (${"%.2f".format(it.prix)} €)" },
            availableConsommables.map { it.id },
            selectedConsommableIds
        )
    }

    private fun multiPick(title: String, labels: List<String>, ids: List<Int?>, selection: MutableSet<Int>) {
        val checked = BooleanArray(labels.size) { ids[it] in selection }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMultiChoiceItems(labels.toTypedArray(), checked) { _, which, isChecked -> checked[which] = isChecked }
            .setPositiveButton("OK") { _, _ ->
                selection.clear()
                checked.forEachIndexed { i, c -> if (c) ids[i]?.let { selection.add(it) } }
                refreshPickerTexts()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    // --- Image sélectionnée (déjà recadrée par PhotoPicker) ---

    private fun setSelectedImage(uri: Uri) {
        selectedImageUri = uri
        binding.imagePreview.visibility = View.VISIBLE
        binding.imagePreview.load(uri) { crossfade(true) }
        binding.btnPickImage.text = "Changer la photo"
    }

    // --- Enregistrement ---

    private fun save() {
        val nom = binding.objectName.text.toString().trim()
        val desc = binding.objectDesc.text.toString().trim()
        val stockage = binding.objectStockage.text.toString().trim()

        if (nom.isBlank()) {
            Toast.makeText(this, "Le nom est obligatoire", Toast.LENGTH_SHORT).show()
            return
        }

        binding.createButton.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val existingId = editId
            val objetId: Int? = if (existingId == null) {
                repository.createObject(
                    nom, desc,
                    tagIds = selectedTagIds.toList(),
                    consommableIds = selectedConsommableIds.toList(),
                    stockage = stockage.ifBlank { null }
                )?.id
            } else {
                repository.updateObject(
                    existingId,
                    UpdateObjectRequest(nom, desc, selectedTagIds.toList(), selectedConsommableIds.toList(), stockage)
                )?.id
            }

            if (objetId == null) {
                binding.createButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@AdminCreateObjectActivity, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val imageUri = selectedImageUri
            if (imageUri != null) {
                val bytes = contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                val mime = contentResolver.getType(imageUri) ?: "image/jpeg"
                if (bytes != null && !repository.uploadObjetImage(objetId, bytes, mime)) {
                    Toast.makeText(this@AdminCreateObjectActivity, "Objet enregistré, mais la photo n'a pas pu être envoyée", Toast.LENGTH_LONG).show()
                }
            }

            binding.progressBar.visibility = View.GONE
            Toast.makeText(
                this@AdminCreateObjectActivity,
                if (existingId == null) "Objet créé" else "Objet modifié",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
