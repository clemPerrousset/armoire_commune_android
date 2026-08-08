package fr.larmoirecommune.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import fr.larmoirecommune.app.databinding.ActivityObjectListBinding
import fr.larmoirecommune.app.model.Objet
import fr.larmoirecommune.app.repository.UserRepository
import fr.larmoirecommune.app.ui.objects.ObjectAdapter
import fr.larmoirecommune.app.ui.objects.ObjectDetailActivity
import kotlinx.coroutines.launch

class FavoriteListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObjectListBinding
    private val repository = UserRepository()
    private var allFavoris: List<Objet> = emptyList()
    private val adapter = ObjectAdapter { objet ->
        val intent = Intent(this, ObjectDetailActivity::class.java)
        intent.putExtra("OBJECT_ID", objet.id)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObjectListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.tvPageTitle.text = "Favoris"

        // Hide chips and tag filter, keep search bar
        binding.chipAll.visibility = View.GONE
        binding.chipAvailable.visibility = View.GONE
        binding.tagRecycler.visibility = View.GONE

        binding.objectRecycler.layoutManager = LinearLayoutManager(this)
        binding.objectRecycler.adapter = adapter

        binding.searchParams.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterFavoris(s?.toString()?.trim())
            }
        })

        loadFavoris()
    }

    private fun loadFavoris() {
        lifecycleScope.launch {
            allFavoris = repository.getFavoris()
            adapter.submitList(allFavoris)
        }
    }

    private fun filterFavoris(query: String?) {
        if (query.isNullOrBlank()) {
            adapter.submitList(allFavoris)
        } else {
            val q = query.lowercase()
            adapter.submitList(allFavoris.filter {
                it.nom.lowercase().contains(q) || it.description.lowercase().contains(q)
            })
        }
    }
}
