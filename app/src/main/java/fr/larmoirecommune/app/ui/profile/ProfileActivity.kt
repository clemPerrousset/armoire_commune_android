package fr.larmoirecommune.app.ui.profile

import fr.larmoirecommune.app.model.Reservation
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import fr.larmoirecommune.app.R
import fr.larmoirecommune.app.databinding.ActivityProfileBinding
import fr.larmoirecommune.app.network.ApiClient
import fr.larmoirecommune.app.ui.profile.FavoriteListActivity
import fr.larmoirecommune.app.ui.profile.HistoryListActivity
import fr.larmoirecommune.app.ui.home.MainActivity
import fr.larmoirecommune.app.ui.objects.ReservationListActivity
import fr.larmoirecommune.app.utils.ProfileManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import coil.load
import coil.transform.CircleCropTransformation
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import androidx.core.content.FileProvider

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var profileManager: ProfileManager
    private var tempCameraUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            saveImageLocally(it)
        }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            tempCameraUri?.let {
                profileManager.avatarUri = it.toString()
                loadAvatar(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileManager = ProfileManager(this)

        setupHeader()
        setupMenuOptions()
        loadProfileData()
        setupEditListeners()
    }

    private fun loadProfileData() {
        binding.profileName.text = profileManager.userName ?: "Entrez votre nom"
        binding.profileEmail.text = ApiClient.currentUserEmail ?: "Membre"
        profileManager.avatarUri?.let {
            loadAvatar(Uri.parse(it))
        }
    }

    private fun loadAvatar(uri: Uri) {
        binding.avatar.load(uri) {
            crossfade(true)
            transformations(CircleCropTransformation())
            placeholder(R.drawable.ic_profile)
            error(R.drawable.ic_profile)
        }
        // Remove padding and tint when image is loaded
        binding.avatar.setPadding(0, 0, 0, 0)
        binding.avatar.imageTintList = null
    }

    private fun saveImageLocally(sourceUri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(sourceUri)
            val fileName = "profile_${UUID.randomUUID()}.jpg"
            val imagesDir = File(filesDir, "images").apply { if (!exists()) mkdirs() }
            val destFile = File(imagesDir, fileName)

            inputStream?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val internalUri = Uri.fromFile(destFile)
            profileManager.avatarUri = internalUri.toString()
            loadAvatar(internalUri)

        } catch (e: Exception) {
            e.printStackTrace()
            // Optional: Show toast or handle error
        }
    }

    private fun setupEditListeners() {
        binding.avatarContainer.setOnClickListener {
            showImageSourceDialog()
        }

        binding.nameContainer.setOnClickListener {
            showNameEditDialog()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Galerie", "Appareil photo")
        AlertDialog.Builder(this)
            .setTitle("Changer l'avatar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImage.launch("image/*")
                    1 -> startCamera()
                }
            }
            .show()
    }

    private fun startCamera() {
        val imagesDir = File(filesDir, "images").apply { if (!exists()) mkdirs() }
        val photoFile = File(imagesDir, "camera_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        tempCameraUri = uri
        takePhoto.launch(uri)
    }

    private fun showNameEditDialog() {
        val editText = EditText(this)
        editText.setText(profileManager.userName)
        val container = LinearLayout(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(48, 16, 48, 16)
        editText.layoutParams = params
        container.addView(editText)

        AlertDialog.Builder(this)
            .setTitle("Modifier votre nom")
            .setView(container)
            .setPositiveButton("Enregistrer") { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotBlank()) {
                    profileManager.userName = newName
                    binding.profileName.text = newName
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun setupHeader() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupMenuOptions() {
        // Mes Emprunts
        with(binding.btnMyReservations) {
            optionIcon.setImageResource(R.drawable.ic_reservations)
            optionTitle.text = "Mes emprunts"
            root.setOnClickListener {
                startActivity(Intent(this@ProfileActivity, ReservationListActivity::class.java))
            }
        }

        // Historique (Mock)
        with(binding.btnHistory) {
            optionIcon.setImageResource(R.drawable.ic_history)
            optionTitle.text = "Historique"
            root.setOnClickListener {
                startActivity(Intent(this@ProfileActivity, HistoryListActivity::class.java))
            }
        }

        // Favoris (Mock)
        with(binding.btnFavorites) {
            optionIcon.setImageResource(R.drawable.ic_heart)
            optionTitle.text = "Favoris"
            root.setOnClickListener {
                startActivity(Intent(this@ProfileActivity, FavoriteListActivity::class.java))
            }
        }

        // FAQ (Mock)
        with(binding.btnFaq) {
            optionIcon.setImageResource(R.drawable.ic_help)
            optionTitle.text = "FAQ"
        }

        // Privacy (Mock)
        with(binding.btnPrivacy) {
            optionIcon.setImageResource(R.drawable.ic_admin)
            optionTitle.text = "Politique de confidentialité"
        }

        // Logout
        binding.logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        ApiClient.token = null
        ApiClient.currentUserIsAdmin = false
        ApiClient.currentUserEmail = null

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
