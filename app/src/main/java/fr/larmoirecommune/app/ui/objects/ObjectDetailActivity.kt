package fr.larmoirecommune.app.ui.objects

import fr.larmoirecommune.app.model.Reservation
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import fr.larmoirecommune.app.databinding.ActivityObjectDetailBinding
import fr.larmoirecommune.app.network.ApiClient
import fr.larmoirecommune.app.ui.auth.LoginActivity
import fr.larmoirecommune.app.viewmodel.ObjectDetailViewModel
import fr.larmoirecommune.app.repository.UserRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import android.widget.ImageView
import android.widget.TextView

class ObjectDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObjectDetailBinding
    private val viewModel: ObjectDetailViewModel by viewModels()
    private var objectId: Int = -1
    private val userRepository = UserRepository()
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        objectId = intent.getIntExtra("OBJECT_ID", -1)
        if (objectId == -1) finish()

        viewModel.objectDetail.observe(this) { obj ->
             if (obj != null) {
                binding.detailName.text = obj.nom
                binding.detailDesc.text = obj.description
                binding.reserveButton.isEnabled = obj.disponibiliteGlobale
            }
        }


        binding.btnFavorite.setOnClickListener {
            if (ApiClient.token == null) {
                Toast.makeText(this, "Connectez-vous pour ajouter aux favoris", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                if (isFavorite) {
                    val success = userRepository.removeFavori(objectId)
                    if (success) {
                        isFavorite = false
                        binding.btnFavorite.alpha = 0.5f
                        Toast.makeText(this@ObjectDetailActivity, "Retiré des favoris", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val success = userRepository.addFavori(objectId)
                    if (success) {
                        isFavorite = true
                        binding.btnFavorite.alpha = 1.0f
                        Toast.makeText(this@ObjectDetailActivity, "Ajouté aux favoris", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.loadObject(objectId)

        binding.btnQrCode.setOnClickListener {
            showQrCodeBottomSheet()
        }

        binding.reserveButton.setOnClickListener {
            if (ApiClient.token == null) {
                val loginIntent = Intent(this, LoginActivity::class.java)
                loginIntent.putExtra("FINISH_ONLY", true)
                startActivity(loginIntent)
            } else {
                val intent = Intent(this, ReservationActivity::class.java)
                intent.putExtra("OBJECT_ID", objectId)
                startActivity(intent)
            }
        }
    }

    private fun showQrCodeBottomSheet() {
        val obj = viewModel.objectDetail.value ?: return
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(fr.larmoirecommune.app.R.layout.layout_qr_code_bottom_sheet, null)

        val qrImage = view.findViewById<ImageView>(fr.larmoirecommune.app.R.id.qrImage)
        val qrObjectName = view.findViewById<TextView>(fr.larmoirecommune.app.R.id.qrObjectName)

        qrObjectName.text = obj.nom

        try {
            val barcodeEncoder = BarcodeEncoder()
            // Format: armoirecommune://objet/{id}
            val content = "armoirecommune://objet/${obj.id}"
            val bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 512, 512)
            qrImage.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erreur lors de la génération du QR Code", Toast.LENGTH_SHORT).show()
        }

        dialog.setContentView(view)
        dialog.show()
    }
}
