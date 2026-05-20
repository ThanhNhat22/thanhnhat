package com.app.findback.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.findback.R
import com.app.findback.databinding.FragmentNotificationsBinding
import com.app.findback.ui.activities.ChatActivity
import com.app.findback.ui.adapters.NotificationAdapter
import com.app.findback.ui.components.toolbar.ToolbarConfig
import com.app.findback.ui.components.toolbar.ToolbarConfigProvider
import com.app.findback.ui.viewmodels.NotificationViewModel
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment(), ToolbarConfigProvider {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter { notification ->
            viewModel.markAsRead(notification.id)


            when (notification.type) {
                "message" -> {
                    val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                        putExtra(ChatActivity.EXTRA_CONVERSATION_ID, notification.conversationId)
                        putExtra(ChatActivity.EXTRA_OTHER_USER_ID, notification.senderId)
                        putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, notification.senderName)
                    }
                    startActivity(intent)
                }

            }
        }

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NotificationsFragment.adapter
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.notifications.collect { list ->
                adapter.submitList(list)
                binding.layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.rvNotifications.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun toolbarConfig(): ToolbarConfig {
        return ToolbarConfig(
            titleResId = R.string.notifications,
            isShowSearch = false,
            isBack = true,
            ib1Res = null,
            ib2Res = null,
            onIB1 = null,
            onIB2 = null
        )
    }
}