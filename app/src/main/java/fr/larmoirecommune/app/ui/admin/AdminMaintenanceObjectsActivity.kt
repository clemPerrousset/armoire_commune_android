package fr.larmoirecommune.app.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import fr.larmoirecommune.app.databinding.ActivityAdminMaintenanceBinding
import fr.larmoirecommune.app.databinding.ItemMaintenanceBinding
import fr.larmoirecommune.app.model.Objet
import fr.larmoirecommune.app.model.ReservationStatus
import fr.larmoirecommune.app.network.ApiClient
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.ui.objects.ObjectDetailActivity
import kotlinx.coroutines.launch

class AdminMaintenanceObjectsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminMaintenanceBinding
    private val repository = AdminRepository()

    private val adapter = MaintenanceAdapter(
        onItemClick = { objet ->
            startActivity(Intent(this, ObjectDetailActivity::class.java).apply {
                putExtra("OBJECT_ID", objet.id)
            })
        },
        onRestoreClick = { objet ->
            lifecycleScope.launch {
                val result = objet.id?.let { repository.scanObjet(it) }
                if (result != null) {
                    Toast.makeText(
                        this@AdminMaintenanceObjectsActivity,
                        "\"${result.objetNom}\" : ${ReservationStatus.label(result.ancienStatut)} → ${ReservationStatus.label(result.nouveauStatut)}",
                        Toast.LENGTH_LONG
                    ).show()
                    loadData()
                } else {
                    Toast.makeText(this@AdminMaintenanceObjectsActivity, "Erreur", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMaintenanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            val items = repository.getMaintenanceObjects()
            adapter.submitList(items)
            binding.tvEmpty.visibility = if (items.isEmpty())
                android.view.View.VISIBLE else android.view.View.GONE
        }
    }
}

class MaintenanceAdapter(
    private val onItemClick: (Objet) -> Unit,
    private val onRestoreClick: (Objet) -> Unit
) : RecyclerView.Adapter<MaintenanceAdapter.VH>() {

    private var items: List<Objet> = emptyList()

    fun submitList(list: List<Objet>) {
        items = list
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemMaintenanceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemMaintenanceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvNom.text = item.nom
        holder.binding.tvDescription.text = item.description

        if (!item.image.isNullOrBlank()) {
            holder.binding.itemImage.load(ApiClient.getUrl(item.image)) {
                crossfade(true)
                placeholder(fr.larmoirecommune.app.R.drawable.ic_objects)
            }
        }

        holder.binding.btnRestore.setOnClickListener { onRestoreClick(item) }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }
}
