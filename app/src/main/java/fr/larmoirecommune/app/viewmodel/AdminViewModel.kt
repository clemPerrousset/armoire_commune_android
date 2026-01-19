package fr.larmoirecommune.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import fr.larmoirecommune.app.model.Reservation
import fr.larmoirecommune.app.repository.AdminRepository
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val repository = AdminRepository()

    private val _actionResult = MutableLiveData<Boolean>()
    val actionResult: LiveData<Boolean> = _actionResult

    private val _reservations = MutableLiveData<List<Reservation>>()
    val reservations: LiveData<List<Reservation>> = _reservations

    fun createObject(nom: String, desc: String, qty: Int, tagId: Int) {
        viewModelScope.launch {
            val success = repository.createObject(nom, desc, qty, tagId, emptyList())
            _actionResult.value = success
        }
    }

    fun createLieu(nom: String, lat: Double, long: Double, addr: String) {
        viewModelScope.launch {
            val success = repository.createLieu(nom, lat, long, addr)
            _actionResult.value = success
        }
    }

    fun loadReservations() {
         viewModelScope.launch {
             val list = repository.getAllReservations()
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
