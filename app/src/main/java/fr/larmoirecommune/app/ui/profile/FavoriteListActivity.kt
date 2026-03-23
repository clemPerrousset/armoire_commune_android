package fr.larmoirecommune.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import fr.larmoirecommune.app.databinding.ActivityObjectListBinding
import fr.larmoirecommune.app.repository.UserRepository
import fr.larmoirecommune.app.ui.objects.ObjectAdapter
import fr.larmoirecommune.app.ui.objects.ObjectDetailActivity
import kotlinx.coroutines.launch

class FavoriteListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObjectListBinding
    private val repository = UserRepository()
    private val adapter = ObjectAdapter { objet ->
        val intent = Intent(this, ObjectDetailActivity::class.java)
        intent.putExtra("OBJECT_ID", objet.id)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We reuse ActivityObjectListBinding to save time
        binding = ActivityObjectListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        // title hidden

        // Hide chips and search which aren't needed here
        binding.filterArea.visibility = View.GONE
        // handled by filterArea

        binding.objectRecycler.layoutManager = LinearLayoutManager(this)
        binding.objectRecycler.adapter = adapter

        loadFavoris()
    }

    private fun loadFavoris() {
        lifecycleScope.launch {
            val favoris = repository.getFavoris()
            adapter.submitList(favoris)
        }
    }
}
