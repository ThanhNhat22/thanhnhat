package com.app.findback.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.findback.R
import com.app.findback.databinding.ItemPostBinding
import com.app.findback.domain.models.Post
import com.app.findback.utils.extentions.ConvertTime

class HomeAdapter(private val context: Context, private val list: MutableList<Post>) : RecyclerView.Adapter<HomeAdapter.MyViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null
    //định nghĩa interface
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemClickShare(position: Int)
        fun onItemClickChat(position: Int)
    }
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        holder.pos = position

        val post = list.get(position)
        holder.binding.tvName.text = "Demo"
        holder.binding.tvTime.text = ConvertTime.formatTime(post.incidentDatetime)
        if (post.postType == "lost") {
            holder.binding.tvStatus.text = "Thất lạc"
            holder.binding.tvStatus.setTextColor(context.resources.getColor(R.color.primary_red))
        } else {
            holder.binding.tvStatus.text = "Tìm thấy"
            holder.binding.tvStatus.setTextColor(context.resources.getColor(R.color.primary_green))
        }
        holder.binding.tvTitle.text = post.title
        holder.binding.tvDescription.text = post.description
        holder.binding.tvLocation.text = post.locationText

    }

    override fun getItemCount(): Int {
        return  list.size
    }

    fun addNewData(newData: List<Post>) {
        list.clear()
        list.addAll(newData)
        notifyDataSetChanged()
    }
    fun clearData() {
        list.clear()
        notifyDataSetChanged()
    }


    inner class MyViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
       var pos:Int = 0
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
}