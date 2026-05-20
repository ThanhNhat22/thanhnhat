package com.app.findback.PopUpNotifications

import android.view.MotionEvent
import android.view.View

class SwipeUpDismissTouchListener(
    private val view: View,
    private val onDismiss: () -> Unit
) : View.OnTouchListener {

    companion object {
        private const val SWIPE_THRESHOLD = 80f // dp
    }

    private var startY = 0f
    private var isDragging = false

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = event.rawY
                isDragging = false
                false // cho phép click vẫn hoạt động
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.rawY - startY
                if (deltaY < -20f) { // đang kéo lên
                    isDragging = true
                    view.translationY = deltaY
                    view.alpha = 1f - (-deltaY / 300f).coerceIn(0f, 1f)
                    true
                } else {
                    false
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val deltaY = event.rawY - startY
                if (isDragging && deltaY < -SWIPE_THRESHOLD) {
                    onDismiss()
                } else if (isDragging) {
                    // Snap back
                    view.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }
                isDragging = false
                false
            }
            else -> false
        }
    }
}