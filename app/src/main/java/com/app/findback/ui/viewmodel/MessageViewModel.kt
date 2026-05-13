package com.app.findback.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.findback.domain.models.Conversation
import com.app.findback.domain.models.Message
import com.app.findback.domain.models.MessageLocation
import com.app.findback.domain.models.MessagePost
import com.app.findback.domain.models.MessageType
import com.app.findback.domain.repository.MessageRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MessageViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    // Lay truc tiep moi lan goi — tranh cache uid rong
    private val currentUserId: String
        get() {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (uid.isEmpty()) Log.w("MessageViewModel", "currentUserId is empty!")
            return uid
        }

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _sendState = MutableSharedFlow<SendState>()
    val sendState: SharedFlow<SendState> = _sendState.asSharedFlow()

    fun loadConversations() {
        viewModelScope.launch {
            repository.getConversations(currentUserId)
                .catch { /* handle error */ }
                .collect { _conversations.value = it }
        }
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            repository.getMessages(conversationId)
                .catch { /* handle error */ }
                .collect { _messages.value = it }
        }
    }

    fun sendTextMessage(receiverId: String, text: String) {
        if (text.isBlank()) return
        val convId = repository.getOrCreateConversationId(currentUserId, receiverId)
        sendMessage(
            Message(
                conversationId = convId,
                senderId = currentUserId,
                receiverId = receiverId,
                type = MessageType.TEXT,
                content = text.trim(),
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun sendLocationMessage(receiverId: String, location: MessageLocation) {
        val convId = repository.getOrCreateConversationId(currentUserId, receiverId)
        sendMessage(
            Message(
                conversationId = convId,
                senderId = currentUserId,
                receiverId = receiverId,
                type = MessageType.LOCATION,
                content = location.address,
                location = location,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun sendPostMessage(receiverId: String, post: MessagePost) {
        val convId = repository.getOrCreateConversationId(currentUserId, receiverId)
        sendMessage(
            Message(
                conversationId = convId,
                senderId = currentUserId,
                receiverId = receiverId,
                type = MessageType.POST,
                content = post.title,
                post = post,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private fun sendMessage(message: Message) {
        viewModelScope.launch {
            _sendState.emit(SendState.Loading)
            repository.sendMessage(message)
                .onSuccess { _sendState.emit(SendState.Success) }
                .onFailure { _sendState.emit(SendState.Error(it.message ?: "Send failed")) }
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId, currentUserId)
        }
    }

    fun markAsRead(conversationId: String) {
        viewModelScope.launch {
            repository.markAsRead(conversationId, currentUserId)
        }
    }

    fun getConversationId(otherUserId: String): String =
        repository.getOrCreateConversationId(currentUserId, otherUserId)

    sealed class SendState {
        object Loading : SendState()
        object Success : SendState()
        data class Error(val msg: String) : SendState()
    }
}