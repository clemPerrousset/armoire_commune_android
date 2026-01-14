package fr.larmoirecommune.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import fr.larmoirecommune.app.repository.ObjectRepository
import kotlinx.coroutines.launch

class ReservationViewModel : ViewModel() {
    private val repository = ObjectRepository()

    private val _reservationResult = MutableLiveData<Boolean>()
    val reservationResult: LiveData<Boolean> = _reservationResult

    fun createReservation(objetId: Int, lieuId: Int, date: String) {
        viewModelScope.launch {
            val success = repository.createReservation(objetId, lieuId, date)
            _reservationResult.value = success
        }
    }
}
