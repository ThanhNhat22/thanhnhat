package com.app.findback.data.source.remote

import com.app.findback.domain.models.ChatMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseChatAiDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("chat_ai")
    private var listener: ValueEventListener? = null

    // TẠO SESSION (thực ra chỉ cần tạo node user)
    fun createChatSession(userId: String) {
        database.child(userId).child("messages").setValue(emptyMap<String, Any>())
    }

    // LƯU MESSAGE (user hoặc AI)
    fun sendMessage(
        userId: String,
        message: ChatMessage,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val messageId = database.push().key ?: return

        val data = mapOf(
            "id" to messageId,
            "content" to message.content,
            "isUser" to message.isUser,
            "timestamp" to message.timestamp
        )

        database.child(userId)
            .child("messages")
            .child(messageId)
            .setValue(data)
            .addOnCompleteListener {
                onComplete(it.isSuccessful)
            }
    }

    // LISTEN REALTIME
    fun getMessages(
        userId: String,
        onChange: (List<ChatMessage>) -> Unit
    ) {
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull {
                    (it.value as? Map<String, Any?>)?.let(ChatMessage::fromMap)
                }.sortedBy { it.timestamp }
                onChange(list)
            }

            override fun onCancelled(p0: DatabaseError) {}
        }

        database.child(userId)
            .child("messages")
            .addValueEventListener(listener!!)
    }

    // UPDATE MESSAGE (ít dùng nhưng có)
    fun updateMessage(
        userId: String,
        message: ChatMessage
    ) {
        database.child(userId)
            .child("messages")
            .child(message.id)
            .setValue(message)
    }

    // stop listen
    fun removeListener(userId: String) {
        listener?.let {
            database.child(userId)
                .child("messages")
                .removeEventListener(it)
        }
    }
}