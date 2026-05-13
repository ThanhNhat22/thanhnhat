package com.app.findback.domain.models

data class Message(
    val messageId: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val type: MessageType = MessageType.TEXT,
    val content: String = "",
    val location: MessageLocation? = null,
    val post: MessagePost? = null,
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)

enum class MessageType {
    TEXT, LOCATION, POST
}

data class MessageLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
)

data class MessagePost(
    val postId: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val description: String = ""
)