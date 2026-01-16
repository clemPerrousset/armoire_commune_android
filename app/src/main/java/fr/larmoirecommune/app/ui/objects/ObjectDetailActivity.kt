package fr.larmoirecommune.app.ui.objects

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import fr.larmoirecommune.app.databinding.ActivityObjectDetailBinding
import fr.larmoirecommune.app.viewmodel.ObjectDetailViewModel

class ObjectDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObjectDetailBinding
    private val viewModel: ObjectDetailViewModel by viewModels()
    private var objectId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        objectId = intent.getIntExtra("OBJECT_ID", -1)
        if (objectId == -1) finish()

        viewModel.objectDetail.observe(this) { obj ->
             if (obj != null) {
                binding.detailName.text = obj.nom
                binding.detailDesc.text = obj.description
                binding.reserveButton.isEnabled = obj.disponibiliteGlobale
            }
        }

        viewModel.loadObject(objectId)

        binding.reserveButton.setOnClickListener {
            val intent = Intent(this, ReservationActivity::class.java)
            intent.putExtra("OBJECT_ID", objectId)
            startActivity(intent)
        }
    }
}
