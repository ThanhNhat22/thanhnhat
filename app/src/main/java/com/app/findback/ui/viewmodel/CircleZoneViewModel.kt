package com.app.findback.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.findback.data.repositories.CircleZoneRepositoryImpl
import com.app.findback.domain.models.CircleZone
import com.app.findback.domain.repository.CircleZoneReporisoty

class CircleZoneViewModel : ViewModel() {
    private val circleZoneRepository: CircleZoneReporisoty = CircleZoneRepositoryImpl()
    private val _circleZones = MutableLiveData<List<CircleZone>>(emptyList())
    val circleZones: LiveData<List<CircleZone>> = _circleZones

    fun fetchCircleZonesByUserId(userId: String){
        circleZoneRepository.getCircleZonesByUserId({ circleZones ->
            _circleZones.postValue(circleZones)
        }, userId)
    }


    fun createCircleZone(circleZone: CircleZone, onSuccess: (Boolean) -> Unit) {
        circleZoneRepository.createCircleZone(onSuccess, circleZone, circleZone.userId)
    }
    fun updateCircleZone(circleZone: CircleZone, userId: String, onSuccess: (Boolean) -> Unit) {
        circleZoneRepository.updateCircleZone(onSuccess, circleZone, userId)
    }
    fun deleteCircleZone(circleZone: CircleZone, userId: String, onSuccess: (Boolean) -> Unit) {
        circleZoneRepository.deleteCircleZone(onSuccess, circleZone, userId)
    }
    fun removeListener() {
        circleZoneRepository.removeListener(onSuccess = {})
    }
    override fun onCleared() {
        circleZoneRepository.removeListener(onSuccess = {})
        super.onCleared()
    }
}

