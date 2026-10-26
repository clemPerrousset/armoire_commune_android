package fr.larmoirecommune.app.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.repository.ObjectRepository
import kotlinx.coroutines.launch

/** Suppression des catégories (tags) et des lieux, avec avertissement avant chaque suppression. */
class AdminDeleteMetaActivity : AppCompatActivity() {
    private val adminRepository = AdminRepository()
    private val objectRepository = ObjectRepository()
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (16 * resources.displayMetrics.density).toInt()
        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        setContentView(ScrollView(this).apply { addView(container) })
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            val tags = objectRepository.getTags()
            val lieux = objectRepository.getLieux()
            container.removeAllViews()
            addTitle("Attention : une suppression est définitive.")
            addTitle("Catégories")
            tags.forEach { tag ->
                addRow(tag.nom) { tag.id?.let { id -> confirm("la catégorie « ${tag.nom} »", "Les objets de cette catégorie seront conservés, sans catégorie.") { deleteTag(id) } } }
            }
            addTitle("Lieux")
            lieux.forEach { lieu ->
                addRow(lieu.nom) { lieu.id?.let { id -> confirm("le lieu « ${lieu.nom} »", "Impossible s'il a des réservations en cours.") { deleteLieu(id) } } }
            }
        }
    }

    private fun confirm(what: String, warning: String, action: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer $what ?")
            .setMessage("$warning\nCette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ -> action() }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun deleteTag(id: Int) = lifecycleScope.launch {
        report(adminRepository.deleteTag(id), "Catégorie supprimée")
    }

    private fun deleteLieu(id: Int) = lifecycleScope.launch {
        report(adminRepository.deleteLieu(id), "Lieu supprimé")
    }

    private fun report(success: Boolean, okMsg: String) {
        Toast.makeText(this, if (success) okMsg else "Suppression impossible (réservations en cours ?)", Toast.LENGTH_LONG).show()
        if (success) load()
    }

    private fun addTitle(text: String) {
        container.addView(TextView(this).apply {
            this.text = text
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 24, 0, 8)
        })
    }

    private fun addRow(label: String, onDelete: () -> Unit) {
        container.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(context).apply {
                text = label
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(Button(context).apply {
                text = "Supprimer"
                setTextColor(Color.parseColor("#EF4444"))
                setOnClickListener { onDelete() }
            })
        })
    }
}
