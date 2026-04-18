package com.app.findback.domain.models

import java.util.UUID

data class CircleZone(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val centerLat: Double,
    val centerLon: Double,
    val radius: Double,
    val name: String = "",
    val createdAt: Long = 0L

) {
   companion object {
       //copy with
       fun CircleZone.copyWith(
           id: String = this.id,
           userId: String = this.userId,
           centerLat: Double = this.centerLat,
           centerLon: Double = this.centerLon,
           radius: Double = this.radius,
           name: String = this.name,
           createdAt: Long = this.createdAt
           ): CircleZone {
           return CircleZone(
               id = id,
               userId = userId,
               centerLat = centerLat,
               centerLon = centerLon,
               radius = radius,
               name = name,
               createdAt = createdAt
           )
       }
       fun fromMap(map: Map<String, Any?>): CircleZone {
           return CircleZone(
               id = map["id"] as? String ?: UUID.randomUUID().toString(),
               userId = map["userId"] as? String ?: "",
               centerLat = (map["centerLat"] as Number).toDouble(),
               centerLon = (map["centerLon"] as Number).toDouble(),
               radius = (map["radius"] as Number).toDouble(),
               name = map["name"] as? String ?: "",
               createdAt = (map["createdAt"] as? Number)?.toLong()
                   ?: 0L
           )
       }
   }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "centerLat" to centerLat,
            "centerLon" to centerLon,
            "radius" to radius,
            "name" to name,
            "createdAt" to createdAt
        )
    }
}