package com.app.findback.domain.models

data class ChatMessage(
    val id: String = "",
    val content: String = "",
    val isUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
){
    companion object{
        fun fromMap(map: Map<String, Any?>): ChatMessage{
            return ChatMessage(
                id = map["id"] as String,
                content = map["content"] as String,
                isUser = map["isUser"] as Boolean,
                timestamp = map["timestamp"] as Long
            )
        }
    }
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "content" to content,
            "isUser" to isUser,
            "timestamp" to timestamp
        )
    }
}