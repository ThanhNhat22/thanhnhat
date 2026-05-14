package com.app.findback.ui.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.findback.databinding.ItemNotificationBinding
import com.app.findback.domain.models.Notification
import com.bumptech.glide.Glide

class NotificationAdapter(
    private val onClick: (Notification) -> Unit
) : ListAdapter<Notification, NotificationAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Notification) {
            binding.tvTitle.text = item.senderName
            binding.tvContent.text = item.content
            binding.viewUnread.visibility = if (!item.isRead) View.VISIBLE else View.GONE

            binding.tvTime.text = DateUtils.getRelativeTimeSpanString(
                item.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )

            if (item.senderAvatar.isNotEmpty()) {
                Glide.with(binding.ivAvatar)
                    .load(item.senderAvatar)
                    .circleCrop()
                    .into(binding.ivAvatar)
            }

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(a: Notification, b: Notification) = a.id == b.id
        override fun areContentsTheSame(a: Notification, b: Notification) = a == b
    }
}