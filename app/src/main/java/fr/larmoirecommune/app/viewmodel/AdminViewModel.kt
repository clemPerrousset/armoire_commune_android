package fr.larmoirecommune.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.larmoirecommune.app.model.Objet
import fr.larmoirecommune.app.model.Reservation
import fr.larmoirecommune.app.repository.AdminRepository
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val repository = AdminRepository()

    private val _createResult = MutableLiveData<Objet?>()
    val createResult: LiveData<Objet?> = _createResult

    private val _actionResult = MutableLiveData<Boolean>()
    val actionResult: LiveData<Boolean> = _actionResult

    private val _reservations = MutableLiveData<List<Reservation>>()
    val reservations: LiveData<List<Reservation>> = _reservations

    fun createObject(nom: String, desc: String, tagId: Int? = null) {
        viewModelScope.launch {
            val objet = repository.createObject(nom, desc, tagId = tagId)
            _createResult.value = objet
        }
    }

    fun createLieu(nom: String, lat: Double, long: Double, addr: String, description: String? = null) {
        viewModelScope.launch {
            val success = repository.createLieu(nom, lat, long, addr, description)
            _actionResult.value = success
        }
    }

    fun loadReservations(status: String? = null) {
        viewModelScope.launch {
            val list = repository.getAllReservations(status)
            _reservations.value = list
        }
    }

    fun returnObject(id: Int) {
        viewModelScope.launch {
            val success = repository.returnObject(id)
            if (success) loadReservations()
            _actionResult.value = success
        }
    }
}
