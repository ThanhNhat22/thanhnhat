package com.app.findback.domain.repository

import com.app.findback.domain.models.ChatMessage
import com.app.findback.domain.models.ChatSession
import com.app.findback.domain.models.Post

interface GeminiRepository {
    suspend fun sendMessage(message: String,session: ChatSession,posts: List<Post>)
    suspend fun getMessages(userId: String,onData: (List<ChatMessage>) -> Unit)
}