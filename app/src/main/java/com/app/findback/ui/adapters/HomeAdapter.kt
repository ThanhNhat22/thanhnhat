package com.app.findback.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.findback.R
import com.app.findback.databinding.ItemPostBinding
import com.app.findback.domain.models.Post
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeAdapter(
    private val context: Context,
    private val list: MutableList<Post>
) : RecyclerView.Adapter<HomeAdapter.MyViewHolder>() {

    // ===== CLICK LISTENER =====

    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {

        fun onItemClick(position: Int)

        fun onItemClickShare(position: Int)

        fun onItemClickChat(position: Int)
    }

    fun setOnItemClickListener(
        listener: OnItemClickListener
    ) {

        this.onItemClickListener = listener
    }

    // ===== VIEW HOLDER =====

    inner class MyViewHolder(
        val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        var pos: Int = 0

        init {

            binding.root.setOnClickListener {

                onItemClickListener?.onItemClick(pos)
            }

            binding.btnShare.setOnClickListener {

                onItemClickListener?.onItemClickShare(pos)
            }

            binding.btnChat.setOnClickListener {

                onItemClickListener?.onItemClickChat(pos)
            }
        }
    }

    // ===== ADAPTER METHODS =====

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )

        return MyViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {

        holder.pos = position

        val post = list[position]

        with(holder.binding) {

            // ===== TEXT =====

            tvTitle.text = post.title

            tvDescription.text = post.description

            tvLocation.text =
                post.locationText.ifEmpty {
                    "Không rõ vị trí"
                }

            // ===== DEFAULT USER =====

            tvName.text = "Đang tải..."

            imgAvatar.setImageResource(
                R.drawable.logo_tran
            )

            // ===== LOAD USER =====

            if (post.userId.isNotEmpty()) {

                FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(post.userId)
                    .get()

                    .addOnSuccessListener { snapshot ->

                        val fullName =
                            snapshot.child("fullName")
                                .value?.toString()
                                ?: "Ẩn danh"

                        val avatar =
                            snapshot.child("avatar")
                                .value?.toString()
                                ?: ""

                        tvName.text = fullName

                        if (avatar.isNotEmpty()) {

                            Glide.with(context)
                                .load(avatar)
                                .placeholder(
                                    R.drawable.logo_tran
                                )
                                .circleCrop()
                                .into(imgAvatar)
                        }
                    }

                    .addOnFailureListener {

                        tvName.text = "Ẩn danh"
                    }

            } else {

                tvName.text = "Ẩn danh"
            }

            // ===== TIME =====

            tvTime.text =
                getRelativeTime(post.createdAt)

            tvTimePerfrom.visibility =
                View.GONE

            // ===== STATUS =====

            val isLost =
                post.postType == "lost"

            tvStatus.text =
                if (isLost)
                    "Thất lạc"
                else
                    "Tìm thấy"

            tvStatus.setTextColor(
                context.getColor(
                    if (isLost)
                        R.color.primary_red
                    else
                        R.color.primary_blue
                )
            )

            // ===== HOME BUTTONS =====

            // HIỆN
            btnShare.visibility = View.VISIBLE
            btnChat.visibility = View.VISIBLE

            // ẨN
            btnEdit.visibility = View.GONE
            btnDelete.visibility = View.GONE

            // ===== IMAGES =====

            if (post.imageUrls.isNotEmpty()) {

                rvPostImages.apply {

                    visibility = View.VISIBLE

                    layoutManager =
                        LinearLayoutManager(
                            context,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )

                    adapter =
                        PostImageAdapter(
                            post.imageUrls
                        )

                    setHasFixedSize(true)
                }

            } else {

                rvPostImages.visibility =
                    View.GONE
            }
        }
    }

    // ===== DATA =====

    fun addNewData(newData: List<Post>) {

        list.clear()

        list.addAll(newData)

        notifyDataSetChanged()
    }

    fun clearData() {

        list.clear()

        notifyDataSetChanged()
    }

    // ===== HELPER =====

    private fun getRelativeTime(
        createdAt: Long
    ): String {

        if (createdAt <= 0L)
            return "Vừa đăng"

        val diff =
            System.currentTimeMillis() - createdAt

        val minutes =
            diff / (1000 * 60)

        val hours =
            diff / (1000 * 60 * 60)

        val days =
            diff / (1000 * 60 * 60 * 24)

        return when {

            minutes < 1 ->
                "Vừa đăng"

            minutes < 60 ->
                "$minutes phút trước"

            hours < 24 ->
                "$hours giờ trước"

            days < 7 ->
                "$days ngày trước"

            else ->

                SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).format(Date(createdAt))
        }
    }
}