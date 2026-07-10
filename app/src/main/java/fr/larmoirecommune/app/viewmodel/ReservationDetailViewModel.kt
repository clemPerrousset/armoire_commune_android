package fr.larmoirecommune.app.viewmodel

import fr.larmoirecommune.app.model.Reservation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import fr.larmoirecommune.app.repository.ObjectRepository
import kotlinx.coroutines.launch

class ReservationDetailViewModel : ViewModel() {
    private val repository = ObjectRepository()

    private val _reservation = MutableLiveData<Reservation?>()
    val reservation: LiveData<Reservation?> = _reservation

    private val _cancelResult = MutableLiveData<Boolean?>()
    val cancelResult: LiveData<Boolean?> = _cancelResult

    fun loadReservation(id: Int) {
        viewModelScope.launch {
            _reservation.value = repository.getReservation(id)
        }
    }

    fun cancelReservation(id: Int) {
        viewModelScope.launch {
            val success = repository.cancelReservation(id)
            _cancelResult.value = success
            if (success) loadReservation(id)
        }
    }
}
