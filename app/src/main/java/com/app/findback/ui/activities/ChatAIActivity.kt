package com.app.findback.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import com.app.findback.BaseActivity
import com.app.findback.R
import com.app.findback.databinding.ActivityChatAiActivityBinding
import com.app.findback.ui.components.chatbox.ChatFragment

class ChatAIActivity : BaseActivity() {
    private lateinit var binding: ActivityChatAiActivityBinding
    private val chatFragment = ChatFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatAiActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setKeybroad()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.chatContainer, chatFragment, "chatFragment")
                .commit()
        }
        setControl()
        setEvent()
    }
    //set control
    private fun setControl() {

    }

    private fun setEvent(){
        setupToolbarCus(
            toolbar = binding.toolbarLayout.toolbar,
            title = getString(R.string.ai_chat),
            isShowSearch = false,
            isBack = true,
            onBack = {
                setKeybroad()
            }
        )
    }
}