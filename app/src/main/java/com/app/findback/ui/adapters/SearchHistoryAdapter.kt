package com.app.findback.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.findback.databinding.ItemSearchHistoryBinding
import com.app.findback.domain.models.Post
import com.app.findback.domain.models.SearchHistory

class SearchHistoryAdapter(private val context: Context,private var list: MutableList<SearchHistory>) : RecyclerView.Adapter<SearchHistoryAdapter.MyViewHolder>()  {

    private var onItemClickListener: OnItemClickListener? = null
    //định nghĩa interface
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemClickDelete(position: Int)
    }

    fun setOnItemClickListener(onItemClickListener: SearchHistoryAdapter.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }
    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): MyViewHolder {
        val binding = ItemSearchHistoryBinding.inflate(LayoutInflater.from(context), p0, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(
        viewHolder: MyViewHolder,
        position: Int
    ) {
        viewHolder.pos = position

        val searchHistory = list.get(position)
        viewHolder.binding.tvSearchQuery.text = searchHistory.content
    }
    fun addNewData(newData: List<SearchHistory>) {
        list.clear()
        list.addAll(newData)
        notifyDataSetChanged()
    }
    fun addData(data : SearchHistory) {
        list.add(data)
        notifyDataSetChanged()
    }
    fun removeData(data: SearchHistory) {
        list.remove(data)
        notifyDataSetChanged()
    }
    fun clearData() {
        list.clear()
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return list.size
    }


    inner class MyViewHolder(val binding: ItemSearchHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        var pos:Int = 0

        init {
            binding.root.setOnClickListener {
                onItemClickListener?.onItemClick(pos)
            }
            binding.ivDeleteHistory.setOnClickListener {
                onItemClickListener?.onItemClickDelete(pos)
            }
        }
    }
}