package com.app.findback.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.app.findback.databinding.ItemMessagePostBinding
import com.app.findback.domain.models.Post

class PostAiAdapter(
    private var items: List<Post> = emptyList(),
    private val onClick: ((Post) -> Unit)? = null
) : RecyclerView.Adapter<PostAiAdapter.PostViewHolder>() {

    fun submitList(list: List<Post>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemMessagePostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class PostViewHolder(private val binding: ItemMessagePostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            // Hiển thị title hoặc fallback
            //binding.textViewMessage.text = post.title.ifBlank { post.locationText.ifBlank { post.postId } }
            itemView.setOnClickListener {
                Log.d("PostAiAdapter", "Post clicked: ${post.postId}")
                onClick?.invoke(post)
            }
        }
    }
}
