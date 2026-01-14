package fr.larmoirecommune.app.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import fr.larmoirecommune.app.R
import fr.larmoirecommune.app.databinding.ActivityMainBinding
import fr.larmoirecommune.app.ui.objects.ObjectListActivity
import fr.larmoirecommune.app.ui.objects.ReservationActivity
import fr.larmoirecommune.app.ui.admin.AdminCreateObjectActivity
import fr.larmoirecommune.app.ui.admin.AdminCreateLieuActivity
import fr.larmoirecommune.app.ui.admin.AdminReservationsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = mutableListOf(
            DashboardItem(getString(R.string.menu_library)) {
                startActivity(Intent(this, ObjectListActivity::class.java))
            },
            DashboardItem(getString(R.string.menu_my_reservations)) {
                 startActivity(Intent(this, fr.larmoirecommune.app.ui.objects.ReservationListActivity::class.java))
            },
            DashboardItem(getString(R.string.menu_profile)) {
                // TODO Profile
            }
        )

        if (fr.larmoirecommune.app.network.ApiClient.currentUserIsAdmin) {
            items.add(DashboardItem(getString(R.string.menu_admin_create_object)) {
                 startActivity(Intent(this, AdminCreateObjectActivity::class.java))
            })
            items.add(DashboardItem(getString(R.string.menu_admin_create_lieu)) {
                 startActivity(Intent(this, AdminCreateLieuActivity::class.java))
            })
            items.add(DashboardItem(getString(R.string.menu_admin_reservations)) {
                 startActivity(Intent(this, AdminReservationsActivity::class.java))
            })
        }

        binding.dashboardRecycler.layoutManager = GridLayoutManager(this, 2)
        binding.dashboardRecycler.adapter = DashboardAdapter(items)
    }
}
