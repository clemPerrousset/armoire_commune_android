package fr.larmoirecommune.app.viewmodel

import fr.larmoirecommune.app.model.Objet
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

    private val _tags = MutableLiveData<List<fr.larmoirecommune.app.model.Tag>>()
    val tags: LiveData<List<fr.larmoirecommune.app.model.Tag>> = _tags

    fun loadObjects(available: Boolean, nom: String? = null, tagId: Int? = null) {
        viewModelScope.launch {
            val list = repository.getObjects(available, nom, tagId)
            _objects.value = list
        }
    }

    fun loadTags() {
        viewModelScope.launch {
            _tags.value = repository.getTags()
        }
    }
}
