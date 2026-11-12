package fr.larmoirecommune.app.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.larmoirecommune.app.databinding.ActivityAdminPickEditBinding
import fr.larmoirecommune.app.databinding.DialogEditLieuBinding
import fr.larmoirecommune.app.databinding.ItemAdminPickBinding
import fr.larmoirecommune.app.model.Consommable
import fr.larmoirecommune.app.model.Lieu
import fr.larmoirecommune.app.model.UpdateLieuRequest
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.repository.ObjectRepository
import kotlinx.coroutines.launch

/**
 * Liste des objets ou des lieux à modifier, avec recherche. Extra "TYPE" : "objet" (défaut) ou "lieu".
 * - objet : ouvre AdminCreateObjectActivity en mode édition (photo, description, consommables…)
 * - lieu : boîte de dialogue (nom, adresse, horaires)
 */
class AdminPickEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminPickEditBinding
    private val adminRepository = AdminRepository()
    private val objectRepository = ObjectRepository()
    private val type get() = intent.getStringExtra("TYPE") ?: "objet"
    private val isLieu get() = type == "lieu"

    private data class Entry(val title: String, val subtitle: String?, val onClick: () -> Unit)

    private var entries: List<Entry> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPickEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.header.screenTitle.text = when (type) {
            "lieu" -> "Modifier un lieu"
            "consommable" -> "Modifier un consommable"
            "consommable_suppr" -> "Supprimer un consommable"
            else -> "Modifier un objet"
        }
        binding.header.btnBack.setOnClickListener { finish() }
        binding.searchLayout.hint = when (type) {
            "lieu" -> "Rechercher un lieu"
            "consommable", "consommable_suppr" -> "Rechercher un consommable"
            else -> "Rechercher un objet"
        }
        binding.searchField.doAfterTextChanged { render() }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            entries = if (isLieu) {
                objectRepository.getLieux().map { lieu ->
                    Entry(lieu.nom, lieu.adresse.takeIf { it.isNotBlank() }) { showLieuDialog(lieu) }
                }
            } else if (type == "consommable" || type == "consommable_suppr") {
                adminRepository.getConsommables().map { c ->
                    Entry(c.nom, "%.2f €".format(c.prix)) {
                        if (type == "consommable") {
                            startActivity(
                                Intent(this@AdminPickEditActivity, AdminCreateConsommableActivity::class.java)
                                    .putExtra("EDIT_CONSO_ID", c.id)
                            )
                        } else {
                            confirmDeleteConsommable(c)
                        }
                    }
                }
            } else {
                objectRepository.getObjects().map { objet ->
                    Entry(objet.nom, objet.allTags.joinToString(", ") { it.nom }.ifBlank { null }) {
                        startActivity(
                            Intent(this@AdminPickEditActivity, AdminCreateObjectActivity::class.java)
                                .putExtra("EDIT_OBJECT_ID", objet.id)
                        )
                    }
                }
            }
            render()
        }
    }

    private fun render() {
        val query = binding.searchField.text.toString().trim()
        val shown = entries.filter { it.title.contains(query, ignoreCase = true) }
        binding.pickList.removeAllViews()
        shown.forEach { entry ->
            val row = ItemAdminPickBinding.inflate(layoutInflater, binding.pickList, false)
            row.pickTitle.text = entry.title
            row.pickSubtitle.text = entry.subtitle
            row.pickSubtitle.visibility = if (entry.subtitle == null) View.GONE else View.VISIBLE
            row.root.setOnClickListener { entry.onClick() }
            binding.pickList.addView(row.root)
        }
        binding.pickEmpty.visibility = if (shown.isEmpty()) View.VISIBLE else View.GONE
        binding.pickEmpty.text = when {
            entries.isEmpty() -> when (type) {
                "lieu" -> "Aucun lieu"
                "consommable", "consommable_suppr" -> "Aucun consommable"
                else -> "Aucun objet"
            }
            else -> "Aucun résultat pour « $query »"
        }
    }

    private fun confirmDeleteConsommable(conso: Consommable) {
        val id = conso.id ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle("Supprimer « ${conso.nom} » ?")
            .setMessage("Il sera retiré de tous les objets auxquels il est associé. Les objets sont conservés.\nCette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ ->
                lifecycleScope.launch {
                    val ok = adminRepository.deleteConsommable(id)
                    Toast.makeText(
                        this@AdminPickEditActivity,
                        if (ok) "Consommable supprimé" else "Suppression impossible",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (ok) load()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showLieuDialog(lieu: Lieu) {
        val form = DialogEditLieuBinding.inflate(layoutInflater)
        form.lieuNom.setText(lieu.nom)
        form.lieuAdresse.setText(lieu.adresse)
        form.lieuHoraires.setText(lieu.description.orEmpty())
        MaterialAlertDialogBuilder(this)
            .setTitle("Modifier « ${lieu.nom} »")
            .setView(form.root)
            .setPositiveButton("Enregistrer") { _, _ ->
                val id = lieu.id ?: return@setPositiveButton
                lifecycleScope.launch {
                    val ok = adminRepository.updateLieu(
                        id,
                        UpdateLieuRequest(
                            nom = form.lieuNom.text.toString().trim().ifBlank { lieu.nom },
                            adresse = form.lieuAdresse.text.toString().trim().ifBlank { lieu.adresse },
                            description = form.lieuHoraires.text.toString().trim()
                        )
                    )
                    Toast.makeText(this@AdminPickEditActivity, if (ok) "Lieu modifié" else "Erreur", Toast.LENGTH_SHORT).show()
                    if (ok) load()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}
