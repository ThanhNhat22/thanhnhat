package com.app.findback.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.findback.R
import com.app.findback.databinding.ItemConversationBinding
import com.app.findback.domain.models.Conversation
import com.app.findback.domain.models.MessageType
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    private val onClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemConversationBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: Conversation) {
            b.tvName.text = item.otherUserName.ifEmpty { "Nguoi dung" }
            b.tvLastMessage.text = when (item.lastMessageType) {
                MessageType.TEXT -> item.lastMessage
                MessageType.LOCATION -> "Gửi vị trí"
                MessageType.POST -> "Gửi bài đăng"
            }
            b.tvTime.text = item.lastMessageTime.toTimeString()

            if (item.unreadCount > 0) {
                b.tvUnreadCount.text = item.unreadCount.toString()
                b.tvUnreadCount.visibility = android.view.View.VISIBLE
            } else {
                b.tvUnreadCount.visibility = android.view.View.GONE
            }

            Glide.with(b.ivAvatar.context)
                .load(item.otherUserAvatar.ifEmpty { null })
                .apply(
                    RequestOptions.circleCropTransform()
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                )
                .into(b.ivAvatar)

            b.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    private fun Long.toTimeString(): String {
        if (this == 0L) return ""
        val now = System.currentTimeMillis()
        val diff = now - this
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m"
            diff < 86_400_000 -> "${diff / 3_600_000}h"
            else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(this))
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Conversation>() {
            override fun areItemsTheSame(a: Conversation, b: Conversation) =
                a.conversationId == b.conversationId
            override fun areContentsTheSame(a: Conversation, b: Conversation) = a == b
        }
    }
}