package com.app.findback.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.findback.R
import com.app.findback.databinding.ItemMessageLocationBinding
import com.app.findback.databinding.ItemMessagePostBinding
import com.app.findback.databinding.ItemMessageReceivedBinding
import com.app.findback.databinding.ItemMessageSentBinding
import com.app.findback.domain.models.Message
import com.app.findback.domain.models.MessageType
import com.google.firebase.auth.FirebaseAuth
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat
import java.util.*
import com.bumptech.glide.Glide

class ChatAdapter(
    private val currentUserId: String,
    private val onLocationClick: (Double, Double) -> Unit,
    private val onPostClick: (String) -> Unit
) : ListAdapter<Message, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val VIEW_TEXT_SENT     = 0
        private const val VIEW_TEXT_RECEIVED = 1
        private const val VIEW_LOC_SENT      = 2
        private const val VIEW_LOC_RECEIVED  = 3
        private const val VIEW_POST_SENT     = 4
        private const val VIEW_POST_RECEIVED = 5

        val DIFF = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(a: Message, b: Message) = a.messageId == b.messageId
            override fun areContentsTheSame(a: Message, b: Message) = a == b
        }
    }

    // Lay truc tiep tu FirebaseAuth moi lan — tranh truong hop currentUserId truyen vao bi rong
    private val myUid get() = FirebaseAuth.getInstance().currentUser?.uid ?: currentUserId

    private fun isMine(senderId: String): Boolean {
        val uid = myUid
        if (uid.isEmpty()) return false
        return senderId.trim() == uid.trim()
    }

    override fun getItemViewType(position: Int): Int {
        val msg = getItem(position)
        val mine = isMine(msg.senderId)
        Log.d("ChatAdapter", "myUid=$myUid | senderId=${msg.senderId} | isMine=$mine")
        return when (msg.type) {
            MessageType.TEXT     -> if (mine) VIEW_TEXT_SENT     else VIEW_TEXT_RECEIVED
            MessageType.LOCATION -> if (mine) VIEW_LOC_SENT      else VIEW_LOC_RECEIVED
            MessageType.POST     -> if (mine) VIEW_POST_SENT     else VIEW_POST_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TEXT_SENT     -> TextSentVH(ItemMessageSentBinding.inflate(inf, parent, false))
            VIEW_TEXT_RECEIVED -> TextReceivedVH(ItemMessageReceivedBinding.inflate(inf, parent, false))
            VIEW_LOC_SENT      -> LocationVH(ItemMessageLocationBinding.inflate(inf, parent, false), true)
            VIEW_LOC_RECEIVED  -> LocationVH(ItemMessageLocationBinding.inflate(inf, parent, false), false)
            VIEW_POST_SENT     -> PostVH(ItemMessagePostBinding.inflate(inf, parent, false), true)
            else               -> PostVH(ItemMessagePostBinding.inflate(inf, parent, false), false)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is TextSentVH     -> holder.bind(msg)
            is TextReceivedVH -> holder.bind(msg)
            is LocationVH     -> holder.bind(msg)
            is PostVH         -> holder.bind(msg)
        }
    }

    // ─── ViewHolders ─────────────────────────────────────────────────────────

    inner class TextSentVH(private val b: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(msg: Message) {
            b.tvMessage.text = msg.content
            b.tvTime.text = msg.timestamp.toTimeString()
        }
    }

    inner class TextReceivedVH(private val b: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(msg: Message) {
            b.tvMessage.text = msg.content
            b.tvTime.text = msg.timestamp.toTimeString()
        }
    }

    inner class LocationVH(
        private val b: ItemMessageLocationBinding,
        private val isSent: Boolean
    ) : RecyclerView.ViewHolder(b.root) {

        fun bind(msg: Message) {
            val loc = msg.location ?: return
            b.tvAddress.text = loc.address.ifEmpty { "View on map" }
            b.tvTime.text = msg.timestamp.toTimeString()

            // Can bubble dung phia
            val cardParams = b.cardLocation.layoutParams as? ViewGroup.MarginLayoutParams
            if (isSent) {
                (b.root as? LinearLayout)?.gravity = android.view.Gravity.END
                cardParams?.marginStart = 80; cardParams?.marginEnd = 0
            } else {
                (b.root as? LinearLayout)?.gravity = android.view.Gravity.START
                cardParams?.marginStart = 0; cardParams?.marginEnd = 80
            }
            b.cardLocation.layoutParams = cardParams

            Glide.with(b.ivMap.context)
                .load(
                    "https://maps.googleapis.com/maps/api/staticmap" +
                            "?center=${loc.latitude},${loc.longitude}" +
                            "&zoom=15&size=300x150" +
                            "&markers=${loc.latitude},${loc.longitude}" +
                            "&key=YOUR_MAPS_API_KEY"
                )
                .apply(RequestOptions().error(R.drawable.ic_location_placeholder))
                .into(b.ivMap)

            b.cardLocation.setOnClickListener { onLocationClick(loc.latitude, loc.longitude) }
        }
    }

    inner class PostVH(
        private val b: ItemMessagePostBinding,
        private val isSent: Boolean
    ) : RecyclerView.ViewHolder(b.root) {
        fun bind(msg: Message) {
            val post = msg.post ?: return
            b.tvPostTitle.text = post.title
            b.tvPostDesc.text = post.description
            b.tvTime.text = msg.timestamp.toTimeString()
            b.tvSentLabel.text = if (isSent) "You shared a post" else "Shared a post"

            Glide.with(b.ivPostImage.context)
                .load(post.imageUrl.ifEmpty { null })
                .apply(RequestOptions().error(R.drawable.ic_post))
                .into(b.ivPostImage)

            b.cardPost.setOnClickListener { onPostClick(post.postId) }
        }
    }

    private fun Long.toTimeString(): String {
        if (this == 0L) return ""
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))
    }
}