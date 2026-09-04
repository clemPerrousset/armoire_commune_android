package fr.larmoirecommune.app.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import fr.larmoirecommune.app.databinding.ActivityAdminCongesBinding
import fr.larmoirecommune.app.repository.AdminRepository
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Congés admin : sélection multiple de semaines (jeudi → mercredi 22h, comme les
 * réservations) à fermer pour tous les objets. "Valider" envoie l'ensemble complet
 * des semaines cochées ; désélectionner une semaine puis valider la retire.
 */
class AdminCongesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminCongesBinding
    private val repository = AdminRepository()
    private val chipRefs = mutableListOf<Pair<Chip, Calendar>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCongesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnValider.setOnClickListener { validerFermetures() }

        loadFermetures()
    }

    private fun loadFermetures() {
        lifecycleScope.launch {
            val fermetures = repository.getFermetures()
            val closedIsoDates = fermetures.map { it.dateDebut.substringBefore("T") }.toSet()
            buildChips(closedIsoDates)
        }
    }

    private fun buildChips(closedIsoDates: Set<String>) {
        binding.weekChipGroup.removeAllViews()
        chipRefs.clear()

        val cal = firstUpcomingThursday()
        repeat(20) {
            val thursday = cal.clone() as Calendar
            val chip = Chip(this).apply {
                text = formatChipDate(thursday)
                isCheckable = true
                isChecked = closedIsoDates.contains(isoDate(thursday))
            }
            binding.weekChipGroup.addView(chip)
            chipRefs.add(Pair(chip, thursday.clone() as Calendar))
            cal.add(Calendar.WEEK_OF_YEAR, 1)
        }
    }

    private fun validerFermetures() {
        val selected = chipRefs.filter { (chip, _) -> chip.isChecked }.map { (_, cal) ->
            String.format(
                "%04d-%02d-%02dT00:00:00",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
            )
        }
        binding.btnValider.isEnabled = false
        lifecycleScope.launch {
            val result = repository.setFermetures(selected)
            binding.btnValider.isEnabled = true
            if (result != null) {
                Toast.makeText(
                    this@AdminCongesActivity,
                    "Fermetures mises à jour (${result.size} semaine(s) fermée(s))",
                    Toast.LENGTH_SHORT
                ).show()
                loadFermetures()
            } else {
                Toast.makeText(this@AdminCongesActivity, "Erreur", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isoDate(cal: Calendar): String = String.format(
        "%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
    )

    private fun formatChipDate(cal: Calendar): String = String.format(
        "%02d/%02d/%04d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)
    )

    companion object {
        fun firstUpcomingThursday(): Calendar {
            val cal = Calendar.getInstance()
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return cal
        }
    }
}
