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
    private val currentUserId : String,
    private val onClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemConversationBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: Conversation) {
            val otherName = item.getOtherUserName(currentUserId)
            val otherAvatar = item.getOtherUserAvatar(currentUserId)
            // Hiển thị tên người dùng
            b.tvName.text = if ( otherName.isNotBlank() &&  otherName.length < 30)
                otherName  else "Người dùng"


            // Last message
            b.tvLastMessage.text = when (item.lastMessageType) {
                MessageType.TEXT -> item.lastMessage.ifEmpty { "Tin nhắn mới" }
                MessageType.LOCATION -> " Đã gửi vị trí"
                MessageType.POST -> " Đã gửi bài đăng"
            }

            // Thời gian
            b.tvTime.text = item.lastMessageTime.toTimeString()

            // Unread count
            if (item.unreadCount > 0) {
                b.tvUnreadCount.text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString()
                b.tvUnreadCount.visibility = android.view.View.VISIBLE
            } else {
                b.tvUnreadCount.visibility = android.view.View.GONE
            }

            // Avatar
            Glide.with(b.ivAvatar.context)
                .load(otherAvatar.ifEmpty { null })
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

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    private fun Long.toTimeString(): String {
        if (this == 0L) return ""

        val now = System.currentTimeMillis()
        val diff = now - this

        return when {
            diff < 60_000 -> "Vừa xong"
            diff < 3_600_000 -> "${diff / 60_000} phút"
            diff < 86_400_000 -> "${diff / 3_600_000} giờ"
            else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(this))
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Conversation>() {
            override fun areItemsTheSame(old: Conversation, new: Conversation) =
                old.conversationId == new.conversationId

            override fun areContentsTheSame(old: Conversation, new: Conversation) = old == new
        }
    }
}