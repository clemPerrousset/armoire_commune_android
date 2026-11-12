package fr.larmoirecommune.app.ui.common

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

/**
 * Choix d'une photo (galerie ou appareil) suivi d'un écran de recadrage avec zoom et
 * déplacement (uCrop). À instancier comme propriété de l'Activity (enregistre des launchers).
 * `onPhoto` reçoit l'Uri de l'image recadrée.
 */
class PhotoPicker(
    private val activity: AppCompatActivity,
    private val onPhoto: (Uri) -> Unit
) {
    private var cameraUri: Uri? = null

    private val pick = activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { crop(it) }
    }

    private val take = activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) cameraUri?.let { crop(it) }
    }

    private val cropResult = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) {
            r.data?.let { UCrop.getOutput(it) }?.let(onPhoto)
        }
    }

    private val cameraPermission = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(activity, "Permission caméra refusée", Toast.LENGTH_SHORT).show()
    }

    fun show() {
        AlertDialog.Builder(activity)
            .setTitle("Ajouter une photo")
            .setItems(arrayOf("Galerie", "Appareil photo")) { _, which ->
                when (which) {
                    0 -> pick.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    1 -> cameraPermission.launch(Manifest.permission.CAMERA)
                }
            }
            .show()
    }

    private fun launchCamera() {
        val dir = File(activity.filesDir, "images").apply { if (!exists()) mkdirs() }
        val file = File(dir, "camera_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", file)
        cameraUri = uri
        take.launch(uri)
    }

    private fun crop(source: Uri) {
        val dest = Uri.fromFile(File(activity.cacheDir, "crop_${UUID.randomUUID()}.jpg"))
        val options = UCrop.Options().apply {
            setFreeStyleCropEnabled(true)       // cadre redimensionnable : on garde l'objet en entier
            setCompressionQuality(90)
            setToolbarTitle("Recadrer la photo")
        }
        val intent = UCrop.of(source, dest).withOptions(options).withMaxResultSize(1600, 1600)
            .getIntent(activity)
            .setClass(activity, CropActivity::class.java) // version qui respecte la safe zone
        cropResult.launch(intent)
    }
}
