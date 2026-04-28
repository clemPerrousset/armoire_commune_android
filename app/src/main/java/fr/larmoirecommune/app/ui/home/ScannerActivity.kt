package fr.larmoirecommune.app.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import fr.larmoirecommune.app.databinding.ActivityScannerBinding
import fr.larmoirecommune.app.repository.ObjectRepository
import kotlinx.coroutines.launch
import fr.larmoirecommune.app.model.Lieu

class ScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannerBinding
    private lateinit var capture: CaptureManager
    private val repository = ObjectRepository()
    private var isScanning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        capture = CaptureManager(this, binding.barcodeScanner)
        capture.initializeFromIntent(intent, savedInstanceState)
        
        binding.barcodeScanner.decodeContinuous { result ->
            if (isScanning) {
                isScanning = false
                handleScanResult(result.text)
            }
        }
    }

    private fun handleScanResult(text: String) {
        // Format attendu: armoirecommune://objet/{id}
        if (text.startsWith("armoirecommune://objet/")) {
            val objectIdStr = text.substringAfterLast("/")
            val objectId = objectIdStr.toIntOrNull()

            if (objectId != null) {
                processObjectScan(objectId)
            } else {
                showErrorDialog("Format de QR Code invalide : ID manquant.")
            }
        } else {
            showErrorDialog("Ce QR Code n'est pas reconnu par L'Armoire Commune.")
        }
    }

    private fun processObjectScan(objectId: Int) {
        lifecycleScope.launch {
            // 1. Essayer le retrait
            val retraitResult = repository.retirerObjet(objectId)
            if (retraitResult.isSuccess) {
                showSuccessDialog("Objet retiré avec succès !")
            } else {
                // 2. Si échec retrait, essayer le retour
                // Pour le retour, on a besoin d'un lieu. On récupère les lieux et on demande ou on prend le premier.
                val lieux = repository.getLieux()
                if (lieux.isNotEmpty()) {
                    showLieuSelectionDialog(objectId, lieux)
                } else {
                    showErrorDialog("Impossible de trouver un lieu pour le retour.")
                }
            }
        }
    }

    private fun showLieuSelectionDialog(objectId: Int, lieux: List<Lieu>) {
        val lieuNames = lieux.map { it.nom }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Sélectionner le lieu de retour")
            .setItems(lieuNames) { _, which ->
                val selectedLieu = lieux[which]
                performRetour(objectId, selectedLieu.id ?: 1)
            }
            .setNegativeButton("Annuler") { _, _ -> resumeScanning() }
            .setCancelable(false)
            .show()
    }

    private fun performRetour(objectId: Int, lieuId: Int) {
        lifecycleScope.launch {
            val retourResult = repository.retournerObjet(objectId, lieuId)
            if (retourResult.isSuccess) {
                showSuccessDialog("Objet retourné avec succès !")
            } else {
                showErrorDialog("Erreur lors du scan : ni retrait ni retour possible.\n\nDétails : ${retourResult.exceptionOrNull()?.message}")
            }
        }
    }

    private fun showSuccessDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Succès")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> resumeScanning() }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Erreur")
            .setMessage(message)
            .setPositiveButton("Réessayer") { _, _ -> resumeScanning() }
            .setCancelable(false)
            .show()
    }

    private fun resumeScanning() {
        isScanning = true
    }

    override fun onResume() {
        super.onResume()
        binding.barcodeScanner.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeScanner.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }
}
