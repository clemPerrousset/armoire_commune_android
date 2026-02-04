package fr.larmoirecommune.app.ui.objects

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import fr.larmoirecommune.app.R
import fr.larmoirecommune.app.databinding.ActivityObjectListBinding
import fr.larmoirecommune.app.viewmodel.ObjectListViewModel

class ObjectListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObjectListBinding
    private val viewModel: ObjectListViewModel by viewModels()
    private val adapter = ObjectAdapter { objet ->
        val intent = Intent(this, ObjectDetailActivity::class.java)
        intent.putExtra("OBJECT_ID", objet.id)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObjectListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Header
        binding.btnBack.setOnClickListener { finish() }

        // Setup Recycler
        binding.objectRecycler.layoutManager = LinearLayoutManager(this)
        binding.objectRecycler.adapter = adapter

        // Observe
        viewModel.objects.observe(this) { list ->
            adapter.submitList(list)
        }

        // Initial Load
        loadObjects(false)

        // Chips Logic
        binding.chipAll.setOnClickListener {
            updateChips(true)
            loadObjects(false)
        }

        binding.chipAvailable.setOnClickListener {
            updateChips(false)
            loadObjects(true)
        }
    }

    private fun updateChips(isAllSelected: Boolean) {
        val mintPrimary = ContextCompat.getColor(this, R.color.mint_primary)
        val white = ContextCompat.getColor(this, R.color.white)
        val textSec = ContextCompat.getColor(this, R.color.text_secondary)

        if (isAllSelected) {
            binding.chipAll.backgroundTintList = ColorStateList.valueOf(mintPrimary)
            binding.chipAll.setTextColor(white)

            binding.chipAvailable.backgroundTintList = ColorStateList.valueOf(white)
            binding.chipAvailable.setTextColor(textSec)
        } else {
            binding.chipAll.backgroundTintList = ColorStateList.valueOf(white)
            binding.chipAll.setTextColor(textSec)

            binding.chipAvailable.backgroundTintList = ColorStateList.valueOf(mintPrimary)
            binding.chipAvailable.setTextColor(white)
        }
    }

    private fun loadObjects(availableOnly: Boolean) {
        viewModel.loadObjects(availableOnly)
    }
}
