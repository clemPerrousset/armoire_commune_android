package fr.larmoirecommune.app.ui.home

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
import fr.larmoirecommune.app.ui.admin.AdminCreateObjectActivity
import fr.larmoirecommune.app.ui.admin.AdminReservationsActivity
import fr.larmoirecommune.app.ui.objects.ObjectListActivity
import fr.larmoirecommune.app.ui.objects.ReservationListActivity
import fr.larmoirecommune.app.ui.profile.ProfileActivity
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Header Actions
        binding.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.searchContainer.setOnClickListener {
            startActivity(Intent(this, ObjectListActivity::class.java))
        }

        // Configuration du LayoutManager pour la grille
        binding.dashboardRecycler.layoutManager = GridLayoutManager(this, 2)

        // Appel API pour récupérer les droits utilisateur
        fetchUserAndSetupDashboard()
    }

    private fun fetchUserAndSetupDashboard() {
        lifecycleScope.launch {
            try {
                // 1. Appel réseau vers /users/me
                val user: User = ApiClient.client.get(ApiClient.getUrl("users/me")).body()

                // 2. Mise à jour des infos dans le singleton ApiClient
                ApiClient.currentUserIsAdmin = user.isAdmin
                ApiClient.currentUserEmail = user.email

                // Update welcome text if user has a name (assuming User has prenom/nom)
                // binding.welcomeText.text = "Bonjour ${user.prenom ?: ""} !"

            } catch (e: Exception) {
                e.printStackTrace()
                // En cas d'erreur (ex: pas de réseau), on met les valeurs par défaut (non-admin)
                ApiClient.currentUserIsAdmin = false
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
            DashboardItem(getString(R.string.menu_library), R.drawable.ic_objects) {
                startActivity(Intent(this, ObjectListActivity::class.java))
            },
            DashboardItem(getString(R.string.menu_my_reservations), R.drawable.ic_reservations) {
                startActivity(Intent(this, ReservationListActivity::class.java))
            },
            DashboardItem(getString(R.string.menu_profile), R.drawable.ic_profile) {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        )

        // Ajout des éléments Admin SI l'utilisateur est admin
        if (ApiClient.currentUserIsAdmin) {
            items.add(DashboardItem(getString(R.string.menu_admin_create_object), R.drawable.ic_admin) {
                startActivity(Intent(this, AdminCreateObjectActivity::class.java))
            })
            items.add(DashboardItem(getString(R.string.menu_admin_create_lieu), R.drawable.ic_admin) {
                startActivity(Intent(this, AdminCreateLieuActivity::class.java))
            })
            items.add(DashboardItem(getString(R.string.menu_admin_reservations), R.drawable.ic_admin) {
                startActivity(Intent(this, AdminReservationsActivity::class.java))
            })
        }

        // Affichage final dans le RecyclerView
        binding.dashboardRecycler.adapter = DashboardAdapter(items)
    }
}
