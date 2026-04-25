package com.app.findback.domain.models

data class AiResponse(
    val reply: String,
    val postIds: List<String>
){
    companion object{
        fun fromMap(map: Map<String, Any?>): AiResponse{
            return AiResponse(
                reply = map["reply"] as String,
                postIds = (map["postIds"] as? List<String>) ?: emptyList()
            )
        }
    }
    fun toMap(): Map<String, Any> {
        return mapOf(
            "reply" to reply,
            "postIds" to postIds
        )
    }
}