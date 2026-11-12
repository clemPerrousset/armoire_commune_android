package fr.larmoirecommune.app.ui.common

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yalantis.ucrop.UCropActivity

/**
 * Écran de recadrage uCrop adapté à l'affichage bord à bord (Android 15+ / targetSdk 36) :
 * uCrop dessine sous la barre d'état et la barre de navigation, ce qui rend la barre
 * d'outils (valider / annuler) inaccessible. On applique donc les marges des barres système
 * (et de l'encoche) au contenu.
 */
class CropActivity : UCropActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(content)
    }
}
