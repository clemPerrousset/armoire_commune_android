package fr.larmoirecommune.app.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import fr.larmoirecommune.app.databinding.ActivitySignupBinding
import fr.larmoirecommune.app.viewmodel.SignupViewModel

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val viewModel: SignupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.signupResult.observe(this) { success ->
             if (success) {
                Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Erreur d'inscription", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signupButton.setOnClickListener {
            val nom = binding.nomInput.text.toString()
            val prenom = binding.prenomInput.text.toString()
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            viewModel.signup(nom, prenom, email, password)
        }

        binding.goToLogin.setOnClickListener {
            finish()
        }
    }
}
