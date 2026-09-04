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
import fr.larmoirecommune.app.databinding.ActivityAdminVerificationBinding
import fr.larmoirecommune.app.databinding.ItemVerificationBinding
import fr.larmoirecommune.app.model.ObjetWithReservation
import fr.larmoirecommune.app.network.ApiClient
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.ui.objects.ObjectDetailActivity
import kotlinx.coroutines.launch

class AdminVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminVerificationBinding
    private val repository = AdminRepository()

    private val adapter = VerificationAdapter(
        onValider = { objet ->
            lifecycleScope.launch {
                val ok = repository.validerObjet(objet.id!!)
                if (ok) {
                    Toast.makeText(this@AdminVerificationActivity, "\"${objet.nom}\" remis en stock", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this@AdminVerificationActivity, "Erreur", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onMaintenance = { objet ->
            lifecycleScope.launch {
                val ok = repository.mettreEnMaintenance(objet.id!!)
                if (ok) {
                    Toast.makeText(this@AdminVerificationActivity, "\"${objet.nom}\" mis en maintenance", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this@AdminVerificationActivity, "Erreur", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            val items = repository.getVerificationObjects()
            adapter.submitList(items)
            binding.tvEmpty.visibility = if (items.isEmpty())
                android.view.View.VISIBLE else android.view.View.GONE
        }
    }
}

class VerificationAdapter(
    private val onValider: (ObjetWithReservation) -> Unit,
    private val onMaintenance: (ObjetWithReservation) -> Unit
) : RecyclerView.Adapter<VerificationAdapter.VH>() {

    private var items: List<ObjetWithReservation> = emptyList()

    fun submitList(list: List<ObjetWithReservation>) {
        items = list
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemVerificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemVerificationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

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

        val res = item.reservation
        if (res != null) {
            val dateRetour = try {
                val date = res.dateFin.substringBefore("T")
                val p = date.split("-")
                "Retourné le ${p[2]}/${p[1]}/${p[0]}"
            } catch (e: Exception) { res.dateFin }
            holder.binding.tvDates.text = dateRetour
        }

        holder.binding.btnValider.setOnClickListener { onValider(item) }
        holder.binding.btnMaintenance.setOnClickListener { onMaintenance(item) }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            context.startActivity(Intent(context, ObjectDetailActivity::class.java).apply {
                putExtra("OBJECT_ID", item.id)
            })
        }
    }
}
