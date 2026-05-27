package com.app.findback.ui.adapters

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

class MyPostAdapter(
    private val list: MutableList<Post>,
    private val isMyPost: Boolean = false,
    private val onEdit: (Post) -> Unit,
    private val onDelete: (Post) -> Unit
) : RecyclerView.Adapter<MyPostAdapter.MyViewHolder>() {

    class MyViewHolder(
        val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {

        val item = list[position]
        val context = holder.itemView.context

        with(holder.binding) {

            // ===== Nội dung =====

            tvTitle.text = item.title

            tvDescription.text = item.description

            tvLocation.text =
                item.locationText.ifEmpty {
                    "Không rõ vị trí"
                }

            // ===== User =====

            tvName.text = "Đang tải..."

            imgAvatar.setImageResource(R.drawable.logo_tran)

            if (item.userId.isNotEmpty()) {

                FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(item.userId)
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
                                .placeholder(R.drawable.logo_tran)
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

            // ===== Time =====

            tvTime.text = getRelativeTime(item.createdAt)

            tvTimePerfrom.visibility = View.GONE

            // ===== Status =====

            val isLost = item.postType == "lost"

            tvStatus.text =
                if (isLost)
                    context.getString(R.string.lost)
                else
                    context.getString(R.string.found)

            tvStatus.setTextColor(
                context.getColor(
                    if (isLost)
                        R.color.primary_red
                    else
                        R.color.primary_blue
                )
            )

            // ===== Buttons =====

            // ===== Buttons =====

            if (isMyPost) {

                // PROFILE → hiện Sửa/Xóa

                btnShare.visibility = View.GONE
                btnChat.visibility = View.GONE

                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE

                btnEdit.setOnClickListener {
                    onEdit(item)
                }

                btnDelete.setOnClickListener {
                    onDelete(item)
                }

            } else {

                // HOME → hiện Chia sẻ/Nhắn tin

                btnShare.visibility = View.VISIBLE
                btnChat.visibility = View.VISIBLE

                btnEdit.visibility = View.GONE
                btnDelete.visibility = View.GONE
            }

            // ===== Images =====

            val images = item.imageUrls

            if (images.isNotEmpty()) {

                rvPostImages.visibility = View.VISIBLE

                rvPostImages.layoutManager =
                    LinearLayoutManager(
                        context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )

                rvPostImages.adapter =
                    PostImageAdapter(images)

                rvPostImages.setHasFixedSize(true)

            } else {

                rvPostImages.visibility = View.GONE
            }
        }
    }

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