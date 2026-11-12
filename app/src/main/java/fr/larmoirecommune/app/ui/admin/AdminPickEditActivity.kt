package fr.larmoirecommune.app.ui.admin

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.larmoirecommune.app.model.Lieu
import fr.larmoirecommune.app.model.UpdateLieuRequest
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.repository.ObjectRepository
import kotlinx.coroutines.launch

/**
 * Liste des objets ou des lieux à modifier. Extra "TYPE" : "objet" (défaut) ou "lieu".
 * - objet : ouvre AdminCreateObjectActivity en mode édition (photo, description, consommables…)
 * - lieu : boîte de dialogue (nom, adresse, horaires)
 */
class AdminPickEditActivity : AppCompatActivity() {
    private val adminRepository = AdminRepository()
    private val objectRepository = ObjectRepository()
    private lateinit var container: LinearLayout
    private val isLieu get() = intent.getStringExtra("TYPE") == "lieu"

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(32), dp(16), dp(16))
        }
        setContentView(ScrollView(this).apply { addView(container) })
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            container.removeAllViews()
            addTitle(if (isLieu) "Modifier un lieu" else "Modifier un objet")
            if (isLieu) {
                val lieux = objectRepository.getLieux()
                if (lieux.isEmpty()) addTitle("Aucun lieu")
                lieux.forEach { lieu -> addRow(lieu.nom) { showLieuDialog(lieu) } }
            } else {
                val objets = objectRepository.getObjects()
                if (objets.isEmpty()) addTitle("Aucun objet")
                objets.forEach { objet ->
                    addRow(objet.nom) {
                        startActivity(Intent(this@AdminPickEditActivity, AdminCreateObjectActivity::class.java)
                            .putExtra("EDIT_OBJECT_ID", objet.id))
                    }
                }
            }
        }
    }

    private fun showLieuDialog(lieu: Lieu) {
        val nom = field("Nom", lieu.nom)
        val adresse = field("Adresse", lieu.adresse)
        val horaires = field("Horaires (une ligne par créneau)", lieu.description.orEmpty(), multiline = true)
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(nom); addView(adresse); addView(horaires)
        }
        AlertDialog.Builder(this)
            .setTitle("Modifier « ${lieu.nom} »")
            .setView(form)
            .setPositiveButton("Enregistrer") { _, _ ->
                val id = lieu.id ?: return@setPositiveButton
                lifecycleScope.launch {
                    val ok = adminRepository.updateLieu(
                        id,
                        UpdateLieuRequest(
                            nom = nom.text.toString().trim().ifBlank { lieu.nom },
                            adresse = adresse.text.toString().trim().ifBlank { lieu.adresse },
                            description = horaires.text.toString().trim()
                        )
                    )
                    Toast.makeText(this@AdminPickEditActivity, if (ok) "Lieu modifié" else "Erreur", Toast.LENGTH_SHORT).show()
                    if (ok) load()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun field(hintText: String, value: String, multiline: Boolean = false) = EditText(this).apply {
        hint = hintText
        setText(value)
        inputType = if (multiline) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        else InputType.TYPE_CLASS_TEXT
        if (multiline) minLines = 3
    }

    private fun addTitle(text: String) {
        container.addView(TextView(this).apply {
            this.text = text
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(12))
        })
    }

    private fun addRow(label: String, onEdit: () -> Unit) {
        container.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(context).apply {
                text = label
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(Button(context).apply {
                text = "Modifier"
                setOnClickListener { onEdit() }
            })
        })
    }
}
