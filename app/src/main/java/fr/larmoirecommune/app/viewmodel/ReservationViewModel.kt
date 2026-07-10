package fr.larmoirecommune.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import fr.larmoirecommune.app.model.Lieu
import fr.larmoirecommune.app.repository.ObjectRepository
import kotlinx.coroutines.launch

class ReservationViewModel : ViewModel() {
    private val repository = ObjectRepository()

    private val _reservationResult = MutableLiveData<Boolean>()
    val reservationResult: LiveData<Boolean> = _reservationResult

    private val _lieux = MutableLiveData<List<Lieu>>()
    val lieux: LiveData<List<Lieu>> = _lieux

    fun loadLieux() {
        viewModelScope.launch {
            _lieux.value = repository.getLieux()
        }
    }

    fun createReservation(objetId: Int, lieuId: Int, date: String, nbSemaines: Int = 1) {
        viewModelScope.launch {
            val success = repository.createReservation(objetId, lieuId, date, nbSemaines)
            _reservationResult.value = success
        }
    }
}
