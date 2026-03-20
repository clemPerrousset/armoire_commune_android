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
import android.text.Editable
import android.text.TextWatcher


class ObjectListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObjectListBinding
    private val viewModel: ObjectListViewModel by viewModels()

    private var isAvailableOnly = false
    private var currentSearchQuery: String? = null
    private var selectedTagId: Int? = null
    private lateinit var tagAdapter: TagAdapter

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


        // Tags Recycler
        tagAdapter = TagAdapter { tag ->
            selectedTagId = tag?.id
            loadObjects(isAvailableOnly)
        }
        binding.tagRecycler.adapter = tagAdapter

        viewModel.tags.observe(this) { tags ->
            tagAdapter.submitList(tags)
        }
        viewModel.loadTags()

        // Search
        binding.searchParams.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s?.toString()?.takeIf { it.isNotBlank() }
                loadObjects(isAvailableOnly)
            }
        })

        // Initial Load
        isAvailableOnly = false
        loadObjects(false)

        // Chips Logic
        binding.chipAll.setOnClickListener {
            updateChips(true)
            isAvailableOnly = false
            loadObjects(false)
        }

        binding.chipAvailable.setOnClickListener {
            updateChips(false)
            isAvailableOnly = true
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
        viewModel.loadObjects(availableOnly, currentSearchQuery, selectedTagId)
    }
}
