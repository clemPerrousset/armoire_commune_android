package fr.larmoirecommune.app.ui.objects

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import fr.larmoirecommune.app.databinding.ActivityObjectDetailBinding
import fr.larmoirecommune.app.model.ReservationStatus
import fr.larmoirecommune.app.network.ApiClient
import fr.larmoirecommune.app.repository.ObjectRepository
import fr.larmoirecommune.app.repository.UserRepository
import fr.larmoirecommune.app.ui.auth.LoginActivity
import fr.larmoirecommune.app.ui.common.ImageViewerActivity
import fr.larmoirecommune.app.viewmodel.ObjectDetailViewModel
import kotlinx.coroutines.launch

class ObjectDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObjectDetailBinding
    private val viewModel: ObjectDetailViewModel by viewModels()
    private var objectId: Int = -1
    private val userRepository = UserRepository()
    private val objectRepository = ObjectRepository()
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        objectId = intent.getIntExtra("OBJECT_ID", -1)
        if (objectId == -1) { finish(); return }

        viewModel.objectDetail.observe(this) { obj ->
            if (obj == null) return@observe
            binding.detailName.text = obj.nom
            binding.detailDesc.text = obj.description
            binding.reserveButton.isEnabled = obj.disponibiliteGlobale

            val categories = obj.allTags
            if (categories.isNotEmpty()) {
                binding.tvTag.text = categories.joinToString("  ·  ") { it.nom }
                binding.tvTag.visibility = View.VISIBLE
            } else {
                binding.tvTag.visibility = View.GONE
            }

            // Code d'emplacement : renvoyé par le serveur uniquement aux admins
            if (ApiClient.currentUserIsAdmin && !obj.stockage.isNullOrBlank()) {
                binding.tvStockage.text = "Stockage : ${obj.stockage}"
                binding.tvStockage.visibility = View.VISIBLE
            } else {
                binding.tvStockage.visibility = View.GONE
            }

            if (!obj.image.isNullOrBlank()) {
                val imageUrl = ApiClient.getUrl(obj.image)
                Log.d("ObjectDetail", "Loading image: $imageUrl")
                binding.objectImage.load(imageUrl) {
                    crossfade(true)
                    error(android.R.drawable.ic_menu_gallery)
                }
                // Toucher la photo : plein écran avec zoom
                binding.objectImage.setOnClickListener {
                    startActivity(Intent(this, ImageViewerActivity::class.java).putExtra("URL", imageUrl))
                }
            } else {
                Log.d("ObjectDetail", "No image for object ${obj.id}")
            }
        }

        viewModel.loadObject(objectId)

        binding.btnQrCode.visibility = if (ApiClient.currentUserIsAdmin) View.VISIBLE else View.GONE
        binding.btnQrCode.setOnClickListener { showQrDialog() }

        if (ApiClient.token != null) {
            lifecycleScope.launch { initFavoriteState() }
        } else {
            binding.btnFavorite.alpha = 0.4f
        }

        binding.btnFavorite.setOnClickListener {
            if (ApiClient.token == null) {
                Toast.makeText(this, "Connectez-vous pour ajouter aux favoris", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                if (isFavorite) {
                    if (userRepository.removeFavori(objectId)) {
                        isFavorite = false
                        binding.btnFavorite.alpha = 0.4f
                        Toast.makeText(this@ObjectDetailActivity, "Retiré des favoris", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (userRepository.addFavori(objectId)) {
                        isFavorite = true
                        binding.btnFavorite.alpha = 1.0f
                        Toast.makeText(this@ObjectDetailActivity, "Ajouté aux favoris", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        loadReservationCalendar()

        binding.reserveButton.setOnClickListener {
            if (ApiClient.token == null) {
                val loginIntent = Intent(this, LoginActivity::class.java)
                loginIntent.putExtra("FINISH_ONLY", true)
                startActivity(loginIntent)
            } else {
                startActivity(Intent(this, ReservationActivity::class.java).apply {
                    putExtra("OBJECT_ID", objectId)
                })
            }
        }
    }

    private fun showQrDialog() {
        val content = "armoirecommune://objet/$objectId"
        try {
            val encoder = BarcodeEncoder()
            val bitmap: Bitmap = encoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 600, 600)
            val imageView = ImageView(this).apply {
                setImageBitmap(bitmap)
                val pad = (32 * resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
            }
            AlertDialog.Builder(this)
                .setTitle("QR Code de l'objet")
                .setView(imageView)
                .setPositiveButton("Fermer", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Impossible de générer le QR code", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun initFavoriteState() {
        val favoris = userRepository.getFavoris()
        isFavorite = favoris.any { it.id == objectId }
        binding.btnFavorite.alpha = if (isFavorite) 1.0f else 0.4f
    }

    /** 10 prochaines semaines (jeudi → mercredi) : grisées si réservées ou fermées, sinon libres. */
    private fun loadReservationCalendar() {
        lifecycleScope.launch {
            val ranges = objectRepository.getReservationsForObjet(objectId).map { it.dateDebut to it.dateFin } +
                objectRepository.getFermetures().map { it.dateDebut to it.dateFin }
            val cal = ReservationActivity.firstUpcomingThursday()
            val text = android.text.SpannableStringBuilder()
            repeat(10) {
                val start = cal.timeInMillis
                val endCal = cal.clone() as java.util.Calendar
                endCal.add(java.util.Calendar.DAY_OF_YEAR, 6)
                val end = endCal.timeInMillis
                val booked = ranges.any { (s, e) -> start <= isoToMillis(e) && end >= isoToMillis(s) }
                val line = "Du ${fmt(cal)} au ${fmt(endCal)} : ${if (booked) "réservé" else "libre"}\n"
                val from = text.length
                text.append(line)
                if (booked) text.setSpan(
                    android.text.style.ForegroundColorSpan(android.graphics.Color.GRAY),
                    from, text.length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                cal.add(java.util.Calendar.WEEK_OF_YEAR, 1)
            }
            binding.tvCalendar.text = text.trim()
        }
    }

    private fun fmt(c: java.util.Calendar) = String.format(
        "%02d/%02d/%04d", c.get(java.util.Calendar.DAY_OF_MONTH), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.YEAR)
    )

    private fun isoToMillis(iso: String): Long = try {
        val (y, m, d) = iso.substringBefore("T").split("-").map { it.toInt() }
        java.util.Calendar.getInstance().apply { set(y, m - 1, d, 0, 0, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
    } catch (e: Exception) { 0L }

    private fun formatDate(iso: String): String {
        return try {
            val date = iso.substringBefore("T")
            val parts = date.split("-")
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } catch (e: Exception) { iso }
    }
}
