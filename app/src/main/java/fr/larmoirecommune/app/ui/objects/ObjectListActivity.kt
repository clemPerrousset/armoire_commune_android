package fr.larmoirecommune.app.ui.objects

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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

        binding.objectRecycler.layoutManager = LinearLayoutManager(this)
        binding.objectRecycler.adapter = adapter

        viewModel.objects.observe(this) { list ->
            adapter.submitList(list)
        }

        loadObjects()

        binding.availableOnly.setOnCheckedChangeListener { _, _ -> loadObjects() }
    }

    private fun loadObjects() {
        val available = binding.availableOnly.isChecked
        viewModel.loadObjects(available)
    }
}
