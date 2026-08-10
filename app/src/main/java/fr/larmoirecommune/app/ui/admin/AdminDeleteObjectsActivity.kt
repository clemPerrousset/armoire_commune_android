package fr.larmoirecommune.app.ui.admin

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.larmoirecommune.app.databinding.ActivityAdminDeleteObjectsBinding
import fr.larmoirecommune.app.model.Objet
import fr.larmoirecommune.app.repository.AdminRepository
import fr.larmoirecommune.app.repository.ObjectRepository
import kotlinx.coroutines.launch

class AdminDeleteObjectsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDeleteObjectsBinding
    private val objectRepository = ObjectRepository()
    private val adminRepository = AdminRepository()

    private lateinit var adapter: AdminDeleteObjectsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDeleteObjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        adapter = AdminDeleteObjectsAdapter { objet -> confirmDelete(objet, position = null) }
        binding.objectRecycler.layoutManager = LinearLayoutManager(this)
        binding.objectRecycler.adapter = adapter

        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.objectRecycler)

        loadObjects()
    }

    private fun loadObjects() {
        lifecycleScope.launch {
            val objets = objectRepository.getObjects()
            adapter.submitList(objets)
            binding.tvEmpty.visibility = if (objets.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun confirmDelete(objet: Objet, position: Int?) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer l'objet ?")
            .setMessage("Êtes-vous sûr de vouloir supprimer cet objet ?")
            .setPositiveButton("Oui") { _, _ -> performDelete(objet) }
            .setNegativeButton("Non") { _, _ -> position?.let { adapter.notifyItemChanged(it) } }
            .setOnCancelListener { position?.let { adapter.notifyItemChanged(it) } }
            .show()
    }

    private fun performDelete(objet: Objet) {
        lifecycleScope.launch {
            val success = adminRepository.deleteObjet(objet.id!!)
            if (success) {
                Toast.makeText(this@AdminDeleteObjectsActivity, "\"${objet.nom}\" supprimé", Toast.LENGTH_SHORT).show()
                loadObjects()
            } else {
                Toast.makeText(
                    this@AdminDeleteObjectsActivity,
                    "Impossible de supprimer : une réservation est peut-être active sur cet objet",
                    Toast.LENGTH_LONG
                ).show()
                loadObjects()
            }
        }
    }

    private val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        private val background = ColorDrawable(Color.parseColor("#FEE2E2"))

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            if (position == RecyclerView.NO_POSITION) return
            val objet = adapter.objetAt(position)
            confirmDelete(objet, position)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            if (dX < 0) {
                background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                background.draw(c)
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}
