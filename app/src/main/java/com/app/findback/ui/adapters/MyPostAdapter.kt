package com.app.findback.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.findback.databinding.ItemPostBinding
import com.app.findback.domain.models.Post

class MyPostAdapter(
    private val list: MutableList<Post>
) : RecyclerView.Adapter<MyPostAdapter.MyViewHolder>() {

    inner class MyViewHolder(
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

        holder.binding.tvTitle.text = item.title

        holder.binding.tvDescription.text =
            item.description

        holder.binding.tvLocation.text =
            item.locationText

        holder.binding.tvTimePerfrom.text =
            item.incidentDatetime

        holder.binding.tvStatus.text =
            if (item.postType == "lost")
                "Thất lạc"
            else
                "Đã nhặt được"

        holder.binding.tvName.text =
            item.userName.ifEmpty { "Người dùng" }

        holder.binding.tvTime.text =
            item.locationText

    }
}