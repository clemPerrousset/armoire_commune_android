package fr.larmoirecommune.app.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import fr.larmoirecommune.app.databinding.ActivityScannerBinding
import fr.larmoirecommune.app.model.ReservationStatus
import fr.larmoirecommune.app.repository.AdminRepository
import kotlinx.coroutines.launch

class ScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannerBinding
    private lateinit var capture: CaptureManager
    private val repository = AdminRepository()
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
        // Format : armoirecommune://objet/{id}
        if (text.startsWith("armoirecommune://objet/")) {
            val objectId = text.substringAfterLast("/").toIntOrNull()
            if (objectId != null) {
                processScan(objectId)
            } else {
                showError("Format de QR Code invalide.")
            }
        } else {
            showError("QR Code non reconnu par L'Armoire Commune.")
        }
    }

    private fun processScan(objectId: Int) {
        lifecycleScope.launch {
            val result = repository.scanObjet(objectId)
            if (result == null) {
                showError("Aucune réservation active pour cet objet, ou erreur réseau.")
                return@launch
            }

            if (result.verificationRequise) {
                // Statut en_verification → l'admin doit choisir
                showVerificationDialog(objectId, result.objetNom)
            } else {
                val msg = "\"${result.objetNom}\"\n" +
                    "${ReservationStatus.label(result.ancienStatut)} → ${ReservationStatus.label(result.nouveauStatut)}"
                showSuccess(msg)
            }
        }
    }

    private fun showVerificationDialog(objectId: Int, nom: String) {
        AlertDialog.Builder(this)
            .setTitle("Vérification requise")
            .setMessage("\"$nom\" a été retourné.\nL'objet fonctionne-t-il correctement ?")
            .setPositiveButton("Remettre en stock") { _, _ ->
                lifecycleScope.launch {
                    val ok = repository.validerObjet(objectId)
                    if (ok) showSuccess("\"$nom\" remis en stock.")
                    else showError("Erreur lors de la validation.")
                }
            }
            .setNegativeButton("Mettre en maintenance") { _, _ ->
                lifecycleScope.launch {
                    val ok = repository.mettreEnMaintenance(objectId)
                    if (ok) showSuccess("\"$nom\" mis en maintenance.")
                    else showError("Erreur lors de la mise en maintenance.")
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun showSuccess(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Succès")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> resumeScanning() }
            .setCancelable(false)
            .show()
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Erreur")
            .setMessage(message)
            .setPositiveButton("Réessayer") { _, _ -> resumeScanning() }
            .setCancelable(false)
            .show()
    }

    private fun resumeScanning() { isScanning = true }

    override fun onResume() { super.onResume(); binding.barcodeScanner.resume() }
    override fun onPause() { super.onPause(); binding.barcodeScanner.pause() }
    override fun onDestroy() { super.onDestroy(); capture.onDestroy() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }
}
