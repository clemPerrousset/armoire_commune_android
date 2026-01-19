package fr.larmoirecommune.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import fr.larmoirecommune.app.model.Reservation
import fr.larmoirecommune.app.repository.ObjectRepository
import kotlinx.coroutines.launch

class ReservationListViewModel : ViewModel() {
    private val repository = ObjectRepository()

    private val _reservations = MutableLiveData<List<Reservation>>()
    val reservations: LiveData<List<Reservation>> = _reservations

    fun loadReservations() {
        viewModelScope.launch {
            val list = repository.getMyReservations()
            _reservations.value = list
        }
    }
}
