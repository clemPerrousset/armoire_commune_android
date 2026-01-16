package fr.larmoirecommune.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import fr.larmoirecommune.app.repository.ObjectRepository
import kotlinx.coroutines.launch

class ObjectDetailViewModel : ViewModel() {
    private val repository = ObjectRepository()

    private val _object = MutableLiveData<Objet?>()
    val objectDetail: LiveData<Objet?> = _object

    fun loadObject(id: Int) {
        viewModelScope.launch {
            val obj = repository.getObject(id)
            _object.value = obj
        }
    }
}
