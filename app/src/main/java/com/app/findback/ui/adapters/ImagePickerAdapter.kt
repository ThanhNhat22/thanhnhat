package com.app.findback.ui.adapters
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.findback.R
import android.widget.ImageView
import android.widget.LinearLayout

class ImagePickerAdapter(
    private val images: MutableList<Uri>,
    private val onAddClick: () -> Unit,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<ImagePickerAdapter.ImageViewHolder>() {

    override fun getItemCount(): Int = images.size + 1

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val layoutAdd: LinearLayout =
            view.findViewById(R.id.layoutAdd)

        val imgPreview: ImageView =
            view.findViewById(R.id.imgPreview)

        val btnRemove: ImageView =
            view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_picker, parent, false)

        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        // Add Button
        if (position == images.size) {

            holder.layoutAdd.visibility = View.VISIBLE
            holder.imgPreview.visibility = View.GONE
            holder.btnRemove.visibility = View.GONE

            holder.layoutAdd.setOnClickListener {
                onAddClick()
            }

        } else {

            holder.layoutAdd.visibility = View.GONE
            holder.imgPreview.visibility = View.VISIBLE
            holder.btnRemove.visibility = View.VISIBLE

            holder.imgPreview.setImageURI(images[position])

            holder.btnRemove.setOnClickListener {
                onRemoveClick(position)
            }
        }
    }

    fun addImages(newImages: List<Uri>) {
        images.addAll(newImages)
        notifyDataSetChanged()
    }

    fun removeImage(position: Int) {
        images.removeAt(position)
        notifyDataSetChanged()
    }

    fun clear() {
        images.clear()
        notifyDataSetChanged()
    }

    fun getImages(): List<Uri> = images
}