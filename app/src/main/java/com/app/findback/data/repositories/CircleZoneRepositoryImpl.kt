package com.app.findback.data.repositories

import com.app.findback.data.source.remote.FirebaseCircleZoneDataSource
import com.app.findback.domain.models.CircleZone
import com.app.findback.domain.repository.CircleZoneReporisoty

class CircleZoneRepositoryImpl(
    private val firebaseCircleZoneDataSource : FirebaseCircleZoneDataSource = FirebaseCircleZoneDataSource()
) : CircleZoneReporisoty {
    override fun getCircleZonesByUserId(
        onData: (List<CircleZone>) -> Unit,
        userId: String
    ) {
        firebaseCircleZoneDataSource.getCircleZonesByUserId(onData, userId)
    }

    override fun getCircleZones(onData: (List<CircleZone>) -> Unit) {
        firebaseCircleZoneDataSource.getCircleZones(onData)
    }

    override fun removeListener(onSuccess: (Boolean) -> Unit) {
        firebaseCircleZoneDataSource.stopListening()
    }

    override fun createCircleZone(
        onSuccess: (Boolean) -> Unit,
        circleZone: CircleZone,
        userId: String
    ) {
        firebaseCircleZoneDataSource.createCircleZone(onSuccess, circleZone, userId)
    }

    override fun updateCircleZone(
        onSuccess: (Boolean) -> Unit,
        circleZone: CircleZone,
        userId: String
    ) {
        firebaseCircleZoneDataSource.updateCircleZone(onSuccess, circleZone, userId)
    }

    override fun deleteCircleZone(
        onSuccess: (Boolean) -> Unit,
        circleZone: CircleZone,
        userId: String
    ) {
        firebaseCircleZoneDataSource.deleteCircleZone(onSuccess, circleZone, userId)
    }
}