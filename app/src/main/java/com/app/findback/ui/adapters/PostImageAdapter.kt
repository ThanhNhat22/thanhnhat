package com.app.findback.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.findback.R
import com.app.findback.databinding.ItemPostImageBinding
import com.bumptech.glide.Glide

class PostImageAdapter(
    private val images: List<String>
) : RecyclerView.Adapter<PostImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(
        val binding: ItemPostImageBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageViewHolder {

        val binding = ItemPostImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ImageViewHolder(binding)
    }

    override fun getItemCount() = images.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        val imageUrls = images[position]

        Glide.with(holder.itemView.context)
            .load(imageUrls)
            .placeholder(R.drawable.logo_tran)
            .error(R.drawable.logo_tran)
            .centerCrop()
            .into(holder.binding.imImage)
    }
}