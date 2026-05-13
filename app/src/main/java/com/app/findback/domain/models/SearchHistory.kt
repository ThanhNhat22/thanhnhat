package com.app.findback.domain.models

data class SearchHistory (
    val id: String = "",
    val userId: String = "",
    val content:String ="",
    val createdAt: String = "",
    val updatedAt: String = ""
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "content" to content,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
        )
    }
    fun copyWith(
        id: String? = null,
        userId: String? = null,
        content: String? = null,
        createdAt: String? = null,
        updatedAt: String? = null
    ) = SearchHistory(
        id = id ?: this.id,
        userId = userId ?: this.userId,
        content = content ?: this.content,
        createdAt = createdAt ?: this.createdAt,
        updatedAt = updatedAt ?: this.updatedAt
    )


    companion object {
        fun fromMap(map: Map<String, Any?>): SearchHistory {
            return SearchHistory(
                id = map["id"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                content = map["content"] as? String ?: "",
                createdAt = map["createdAt"] as? String ?: "",
                updatedAt = map["updatedAt"] as? String ?: "",
            )
        }
    }
}
