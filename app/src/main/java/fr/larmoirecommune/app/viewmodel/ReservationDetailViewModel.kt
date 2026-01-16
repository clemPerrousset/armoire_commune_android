package fr.larmoirecommune.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import fr.larmoirecommune.app.model.Reservation
import fr.larmoirecommune.app.repository.ObjectRepository // On utilise bien ObjectRepository
import kotlinx.coroutines.launch

class ReservationDetailViewModel : ViewModel() {

    // On instancie votre ObjectRepository
    private val repository = ObjectRepository()

    private val _reservation = MutableLiveData<Reservation?>()
    val reservation: LiveData<Reservation?> = _reservation

    fun loadReservation(id: Int) {
        viewModelScope.launch {
            // On appelle la nouvelle méthode qu'on vient d'ajouter au repo
            val res = repository.getReservation(id)
            _reservation.value = res
        }
    }
}