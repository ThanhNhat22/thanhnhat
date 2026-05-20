package com.app.findback.PopUpNotifications

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import com.app.findback.R
import com.app.findback.databinding.BannerInAppNotificationBinding
import com.app.findback.domain.models.Notification
import com.bumptech.glide.Glide


class InAppNotificationBanner(
    private val activity: Activity,
    private val notification: Notification,
    private val onClick: (Notification) -> Unit
) {

    companion object {
        private const val ANIMATION_DURATION_MS = 300L
        private const val AUTO_DISMISS_MS = 4000L
    }

    private var binding: BannerInAppNotificationBinding? = null
    private var rootView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isDismissed = false

    private val autoDismissRunnable = Runnable { dismiss() }

    fun show() {
        if (activity.isFinishing || activity.isDestroyed) return

        val inflater = LayoutInflater.from(activity)
        binding = BannerInAppNotificationBinding.inflate(inflater)
        rootView = binding!!.root

        // Add vào DecorView (lớp trên cùng của Activity)
        val decorView = activity.window.decorView as ViewGroup
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        ).apply {
            // Margin để tránh status bar — lấy status bar height
            topMargin = getStatusBarHeight()
            leftMargin = 16.dpToPx()
            rightMargin = 16.dpToPx()
        }
        decorView.addView(rootView, params)

        bindData()
        animateIn()

        // Auto dismiss
        handler.postDelayed(autoDismissRunnable, AUTO_DISMISS_MS)

        // Swipe up để dismiss
        rootView?.setOnTouchListener(SwipeUpDismissTouchListener(rootView!!) {
            dismiss()
        })
    }

    private fun bindData() {
        val b = binding ?: return

        b.tvSenderName.text = notification.senderName.ifEmpty { notification.title }
        b.tvContent.text = notification.content

        if (notification.senderAvatar.isNotEmpty()) {
            Glide.with(activity)
                .load(notification.senderAvatar)
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar) // thay bằng drawable có sẵn
                .into(b.ivAvatar)
        }

        b.root.setOnClickListener {
            dismiss()
            onClick(notification)
        }
    }

    private fun animateIn() {
        val view = rootView ?: return
        view.translationY = -500f
        view.alpha = 0f
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(ANIMATION_DURATION_MS)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun dismiss() {
        if (isDismissed) return
        isDismissed = true
        handler.removeCallbacks(autoDismissRunnable)

        val view = rootView ?: return
        view.animate()
            .translationY(-view.height.toFloat() - 50f)
            .alpha(0f)
            .setDuration(ANIMATION_DURATION_MS)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                try {
                    (view.parent as? ViewGroup)?.removeView(view)
                } catch (e: Exception) { /* ignore */ }
                binding = null
                rootView = null
            }
            .start()
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) activity.resources.getDimensionPixelSize(resourceId) else 0
    }

    private fun Int.dpToPx(): Int =
        (this * activity.resources.displayMetrics.density).toInt()
}