package com.app.findback.data.repositories

import com.app.findback.data.source.remote.FirebaseMessageDataSource
import com.app.findback.domain.models.Conversation
import com.app.findback.domain.models.Message
import com.app.findback.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow

class MessageRepositoryImpl(
    private val dataSource: FirebaseMessageDataSource
) : MessageRepository {

    override fun getMessages(conversationId: String): Flow<List<Message>> =
        dataSource.getMessages(conversationId)

    override fun getConversations(userId: String): Flow<List<Conversation>> =
        dataSource.getConversations(userId)

    override suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            dataSource.sendMessage(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(conversationId: String, userId: String): Result<Unit> {
        return try {
            dataSource.markAsRead(conversationId, userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteConversation(conversationId: String, userId: String): Result<Unit> {
        return try {
            dataSource.deleteConversation(conversationId, userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getOrCreateConversationId(userId1: String, userId2: String): String =
        dataSource.getOrCreateConversationId(userId1, userId2)
}