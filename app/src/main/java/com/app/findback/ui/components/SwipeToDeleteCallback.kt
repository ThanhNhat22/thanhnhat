package com.app.findback.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.findback.R

class SwipeToDeleteCallback(
    private val context: Context,
    private val onSwiped: (position: Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val background = ColorDrawable(Color.parseColor("#FF3B30"))
    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
    private val iconMargin = 48

    // Gioi han keo toi da 120dp
    private val maxSwipeDp = 120f
    private val maxSwipePx = (context.resources.displayMetrics.density * maxSwipeDp)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onSwiped(viewHolder.adapterPosition)
    }


    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 0.3f


    override fun getSwipeVelocityThreshold(defaultValue: Float) = defaultValue * 0.5f
    override fun getSwipeEscapeVelocity(defaultValue: Float) = defaultValue * 4f

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView


        val clampedDx = dX.coerceAtLeast(-maxSwipePx)


        background.setBounds(
            itemView.right + clampedDx.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        background.draw(c)


        val iconVisible = clampedDx < -iconMargin * 2
        if (iconVisible) {
            deleteIcon?.let { icon ->
                val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                val iconBottom = iconTop + icon.intrinsicHeight
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                icon.draw(c)
            }
        }


        super.onChildDraw(c, recyclerView, viewHolder, clampedDx, dY, actionState, isCurrentlyActive)
    }


    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.translationX = 0f
    }
}