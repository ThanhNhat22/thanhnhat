package com.app.findback

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.app.findback.databinding.ActivityMainBinding
import com.app.findback.ui.activities.BaseBottomNavActivity
import com.onesignal.OneSignal
import kotlinx.coroutines.launch
import android.util.Log
import com.onesignal.notifications.INotificationClickListener
import com.onesignal.notifications.INotificationClickEvent
import com.app.findback.ui.activities.ChatActivity


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        OneSignal.initWithContext(
            this,
            "bdf5516d-cd73-4dd2-b189-c429f8311bd5"
        )
        OneSignal.Notifications.addClickListener(
            object : INotificationClickListener {

                override fun onClick(event: INotificationClickEvent) {

                    val data = event.notification.additionalData

                    val conversationId =
                        data?.optString("conversationId", "") ?: ""

                    val otherUserId =
                        data?.optString("otherUserId", "") ?: ""

                    val otherUserName =
                        data?.optString("otherUserName", "") ?: ""

                    val intent = Intent(
                        this@MainActivity,
                        ChatActivity::class.java
                    )

                    intent.putExtra(
                        ChatActivity.EXTRA_CONVERSATION_ID,
                        conversationId
                    )

                    intent.putExtra(
                        ChatActivity.EXTRA_OTHER_USER_ID,
                        otherUserId
                    )

                    intent.putExtra(
                        ChatActivity.EXTRA_OTHER_USER_NAME,
                        otherUserName
                    )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP

                    startActivity(intent)
                }
            }
        )

        lifecycleScope.launch {
            OneSignal.Notifications.requestPermission(true)
        }
        navigateToMainScreen()
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