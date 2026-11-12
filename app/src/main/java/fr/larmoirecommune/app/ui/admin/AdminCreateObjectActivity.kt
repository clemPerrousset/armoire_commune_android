package fr.larmoirecommune.app.ui.admin

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import fr.larmoirecommune.app.R
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

        binding.btnPickImage.setOnClickListener { photoPicker.show() }
        binding.createButton.setOnClickListener { save() }

        lifecycleScope.launch {
            availableTags = objectRepository.getTags()
            availableConsommables = repository.getConsommables()
            renderChips()
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
        renderChips()
        if (!obj.image.isNullOrBlank()) {
            binding.imagePreview.visibility = View.VISIBLE
            binding.imagePreview.load(ApiClient.getUrl(obj.image)) { crossfade(true) }
        }
    }

    // --- Sélections multiples ---

    /** (Re)construit les puces des catégories et des consommables selon la sélection courante. */
    private fun renderChips() {
        fillChips(
            binding.tagChips,
            availableTags.mapNotNull { t -> t.id?.let { it to t.nom } },
            selectedTagIds
        )
        fillChips(
            binding.consommableChips,
            availableConsommables.mapNotNull { c -> c.id?.let { it to "${c.nom} · ${"%.2f".format(c.prix)} €" } },
            selectedConsommableIds
        )
        binding.consommablesEmpty.visibility =
            if (availableConsommables.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun fillChips(group: ChipGroup, items: List<Pair<Int, String>>, selection: MutableSet<Int>) {
        group.removeAllViews()
        items.forEach { (id, label) ->
            val chip = layoutInflater.inflate(R.layout.item_filter_chip, group, false) as Chip
            chip.text = label
            chip.isChecked = id in selection
            chip.setOnCheckedChangeListener { _, checked -> if (checked) selection.add(id) else selection.remove(id) }
            group.addView(chip)
        }
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
