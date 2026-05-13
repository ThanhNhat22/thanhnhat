package com.app.findback.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.findback.R
import com.app.findback.databinding.ItemPostPickerBinding
import com.app.findback.domain.models.Post
import com.bumptech.glide.Glide

class PostPickerAdapter(
    private val onSelect: (Post) -> Unit
) : ListAdapter<Post, PostPickerAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemPostPickerBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(post: Post) {
            b.tvTitle.text = post.title
            b.tvDesc.text = post.description
            Glide.with(b.ivImage.context)
                .load(post.imageUrl.ifEmpty { null })
                .error(R.drawable.ic_post)
                .into(b.ivImage)
            b.root.setOnClickListener { onSelect(post) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemPostPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(h: VH, position: Int) = h.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(a: Post, b: Post) = a.postId == b.postId
            override fun areContentsTheSame(a: Post, b: Post) = a == b
        }
    }
}