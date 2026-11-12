package fr.larmoirecommune.app.ui.common

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import coil.load
import fr.larmoirecommune.app.R

/** Photo en plein écran avec zoom. Extra : "URL" (adresse complète de l'image). */
class ImageViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("URL")
        if (url.isNullOrBlank()) { finish(); return }

        val dp = resources.displayMetrics.density
        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }

        val spinner = ProgressBar(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER
            )
        }
        val zoom = ZoomImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
            load(url) {
                crossfade(true)
                listener(
                    onSuccess = { _, _ -> spinner.visibility = View.GONE },
                    onError = { _, _ -> spinner.visibility = View.GONE }
                )
            }
        }
        val close = ImageView(this).apply {
            setImageResource(R.drawable.ic_arrow_back)
            setColorFilter(Color.WHITE)
            contentDescription = "Fermer"
            val pad = (12 * dp).toInt()
            setPadding(pad, pad, pad, pad)
            layoutParams = FrameLayout.LayoutParams((56 * dp).toInt(), (56 * dp).toInt(), Gravity.TOP or Gravity.START)
                .apply { topMargin = (32 * dp).toInt() }
            setOnClickListener { finish() }
        }
        root.addView(zoom)
        root.addView(spinner)
        root.addView(close)
        setContentView(root)
    }
}
