package fr.larmoirecommune.app.utils

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import fr.larmoirecommune.app.model.Lieu

/** Les horaires sont saisis dans `description` : une ligne par créneau (séparateurs ; ou | acceptés). */
fun formatHoraires(description: String?): String =
    description.orEmpty()
        .replace(Regex("\\s*[;|]\\s*"), "\n")
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString("\n")

/** Bottom sheet d'un point de retrait : nom, adresse, horaires. */
fun showLieuBottomSheet(context: Context, lieu: Lieu) {
    val dp = context.resources.displayMetrics.density
    fun pad(v: Int) = (v * dp).toInt()

    fun text(value: String, sizeSp: Float, bold: Boolean = false, topMargin: Int = 0) = TextView(context).apply {
        text = value
        textSize = sizeSp
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { this.topMargin = pad(topMargin) }
    }

    val content = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(pad(24), pad(24), pad(24), pad(32))
        addView(text(lieu.nom, 20f, bold = true))
        addView(text(lieu.adresse, 15f, topMargin = 8))
        val horaires = formatHoraires(lieu.description)
        addView(text("Horaires d'ouverture", 15f, bold = true, topMargin = 16))
        addView(text(horaires.ifBlank { "Non renseignés" }, 15f, topMargin = 4).apply {
            setLineSpacing(0f, 1.3f)
        })
    }
    BottomSheetDialog(context).apply {
        setContentView(content)
        show()
    }
}
