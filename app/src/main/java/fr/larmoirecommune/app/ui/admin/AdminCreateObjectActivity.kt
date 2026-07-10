package fr.larmoirecommune.app.ui.admin

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import fr.larmoirecommune.app.databinding.ActivityAdminCreateObjectBinding
import fr.larmoirecommune.app.model.Tag
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.repository.ObjectRepository
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class AdminCreateObjectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminCreateObjectBinding
    private val repository = AdminRepository()
    private val objectRepository = ObjectRepository()
    private var availableTags: List<Tag> = emptyList()
    private var selectedTagId: Int? = null

    // Image sélectionnée (URI local)
    private var selectedImageUri: Uri? = null
    private var tempCameraUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { setSelectedImage(it) }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) tempCameraUri?.let { setSelectedImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCreateObjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // Chargement des tags
        lifecycleScope.launch {
            availableTags = objectRepository.getTags()
        }

        binding.objectTagId.setOnClickListener { showTagPicker() }
        binding.objectTagId.isFocusable = false

        // Bouton photo
        binding.btnPickImage.setOnClickListener { showImageSourceDialog() }

        binding.createButton.setOnClickListener { createObject() }
    }

    private fun showTagPicker() {
        if (availableTags.isEmpty()) {
            Toast.makeText(this, "Aucun tag disponible", Toast.LENGTH_SHORT).show()
            return
        }
        val tagNames = availableTags.map { it.nom }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Choisir une catégorie")
            .setItems(tagNames) { _, idx ->
                selectedTagId = availableTags[idx].id
                binding.objectTagId.setText(availableTags[idx].nom)
            }
            .setNeutralButton("Aucune catégorie") { _, _ ->
                selectedTagId = null
                binding.objectTagId.setText("")
            }
            .show()
    }

    private fun showImageSourceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Ajouter une photo")
            .setItems(arrayOf("Galerie", "Appareil photo")) { _, which ->
                when (which) {
                    0 -> pickImage.launch("image/*")
                    1 -> startCamera()
                }
            }
            .show()
    }

    private fun startCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 42)
            return
        }
        launchCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 42 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        }
    }

    private fun launchCamera() {
        val imagesDir = File(filesDir, "images").apply { if (!exists()) mkdirs() }
        val photoFile = File(imagesDir, "camera_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        tempCameraUri = uri
        takePhoto.launch(uri)
    }

    private fun setSelectedImage(uri: Uri) {
        selectedImageUri = uri
        binding.imagePreview.visibility = View.VISIBLE
        binding.imagePreview.load(uri) {
            crossfade(true)
        }
        binding.btnPickImage.text = "Changer la photo"
    }

    private fun createObject() {
        val nom = binding.objectName.text.toString().trim()
        val desc = binding.objectDesc.text.toString().trim()

        if (nom.isBlank()) {
            Toast.makeText(this, "Le nom est obligatoire", Toast.LENGTH_SHORT).show()
            return
        }

        binding.createButton.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val objet = repository.createObject(
                nom = nom,
                description = desc,
                tagId = selectedTagId,
                consommableIds = emptyList()
            )

            if (objet == null) {
                binding.createButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@AdminCreateObjectActivity, "Erreur lors de la création", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Upload de l'image si sélectionnée
            val imageUri = selectedImageUri
            if (imageUri != null && objet.id != null) {
                val bytes = readImageBytes(imageUri)
                val mime = contentResolver.getType(imageUri) ?: "image/jpeg"
                if (bytes != null) {
                    repository.uploadObjetImage(objet.id, bytes, mime)
                }
            }

            binding.progressBar.visibility = View.GONE
            Toast.makeText(this@AdminCreateObjectActivity, "Objet créé", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun readImageBytes(uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
