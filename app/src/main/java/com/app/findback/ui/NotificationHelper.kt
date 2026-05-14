package com.app.findback.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.app.findback.R
import com.app.findback.ui.activities.ChatActivity

object NotificationHelper {

    private const val CHANNEL_ID = "chat_channel"
    private const val CHANNEL_NAME = "Tin nhắn"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Tin nhắn chat"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    fun showChatNotification(
        context: Context,
        conversationId: String,
        senderName: String,
        content: String,
        avatarUrl: String? = null,
        otherUserId: String
    ) {
        createNotificationChannel(context)

        val notificationId = conversationId.hashCode()

        // Intent khi click vào notification
        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversationId)
            putExtra(ChatActivity.EXTRA_OTHER_USER_ID, otherUserId)
            putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, senderName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val contentView = RemoteViews(context.packageName, R.layout.item_notification).apply {
            setTextViewText(R.id.tvTitle, senderName)
            setTextViewText(R.id.tvContent, content)
            setTextViewText(R.id.tvTime, "Vừa xong")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setCustomContentView(contentView)
            .setCustomBigContentView(contentView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}