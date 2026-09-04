package fr.larmoirecommune.app.ui.home

import fr.larmoirecommune.app.model.Reservation
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import fr.larmoirecommune.app.R
import fr.larmoirecommune.app.databinding.ActivityMainBinding
import fr.larmoirecommune.app.model.User
import fr.larmoirecommune.app.network.ApiClient
import fr.larmoirecommune.app.ui.admin.AdminCreateLieuActivity
import fr.larmoirecommune.app.ui.admin.AdminCreateTagActivity
import fr.larmoirecommune.app.ui.admin.AdminAlertObjectsActivity
import fr.larmoirecommune.app.ui.admin.AdminVerificationActivity
import fr.larmoirecommune.app.ui.admin.AdminMaintenanceObjectsActivity
import fr.larmoirecommune.app.ui.admin.AdminDeleteObjectsActivity


import fr.larmoirecommune.app.ui.admin.AdminCreateObjectActivity
import fr.larmoirecommune.app.ui.admin.AdminReservationsActivity
import fr.larmoirecommune.app.ui.auth.LoginActivity
import fr.larmoirecommune.app.ui.objects.ObjectListActivity
import fr.larmoirecommune.app.ui.objects.ReservationListActivity
import fr.larmoirecommune.app.ui.profile.ProfileActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import fr.larmoirecommune.app.worker.ReminderWorker
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit

import fr.larmoirecommune.app.ui.home.LieuMapActivity

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // L'utilisateur a refusé la permission, on peut l'informer
            // Toast.makeText(this, "Les notifications sont désactivées", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startActivity(Intent(this, ScannerActivity::class.java))
        } else {
            Toast.makeText(this, "La permission caméra est nécessaire pour scanner des QR codes", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkCameraPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startActivity(Intent(this, ScannerActivity::class.java))
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }



    private fun setupWorker() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ReminderWorker",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Header Actions
        binding.profileButton.setOnClickListener {
            if (ApiClient.token == null) {
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
            } else {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }

        binding.searchContainer.setOnClickListener {
            startActivity(Intent(this, ObjectListActivity::class.java))
        }

        // Configuration du LayoutManager pour la grille
        binding.dashboardRecycler.layoutManager = GridLayoutManager(this, 2)

        setupWorker()
        checkNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        // Appel API pour récupérer les droits utilisateur (et rafraîchir après login/logout)
        fetchUserAndSetupDashboard()
    }

    private fun fetchUserAndSetupDashboard() {
        lifecycleScope.launch {
            try {
                if (ApiClient.token == null) {
                    ApiClient.currentUserIsAdmin = false
                    ApiClient.currentUserIsPointRelais = false
                    ApiClient.currentUserEmail = null
                    setupDashboardItems()
                    return@launch
                }

                // 1. Appel réseau vers /users/me
                val user: User = ApiClient.client.get(ApiClient.getUrl("users/me")).body()

                // 2. Mise à jour des infos dans le singleton ApiClient
                ApiClient.currentUserIsAdmin = user.isAdmin
                ApiClient.currentUserIsPointRelais = user.isPointRelais
                ApiClient.currentUserEmail = user.email
                ApiClient.currentUserCredits = user.credits

                // Update welcome text if user has a name (assuming User has prenom/nom)
                // binding.welcomeText.text = "Bonjour ${user.prenom ?: ""} !"

            } catch (e: Exception) {
                e.printStackTrace()
                // En cas d'erreur (ex: pas de réseau), on met les valeurs par défaut (non-admin)
                ApiClient.currentUserIsAdmin = false
                ApiClient.currentUserIsPointRelais = false
                ApiClient.currentUserEmail = null
                // Toast.makeText(this@MainActivity, "Mode hors ligne", Toast.LENGTH_SHORT).show()
            } finally {
                // 3. Construction du menu (que l'appel ait réussi ou échoué)
                setupDashboardItems()
            }
        }
    }

    private fun setupDashboardItems() {
        // Liste des éléments accessibles à tous
        val items = mutableListOf(
            DashboardItem(getString(R.string.menu_lieux), R.drawable.ic_admin, R.drawable.bg_gradient_indigo) {
                startActivity(Intent(this, LieuMapActivity::class.java))
            },

            DashboardItem(getString(R.string.menu_library), R.drawable.ic_objects, R.drawable.bg_gradient_green) {
                startActivity(Intent(this, ObjectListActivity::class.java))
            },
            DashboardItem(getString(R.string.menu_my_reservations), R.drawable.ic_reservations, R.drawable.bg_gradient_orange) {
                if (ApiClient.token == null) {
                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                } else {
                    startActivity(Intent(this, ReservationListActivity::class.java))
                }
            },
            DashboardItem(getString(R.string.menu_profile), R.drawable.ic_profile, R.drawable.bg_gradient_blue) {
                if (ApiClient.token == null) {
                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                } else {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
            }
        )

        // Ajout du scanner pour Admin ou Point Relais
        if (ApiClient.currentUserIsAdmin || ApiClient.currentUserIsPointRelais) {
            items.add(0, DashboardItem("Scanner QR Code", R.drawable.ic_scan, R.drawable.bg_gradient_indigo) {
                checkCameraPermissionAndScan()
            })
        }

        // Ajout des éléments Admin SI l'utilisateur est admin
        if (ApiClient.currentUserIsAdmin) {
            items.add(DashboardItem(getString(R.string.menu_admin_create_object), R.drawable.ic_admin, R.drawable.bg_gradient_purple) {
                startActivity(Intent(this, AdminCreateObjectActivity::class.java))
            })
            items.add(DashboardItem(getString(R.string.menu_admin_create_lieu), R.drawable.ic_admin, R.drawable.bg_gradient_indigo) {
                startActivity(Intent(this, AdminCreateLieuActivity::class.java))
            })
            items.add(DashboardItem(getString(R.string.menu_admin_create_tag), R.drawable.ic_admin, R.drawable.bg_gradient_blue) {
                startActivity(Intent(this, AdminCreateTagActivity::class.java))
            })
            items.add(DashboardItem("Objets en retard", R.drawable.ic_admin, R.drawable.bg_gradient_red) {
                startActivity(Intent(this, AdminAlertObjectsActivity::class.java))
            })
            items.add(DashboardItem("À vérifier", R.drawable.ic_admin, R.drawable.bg_gradient_purple) {
                startActivity(Intent(this, AdminVerificationActivity::class.java))
            })
            items.add(DashboardItem("En maintenance", R.drawable.ic_admin, R.drawable.bg_gradient_blue) {
                startActivity(Intent(this, AdminMaintenanceObjectsActivity::class.java))
            })
            items.add(DashboardItem(getString(R.string.menu_admin_reservations), R.drawable.ic_admin, R.drawable.bg_gradient_orange) {
                startActivity(Intent(this, AdminReservationsActivity::class.java))
            })
            items.add(DashboardItem("Supprimer un objet", R.drawable.ic_delete, R.drawable.bg_gradient_red) {
                startActivity(Intent(this, AdminDeleteObjectsActivity::class.java))
            })
        }

        // Affichage final dans le RecyclerView
        binding.dashboardRecycler.adapter = DashboardAdapter(items)
    }
}
