package com.app.findback.data.source.remote

import com.app.findback.domain.models.*
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.min
import kotlin.math.max

class FirebaseMessageDataSource {

    private val db = FirebaseDatabase.getInstance().reference

    // ─── Messages ────────────────────────────────────────────────────────────

    fun getMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val ref = db.child("messages").child(conversationId)
            .orderByChild("timestamp")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.toMessage() }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun sendMessage(message: Message) {
        val convRef = db.child("messages").child(message.conversationId)
        val msgId = message.messageId.ifEmpty { convRef.push().key ?: return }
        val finalMsg = message.copy(messageId = msgId)

        convRef.child(msgId).setValue(finalMsg.toMap()).await()
        updateConversation(finalMsg)
    }

    private suspend fun updateConversation(message: Message) {
        val convId = message.conversationId
        val convRef = db.child("conversations").child(convId)

        // Luôn sắp xếp user1 < user2 để tránh đảo chiều
        val user1 = minOf(message.senderId, message.receiverId)
        val user2 = maxOf(message.senderId, message.receiverId)

        val lastMsgText = when (message.type) {
            MessageType.TEXT -> message.content
            MessageType.LOCATION -> "Đã gửi vị trí"
            MessageType.POST -> "Đã gửi bài đăng"
        }

        val updates = mapOf<String, Any?>(
            "conversationId" to convId,
            "user1Id" to user1,
            "user2Id" to user2,
            "lastMessage" to lastMsgText,
            "lastMessageType" to message.type.name,
            "lastMessageTime" to message.timestamp,
            "lastMessageSenderId" to message.senderId
            // "createdAt" có thể set chỉ lần đầu nếu cần
        )

        convRef.updateChildren(updates).await()

        // Tăng unread cho người nhận (không tăng cho chính mình)
        if (message.receiverId != message.senderId) {
            convRef.child("unread_${message.receiverId}")
                .setValue(ServerValue.increment(1)).await()
        }
    }

    // ─── Conversations ────────────────────────────────────────────────────────

    fun getConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val ref = db.child("conversations")
            .orderByChild("lastMessageTime")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children
                    .mapNotNull { it.toConversation(userId) }
                    .filter { conv ->
                        (conv.user1Id == userId || conv.user2Id == userId) &&
                                !isDeletedByUser(snapshot.child(conv.conversationId), userId)
                    }
                    .sortedByDescending { it.lastMessageTime }

                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private fun isDeletedByUser(snapshot: DataSnapshot, userId: String): Boolean {
        return snapshot.child("deleted_$userId").getValue(Boolean::class.java) ?: false
    }

    suspend fun markAsRead(conversationId: String, userId: String) {
        db.child("conversations")
            .child(conversationId)
            .child("unread_$userId")
            .setValue(0).await()
    }

    suspend fun deleteConversation(conversationId: String, userId: String) {
        val convRef = db.child("conversations").child(conversationId)

        convRef.child("deleted_$userId").setValue(true).await()

        val snapshot = convRef.get().await()
        val map = snapshot.value as? Map<*, *> ?: return

        val u1 = map["user1Id"] as? String ?: ""
        val u2 = map["user2Id"] as? String ?: ""

        val otherId = if (u1 == userId) u2 else u1
        val otherDeleted = map["deleted_$otherId"] as? Boolean ?: false

        if (otherDeleted) {
            convRef.removeValue().await()
            db.child("messages").child(conversationId).removeValue().await()
        }
    }

    fun getOrCreateConversationId(uid1: String, uid2: String): String {
        val sorted = listOf(uid1, uid2).sorted()
        return "${sorted[0]}_${sorted[1]}"
    }

    // ─── Mapper ─────────────────────────────────────────────────────────────

    private fun DataSnapshot.toMessage(): Message? {
        return try {
            val map = value as? Map<*, *> ?: return null
            val locMap = map["location"] as? Map<*, *>
            val postMap = map["post"] as? Map<*, *>

            Message(
                messageId = map["messageId"] as? String ?: key ?: "",
                conversationId = map["conversationId"] as? String ?: "",
                senderId = map["senderId"] as? String ?: "",
                receiverId = map["receiverId"] as? String ?: "",
                type = MessageType.valueOf(map["type"] as? String ?: "TEXT"),
                content = map["content"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L,
                isRead = map["isRead"] as? Boolean ?: false,
                location = locMap?.let {
                    MessageLocation(
                        latitude = (it["latitude"] as? Double) ?: 0.0,
                        longitude = (it["longitude"] as? Double) ?: 0.0,
                        address = it["address"] as? String ?: ""
                    )
                },
                post = postMap?.let {
                    MessagePost(
                        postId = it["postId"] as? String ?: "",
                        title = it["title"] as? String ?: "",
                        imageUrl = it["imageUrl"] as? String ?: "",
                        description = it["description"] as? String ?: ""
                    )
                }
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun DataSnapshot.toConversation(currentUserId: String): Conversation? {
        return try {
            val map = value as? Map<*, *> ?: return null
            val convId = map["conversationId"] as? String ?: key ?: ""

            val u1 = map["user1Id"] as? String ?: ""
            val u2 = map["user2Id"] as? String ?: ""

            val unread = (map["unread_$currentUserId"] as? Number)?.toInt() ?: 0

            val otherUserName = map["otherUserName"] as? String
                ?: if (u1 == currentUserId) u2 else u1

            Conversation(
                conversationId = convId,
                user1Id = u1,
                user2Id = u2,
                otherUserName = otherUserName,
                otherUserAvatar = map["otherUserAvatar"] as? String ?: "",
                lastMessage = map["lastMessage"] as? String ?: "",
                lastMessageType = MessageType.valueOf(
                    map["lastMessageType"] as? String ?: "TEXT"
                ),
                lastMessageTime = (map["lastMessageTime"] as? Number)?.toLong() ?: 0L,
                unreadCount = unread,
                lastMessageSenderId = map["lastMessageSenderId"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun Message.toMap(): Map<String, Any?> = mapOf(
        "messageId" to messageId,
        "conversationId" to conversationId,
        "senderId" to senderId,
        "receiverId" to receiverId,
        "type" to type.name,
        "content" to content,
        "timestamp" to timestamp,
        "isRead" to isRead,
        "location" to location?.let {
            mapOf("latitude" to it.latitude, "longitude" to it.longitude, "address" to it.address)
        },
        "post" to post?.let {
            mapOf(
                "postId" to it.postId,
                "title" to it.title,
                "imageUrl" to it.imageUrl,
                "description" to it.description
            )
        }
    )
}