package fr.larmoirecommune.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import fr.larmoirecommune.app.repository.AuthRepository
import kotlinx.coroutines.launch

class SignupViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _signupResult = MutableLiveData<Boolean>()
    val signupResult: LiveData<Boolean> = _signupResult

    fun signup(nom: String, prenom: String, email: String, pass: String) {
        viewModelScope.launch {
            val success = repository.signup(nom, prenom, email, pass)
            _signupResult.value = success
        }
    }
}
