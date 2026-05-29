package com.app.findback.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val postType: String = "lost",
    val title: String = "",
    val description: String = "",
    val itemCategory: String = "",
    val itemBrand: String = "",
    val itemFeatures: String = "",
    val incidentDatetime: Long = 0L,
    val locationText: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = "",
    val status: String = "active",
    val imageUrls: List<String> = emptyList(),
    val viewCount: Int = 0,
    val contactCount: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val expiresAt: Long? = null
) : Parcelable {

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "postId" to postId,
            "userId" to userId,
            "userName" to userName,
            "userAvatar" to userAvatar,
            "postType" to postType,
            "title" to title,
            "description" to description,
            "itemCategory" to itemCategory,
            "itemBrand" to itemBrand,
            "itemFeatures" to itemFeatures,
            "incidentDatetime" to incidentDatetime,
            "locationText" to locationText,
            "latitude" to latitude,
            "longitude" to longitude,
            "imageUrl" to imageUrl,
            "imageUrls" to imageUrls,
            "status" to status,
            "viewCount" to viewCount,
            "contactCount" to contactCount,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Post {
            return Post(
                postId = map["postId"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                userName = map["userName"] as? String ?: "",
                userAvatar = map["userAvatar"] as? String ?: "",
                postType = map["postType"] as? String ?: "lost",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                itemCategory = map["itemCategory"] as? String ?: "",
                itemBrand = map["itemBrand"] as? String ?: "",
                itemFeatures = map["itemFeatures"] as? String ?: "",
                incidentDatetime = when (val v = map["incidentDatetime"]) {
                    is Long -> v
                    is Number -> v.toLong()
                    is String -> v.toLongOrNull() ?: 0L
                    else -> 0L
                },
                locationText = map["locationText"] as? String ?: "",
                latitude = (map["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (map["longitude"] as? Number)?.toDouble() ?: 0.0,
                imageUrl = map["imageUrl"] as? String ?: "",
                status = map["status"] as? String ?: "active",
                viewCount = (map["viewCount"] as? Number)?.toInt() ?: 0,
                contactCount = (map["contactCount"] as? Number)?.toInt() ?: 0,
                createdAt = when (val v = map["createdAt"]) {
                    is Long -> v
                    is Number -> v.toLong()
                    is String -> v.toLongOrNull() ?: 0L
                    else -> 0L
                },
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L,
                imageUrls = (map["imageUrls"] as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
            )
        }
    }

    fun isMyPost(currentUserId: String): Boolean = this.userId == currentUserId
}