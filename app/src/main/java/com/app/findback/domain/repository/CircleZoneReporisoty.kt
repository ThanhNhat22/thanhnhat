package com.app.findback.domain.repository

import com.app.findback.domain.models.CircleZone

interface CircleZoneReporisoty {
    fun getCircleZonesByUserId(onData: (List<CircleZone>) -> Unit, userId: String)
    fun getCircleZones(onData: (List<CircleZone>) -> Unit)
    fun removeListener(onSuccess: (Boolean) -> Unit)
    fun createCircleZone(onSuccess: (Boolean) -> Unit, circleZone: CircleZone,userId:String)
    fun updateCircleZone(onSuccess: (Boolean) -> Unit, circleZone: CircleZone,userId:String)
    fun deleteCircleZone(onSuccess: (Boolean) -> Unit, circleZone: CircleZone,userId:String)
}