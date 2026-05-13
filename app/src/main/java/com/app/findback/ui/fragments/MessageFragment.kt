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
import com.app.findback.data.repositories.MessageRepositoryImpl
import com.app.findback.data.source.remote.FirebaseMessageDataSource
import com.app.findback.databinding.FragmentMessageBinding
import com.app.findback.domain.models.Conversation
import com.app.findback.ui.activities.ChatActivity
import com.app.findback.ui.adapters.ConversationAdapter
import com.app.findback.ui.components.toolbar.ToolbarConfig
import com.app.findback.ui.components.toolbar.ToolbarConfigProvider
import com.app.findback.ui.viewmodel.MessageViewModel
import com.app.findback.ui.viewmodel.MessageViewModelFactory
import com.app.findback.ui.components.SwipeToDeleteCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import androidx.recyclerview.widget.ItemTouchHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MessageFragment : Fragment(), ToolbarConfigProvider {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val viewModel: MessageViewModel by viewModels {
        MessageViewModelFactory(MessageRepositoryImpl(FirebaseMessageDataSource()))
    }

    private val adapter by lazy {
        ConversationAdapter { conversation -> openChat(conversation) }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeConversations()
        viewModel.loadConversations()
    }

    private fun setupRecyclerView() {
        binding.rvConversations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MessageFragment.adapter
        }

        // Swipe to delete
        val swipeCallback = SwipeToDeleteCallback(requireContext()) { position ->
            val conversation = adapter.currentList.getOrNull(position) ?: return@SwipeToDeleteCallback
            confirmDelete(conversation)
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvConversations)
    }

    private fun observeConversations() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.conversations.collectLatest { list ->
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun confirmDelete(conversation: Conversation) {
        val position = adapter.currentList.indexOf(conversation)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Xóa cuộc trò chuyện")
            .setMessage("Bạn có chắc muốn xóa cuộc trò chuyện?")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteConversation(conversation.conversationId)
            }
            .setNegativeButton("Hủy") { _, _ ->
                if (position >= 0) adapter.notifyItemChanged(position)
            }
            .setOnCancelListener {
                // Bam ra ngoai dialog cung snap back
                if (position >= 0) adapter.notifyItemChanged(position)
            }
            .show()
    }

    // Mo chat tu danh sach conversation
    private fun openChat(conversation: Conversation) {
        val otherUserId = if (conversation.user1Id == currentUserId)
            conversation.user2Id else conversation.user1Id
        startChatWith(
            otherUserId = otherUserId,
            otherUserName = conversation.otherUserName,
            otherUserAvatar = conversation.otherUserAvatar
        )
    }

    override fun toolbarConfig() = ToolbarConfig(
        titleResId = R.string.title_name,
        isBack = false,
        isShowSearch = false
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Dung startChatWith() extension ben duoi de mo chat tu bat ky Fragment nao
    }
}

// Extension function - goi nhu the nay o bat ky Fragment nao:
// requireActivity().startChatWith(post.userId.toString(), post.title)
fun Fragment.startChatWith(
    otherUserId: String,
    otherUserName: String,
    otherUserAvatar: String = ""
) {
    val intent = Intent(requireContext(), ChatActivity::class.java).apply {
        putExtra(ChatActivity.EXTRA_OTHER_USER_ID, otherUserId)
        putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, otherUserName)
        putExtra(ChatActivity.EXTRA_OTHER_USER_AVATAR, otherUserAvatar)
    }
    startActivity(intent)
}