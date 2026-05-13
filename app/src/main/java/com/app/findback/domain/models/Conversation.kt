package com.app.findback.domain.models

data class Conversation(
    val conversationId: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val otherUserName: String = "",
    val otherUserAvatar: String = "",
    val lastMessage: String = "",
    val lastMessageType: MessageType = MessageType.TEXT,
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0,
    val lastMessageSenderId: String = "",
    val createdAt: Long = 0L
) {
    fun getOtherUserId(currentUserId: String): String {
        return if (user1Id == currentUserId) user2Id else user1Id
    }

    fun isValid(): Boolean {
        return conversationId.isNotEmpty() && (user1Id.isNotEmpty() || user2Id.isNotEmpty())
    }
}