package fr.larmoirecommune.app.ui.common

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

/** ImageView avec pincement pour zoomer, glisser pour déplacer, double-tap pour zoom / reset. */
class ZoomImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val m = Matrix()
    private val values = FloatArray(9)
    private var baseScale = 1f

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(d: ScaleGestureDetector): Boolean {
            val current = currentScale()
            val target = (current * d.scaleFactor).coerceIn(baseScale, baseScale * MAX_ZOOM)
            val factor = target / current
            m.postScale(factor, factor, d.focusX, d.focusY)
            clampTranslation()
            imageMatrix = m
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
            if (scaleDetector.isInProgress) return false
            m.postTranslate(-dx, -dy)
            clampTranslation()
            imageMatrix = m
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (currentScale() > baseScale * 1.01f) {
                fitToView()
            } else {
                m.postScale(2.5f, 2.5f, e.x, e.y)
                clampTranslation()
                imageMatrix = m
            }
            return true
        }
    })

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        post { fitToView() }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fitToView()
    }

    private fun fitToView() {
        val d = drawable ?: return
        val dw = d.intrinsicWidth.toFloat()
        val dh = d.intrinsicHeight.toFloat()
        if (dw <= 0 || dh <= 0 || width == 0 || height == 0) return
        baseScale = min(width / dw, height / dh)
        m.reset()
        m.postScale(baseScale, baseScale)
        m.postTranslate((width - dw * baseScale) / 2f, (height - dh * baseScale) / 2f)
        imageMatrix = m
    }

    private fun currentScale(): Float {
        m.getValues(values)
        return values[Matrix.MSCALE_X]
    }

    /** Garde l'image dans le cadre (centrée si plus petite que la vue). */
    private fun clampTranslation() {
        val d = drawable ?: return
        val rect = RectF(0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
        m.mapRect(rect)
        val dx = when {
            rect.width() <= width -> (width - rect.width()) / 2f - rect.left
            rect.left > 0 -> -rect.left
            rect.right < width -> width - rect.right
            else -> 0f
        }
        val dy = when {
            rect.height() <= height -> (height - rect.height()) / 2f - rect.top
            rect.top > 0 -> -rect.top
            rect.bottom < height -> height - rect.bottom
            else -> 0f
        }
        m.postTranslate(dx, dy)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    private companion object {
        const val MAX_ZOOM = 5f
    }
}
