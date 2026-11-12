package fr.larmoirecommune.app.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.ceil
import kotlin.math.max

/**
 * Histogramme groupé, sans dépendance : une barre par série et par abscisse (le temps).
 * Légende en haut, graduations à gauche, étiquettes espacées en bas.
 */
class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    class Series(val name: String, val color: Int, val values: List<Int>)

    private var labels: List<String> = emptyList()
    private var series: List<Series> = emptyList()

    private val density = resources.displayMetrics.density
    private val sp = resources.displayMetrics.scaledDensity
    private fun dp(v: Float) = v * density

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF6B7280.toInt()
        textSize = 11f * sp
    }
    private val gridPaint = Paint().apply {
        color = 0xFFE5E7EB.toInt()
        strokeWidth = dp(1f)
    }
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    fun setData(labels: List<String>, series: List<Series>) {
        this.labels = labels
        this.series = series
        contentDescription = series.joinToString(", ") { s -> "${s.name} : ${s.values.sum()} au total" }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val h = resolveSize(dp(240f).toInt(), heightMeasureSpec)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val peak = max(1, series.maxOfOrNull { s -> s.values.maxOrNull() ?: 0 } ?: 0)
        val empty = series.isEmpty() || series.all { s -> s.values.all { it == 0 } }

        val left = dp(30f)
        val top = dp(28f)
        val bottom = height - dp(24f)
        val right = width - dp(6f)
        val chartH = bottom - top

        // Légende
        var lx = left
        series.forEach { s ->
            barPaint.color = s.color
            rect.set(lx, dp(6f), lx + dp(10f), dp(16f))
            canvas.drawRoundRect(rect, dp(2f), dp(2f), barPaint)
            canvas.drawText(s.name, lx + dp(14f), dp(15f), textPaint)
            lx += dp(14f) + textPaint.measureText(s.name) + dp(16f)
        }

        // Grille et graduations (0, milieu, max)
        listOf(0f, 0.5f, 1f).forEach { f ->
            val y = bottom - chartH * f
            canvas.drawLine(left, y, right, y, gridPaint)
            val tick = (peak * f).let { if (f == 0.5f) ceil(it).toInt() else it.toInt() }
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(tick.toString(), left - dp(6f), y + dp(4f), textPaint)
        }
        textPaint.textAlign = Paint.Align.LEFT

        if (empty || labels.isEmpty()) {
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Aucune donnée sur cette période", (left + right) / 2, (top + bottom) / 2, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
            return
        }

        val n = labels.size
        val slot = (right - left) / n
        val barW = slot * 0.8f / series.size
        series.forEachIndexed { j, s ->
            barPaint.color = s.color
            s.values.forEachIndexed { i, v ->
                if (v > 0) {
                    val x = left + i * slot + slot * 0.1f + j * barW
                    rect.set(x, bottom - chartH * v / peak, x + barW - dp(1f), bottom)
                    canvas.drawRoundRect(rect, dp(2f), dp(2f), barPaint)
                }
            }
        }

        // Abscisse : une étiquette sur `step`, pour que le texte ne se chevauche pas
        val labelW = textPaint.measureText(labels.maxByOrNull { it.length } ?: "") + dp(8f)
        val step = max(1, ceil(labelW / slot).toInt())
        textPaint.textAlign = Paint.Align.CENTER
        for (i in labels.indices step step) {
            canvas.drawText(labels[i], left + i * slot + slot / 2, height - dp(6f), textPaint)
        }
        textPaint.textAlign = Paint.Align.LEFT
    }
}
