package com.app.findback.domain.repository

import com.app.findback.domain.models.Conversation
import com.app.findback.domain.models.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessages(conversationId: String): Flow<List<Message>>
    fun getConversations(userId: String): Flow<List<Conversation>>
    suspend fun sendMessage(message: Message): Result<Unit>
    suspend fun markAsRead(conversationId: String, userId: String): Result<Unit>
    suspend fun deleteConversation(conversationId: String, userId: String): Result<Unit>
    fun getOrCreateConversationId(userId1: String, userId2: String): String
}