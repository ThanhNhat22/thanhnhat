package com.app.findback

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.app.findback.databinding.ActivityMainBinding
import com.app.findback.ui.NotificationHelper
import com.app.findback.ui.activities.BaseBottomNavActivity
import com.app.findback.ui.activities.ChatActivity
import com.onesignal.OneSignal
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.createNotificationChannel(this)

        setupOneSignal()
        setToolbar()
        navigateToMainScreen()
    }

    private fun setupOneSignal() {
        OneSignal.initWithContext(this, "bdf5516d-cd73-4dd2-b189-c429f8311bd5")

        // Click Listener
        OneSignal.Notifications.addClickListener(object :
            com.onesignal.notifications.INotificationClickListener {

            override fun onClick(event: com.onesignal.notifications.INotificationClickEvent) {
                val data = event.notification.additionalData ?: return

                val intent = Intent(this@MainActivity, ChatActivity::class.java).apply {
                    putExtra(ChatActivity.EXTRA_CONVERSATION_ID, data.optString("conversationId", ""))
                    putExtra(ChatActivity.EXTRA_OTHER_USER_ID, data.optString("otherUserId", ""))
                    putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, data.optString("otherUserName", ""))
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
            }
        })

        // Yêu cầu quyền
        lifecycleScope.launch {
            OneSignal.Notifications.requestPermission(true)
        }
    }

    private fun navigateToMainScreen() {
        startActivity(Intent(this, BaseBottomNavActivity::class.java))
        finish()
    }

    private fun setToolbar() {
        setupToolbarCus(
            binding.toolbarLayout.toolbar,
            "Goc tim do",
            false,
            null,
            false,
            R.drawable.logo_tran,
            R.drawable.ic_notification,
            R.drawable.ic_search
        )
    }
}