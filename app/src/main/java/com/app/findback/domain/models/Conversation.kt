package com.app.findback.domain.models

data class Conversation(
    val conversationId: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val user1Name: String = "",
    val user2Name: String = "",
    val user1Avatar: String = "",
    val user2Avatar: String = "",
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


    fun getOtherUserName(currentUserId: String): String {
        return if (user1Id == currentUserId) user2Name else user1Name
    }


    fun getOtherUserAvatar(currentUserId: String): String {
        return if (user1Id == currentUserId) user2Avatar else user1Avatar
    }

    fun isValid(): Boolean {
        return conversationId.isNotEmpty() && (user1Id.isNotEmpty() || user2Id.isNotEmpty())
    }
}