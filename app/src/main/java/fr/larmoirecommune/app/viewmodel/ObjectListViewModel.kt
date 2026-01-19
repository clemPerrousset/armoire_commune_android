package fr.larmoirecommune.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import fr.larmoirecommune.app.model.Objet
import fr.larmoirecommune.app.repository.ObjectRepository
import kotlinx.coroutines.launch

class ObjectListViewModel : ViewModel() {
    private val repository = ObjectRepository()

    private val _objects = MutableLiveData<List<Objet>>()
    val objects: LiveData<List<Objet>> = _objects

    fun loadObjects(available: Boolean) {
        viewModelScope.launch {
            val list = repository.getObjects(available)
            _objects.value = list
        }
    }
}
