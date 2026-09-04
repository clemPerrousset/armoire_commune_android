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

    private val _bookedRanges = MutableLiveData<List<Pair<String, String>>>(emptyList())
    val bookedRanges: LiveData<List<Pair<String, String>>> = _bookedRanges

    fun loadLieux() {
        viewModelScope.launch {
            _lieux.value = repository.getLieux()
        }
    }

    fun loadReservationsForObjet(objetId: Int) {
        viewModelScope.launch {
            val reservations = repository.getReservationsForObjet(objetId)
            val fermetures = repository.getFermetures()
            _bookedRanges.value = reservations.map { Pair(it.dateDebut, it.dateFin) } +
                fermetures.map { Pair(it.dateDebut, it.dateFin) }
        }
    }

    fun createReservation(objetId: Int, lieuId: Int, date: String, nbSemaines: Int = 1) {
        viewModelScope.launch {
            val success = repository.createReservation(objetId, lieuId, date, nbSemaines)
            _reservationResult.value = success
        }
    }
}
