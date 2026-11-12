package fr.larmoirecommune.app.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import fr.larmoirecommune.app.R
import fr.larmoirecommune.app.databinding.ActivityAdminStatsBinding
import fr.larmoirecommune.app.databinding.ItemKpiBinding
import fr.larmoirecommune.app.databinding.ItemStatRowBinding
import fr.larmoirecommune.app.model.NomCount
import fr.larmoirecommune.app.model.StatsResponse
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.ui.common.BarChartView
import kotlinx.coroutines.launch

/**
 * Vue statistiques (admin) : filtre semaine / mois / année, chiffres clés, graphique
 * dans le temps, classements, et export PDF envoyé par e-mail par le serveur.
 */
class AdminStatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminStatsBinding
    private val repository = AdminRepository()
    private var periode = "mois"

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.header.screenTitle.text = "Statistiques"
        binding.header.btnBack.setOnClickListener { finish() }

        binding.periodToggle.check(R.id.btnMois)
        binding.periodToggle.addOnButtonCheckedListener { _, id, checked ->
            if (!checked) return@addOnButtonCheckedListener
            periode = when (id) {
                R.id.btnSemaine -> "semaine"
                R.id.btnAnnee -> "annee"
                else -> "mois"
            }
            load()
        }
        binding.btnExport.setOnClickListener { confirmExport() }
        load()
    }

    private fun load() {
        binding.progressBar.visibility = View.VISIBLE
        binding.content.visibility = View.GONE
        lifecycleScope.launch {
            val stats = repository.getStats(periode)
            binding.progressBar.visibility = View.GONE
            if (stats == null) {
                Toast.makeText(this@AdminStatsActivity, "Impossible de charger les statistiques", Toast.LENGTH_LONG).show()
                return@launch
            }
            render(stats)
            binding.content.visibility = View.VISIBLE
        }
    }

    private fun render(s: StatsResponse) {
        binding.periodLabel.text = "${s.periodeLabel} · du ${frDate(s.debut)} au ${frDate(s.fin)}"
        val a = s.activite
        val n = s.snapshot

        fillKpis(
            binding.kpiActivity, listOf(
                Kpi(a.reservations.toString(), "Objets réservés"),
                Kpi(a.objetsRendus.toString(), "Objets rendus"),
                Kpi(a.semainesReservees.toString(), "Semaines réservées"),
                Kpi(a.creditsConsommes.toString(), "Crédits consommés"),
                Kpi("${a.annulations} (${"%.0f".format(a.tauxAnnulation)} %)", "Annulations"),
                Kpi(a.emprunteursActifs.toString(), "Emprunteurs actifs"),
                Kpi(a.objetsDistinctsReserves.toString(), "Objets différents réservés"),
                Kpi(a.nouveauxInscrits.toString(), "Nouveaux inscrits"),
            )
        )
        fillKpis(
            binding.kpiSnapshot, listOf(
                Kpi(n.utilisateursInscrits.toString(), "Utilisateurs inscrits"),
                Kpi(n.pointsRelais.toString(), "Points relais"),
                Kpi(n.objetsTotal.toString(), "Objets au catalogue"),
                Kpi(n.objetsEmpruntes.toString(), "Empruntés en ce moment"),
                Kpi(n.objetsEnVerification.toString(), "En vérification (révision)"),
                Kpi(n.objetsEnMaintenance.toString(), "En maintenance"),
                Kpi(n.objetsEnRetard.toString(), "En retard", alert = n.objetsEnRetard > 0),
                Kpi(n.lieux.toString(), "Lieux de retrait"),
            )
        )

        binding.chart.setData(
            s.series.labels,
            listOf(
                BarChartView.Series("Réservations", getColor(R.color.mint_primary), s.series.reservations),
                BarChartView.Series("Retours", getColor(R.color.status_reserved), s.series.retours),
            )
        )

        fillRows(binding.statusList, n.reservationsParStatut.map { it.label to it.nombre })
        fillList(binding.titleTop, binding.cardTop, binding.topList, a.topObjets)
        fillList(binding.titleCategories, binding.cardCategories, binding.categoriesList, a.parCategorie)
        fillList(binding.titleLieux, binding.cardLieux, binding.lieuxList, a.parLieu)

        binding.footnote.text = "Généré le ${s.genereLe}. « Réservations » est compté à la date de début du prêt, " +
            "« retours » à la date de fin prévue des prêts restitués. " +
            "Les nouveaux inscrits ne comptent que les comptes créés depuis l'ajout de cette statistique."
    }

    private data class Kpi(val value: String, val label: String, val alert: Boolean = false)

    private fun fillKpis(container: LinearLayout, kpis: List<Kpi>) {
        container.removeAllViews()
        kpis.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
            }
            pair.forEachIndexed { i, k ->
                val card = ItemKpiBinding.inflate(layoutInflater, row, false)
                card.kpiValue.text = k.value
                card.kpiLabel.text = k.label
                if (k.alert) card.kpiValue.setTextColor(getColor(R.color.status_unavailable))
                (card.root.layoutParams as LinearLayout.LayoutParams).apply {
                    if (i == 0) marginEnd = dp(4) else marginStart = dp(4)
                }
                row.addView(card.root)
            }
            if (pair.size == 1) row.addView(View(this), LinearLayout.LayoutParams(0, 0, 1f)) // garde la largeur
            container.addView(row)
        }
    }

    private fun fillRows(container: LinearLayout, rows: List<Pair<String, Int>>) {
        container.removeAllViews()
        rows.forEach { (label, value) ->
            val row = ItemStatRowBinding.inflate(layoutInflater, container, false)
            row.rowLabel.text = label
            row.rowValue.text = value.toString()
            container.addView(row.root)
        }
    }

    private fun fillList(title: TextView, card: MaterialCardView, container: LinearLayout, items: List<NomCount>) {
        val visible = if (items.isEmpty()) View.GONE else View.VISIBLE
        title.visibility = visible
        card.visibility = visible
        fillRows(container, items.map { it.nom to it.reservations })
    }

    // --- Export PDF par e-mail ---

    private fun confirmExport() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Envoyer le rapport ?")
            .setMessage("Le rapport PDF (${binding.periodLabel.text.toString().substringBefore(" ·")}) sera envoyé par e-mail à contact@larmoirecommune.fr.")
            .setPositiveButton("Envoyer") { _, _ -> export() }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun export() {
        binding.btnExport.isEnabled = false
        lifecycleScope.launch {
            val error = repository.exportStats(periode)
            binding.btnExport.isEnabled = true
            Toast.makeText(
                this@AdminStatsActivity,
                error ?: "Rapport envoyé à contact@larmoirecommune.fr",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /** « 2030-01-15 » → « 15/01/2030 » */
    private fun frDate(iso: String): String {
        val p = iso.split("-")
        return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else iso
    }
}
