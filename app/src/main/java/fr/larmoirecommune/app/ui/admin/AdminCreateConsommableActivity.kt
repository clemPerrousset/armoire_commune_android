package fr.larmoirecommune.app.ui.admin

import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import fr.larmoirecommune.app.model.CreateConsommableRequest
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.ui.common.PhotoPicker
import kotlinx.coroutines.launch

/** Création d'un consommable : photo, intitulé, description, prix et lien d'achat (Yapla pour l'instant). */
class AdminCreateConsommableActivity : AppCompatActivity() {
    private val repository = AdminRepository()
    private var imageUri: Uri? = null
    private lateinit var preview: ImageView
    private lateinit var btnPhoto: Button
    private lateinit var btnCreate: Button

    private val photoPicker = PhotoPicker(this) { uri ->
        imageUri = uri
        preview.visibility = View.VISIBLE
        preview.load(uri) { crossfade(true) }
        btnPhoto.text = "Changer la photo"
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun edit(hintText: String, type: Int, multiline: Boolean = false) = EditText(this).apply {
        hint = hintText
        inputType = type or (if (multiline) InputType.TYPE_TEXT_FLAG_MULTI_LINE else 0)
        if (multiline) minLines = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nom = edit("Intitulé (ex. Détergent Karcher)", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        val description = edit("Description", InputType.TYPE_CLASS_TEXT, multiline = true)
        val prix = edit("Prix en € (ex. 12.50)", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val url = edit("Lien d'achat (Yapla)", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)

        preview = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(0xFFEEEEEE.toInt())
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(220))
                .apply { topMargin = dp(8) }
        }
        btnPhoto = Button(this).apply {
            text = "Ajouter une photo"
            setOnClickListener { photoPicker.show() }
        }
        btnCreate = Button(this).apply { text = "Créer le consommable" }

        btnCreate.setOnClickListener {
            val name = nom.text.toString().trim()
            val price = prix.text.toString().replace(',', '.').toDoubleOrNull()
            if (name.isBlank() || price == null) {
                Toast.makeText(this, "Intitulé et prix sont obligatoires", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val link = url.text.toString().trim()
            if (link.isNotEmpty() && !link.startsWith("http://") && !link.startsWith("https://")) {
                Toast.makeText(this, "Le lien doit commencer par http:// ou https://", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnCreate.isEnabled = false
            lifecycleScope.launch {
                val created = repository.createConsommable(
                    CreateConsommableRequest(
                        nom = name,
                        description = description.text.toString().trim().ifBlank { null },
                        prix = price,
                        url = link.ifBlank { null }
                    )
                )
                val id = created?.id
                if (id == null) {
                    btnCreate.isEnabled = true
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

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(32), dp(16), dp(16))
            addView(TextView(context).apply {
                text = "Créer un consommable"
                textSize = 18f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dp(12))
            })
            addView(nom); addView(description); addView(prix); addView(url)
            addView(preview); addView(btnPhoto); addView(btnCreate)
        }
        setContentView(ScrollView(this).apply { addView(form) })
    }
}
