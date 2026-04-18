package com.app.findback.data.repositories

import com.app.findback.data.source.gemini.GeminiChatService
import com.app.findback.data.source.remote.FirebaseChatAiDataSource
import com.app.findback.domain.models.ChatMessage
import com.app.findback.domain.models.ChatSession
import com.app.findback.domain.models.Post
import com.app.findback.domain.repository.GeminiRepository

class GeminiRepositoryImpl : GeminiRepository {
    private val apiService = GeminiChatService()
    private val firebaseChatAiDataSource = FirebaseChatAiDataSource()
    override suspend fun sendMessage(message: String, session: ChatSession, posts: List<Post>){
        apiService.sendMessage(session, message, posts)
    }
    override suspend fun getMessages(userId: String, onData: (List<ChatMessage>) -> Unit) {
        firebaseChatAiDataSource.getMessages(userId,onData)
    }
}