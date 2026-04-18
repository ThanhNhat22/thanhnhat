package com.app.findback.domain.models

data class Post(
	val postId: String = "",
	val userId: Int = 0,
	val postType: String = "lost",
	val title: String = "",
	val description: String = "",
	val itemCategory: String = "",
	val itemBrand: String = "",
	val itemFeatures: String = "",
	val incidentDatetime: String = "",
	val locationText: String = "",
	val latitude: Double = 0.0,
	val longitude: Double = 0.0,
	val status: String = "active",
	val viewCount: Int = 0,
	val contactCount: Int = 0,
	val createdAt: Long = 0L,
	val updatedAt: Long = 0L,
	val expiresAt: Long? = null
) {
	// convert object → Firebase
	fun toMap(): Map<String, Any?> {
		return mapOf(
			"postId" to postId,
			"userId" to userId,
			"postType" to postType,
			"title" to title,
			"description" to description,
			"itemCategory" to itemCategory,
			"incidentDatetime" to incidentDatetime,
			"locationText" to locationText,
			"latitude" to latitude,
			"longitude" to longitude,
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

				userId = (map["userId"] as? Number)?.toInt() ?: 0,

				postType = map["postType"] as? String ?: "lost",
				title = map["title"] as? String ?: "",
				description = map["description"] as? String ?: "",

				itemCategory = map["itemCategory"] as? String ?: "",

				incidentDatetime = map["incidentDatetime"] as? String ?: "",
				locationText = map["locationText"] as? String ?: "",

				latitude = (map["latitude"] as? Number)?.toDouble() ?: 0.0,
				longitude = (map["longitude"] as? Number)?.toDouble() ?: 0.0,

				status = map["status"] as? String ?: "active",

				viewCount = (map["viewCount"] as? Number)?.toInt() ?: 0,
				contactCount = (map["contactCount"] as? Number)?.toInt() ?: 0,

				createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
				updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L,
			)
		}
	}
}