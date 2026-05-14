package com.app.findback.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.findback.R
import com.app.findback.databinding.FragmentHomeBinding
import com.app.findback.domain.models.Post
import com.app.findback.ui.activities.BaseBottomNavActivity
import com.app.findback.ui.activities.ChatActivity
import com.app.findback.ui.activities.PostDetailActivity
import com.app.findback.ui.activities.SearchPostActivity
import com.app.findback.ui.adapters.HomeAdapter
import com.app.findback.ui.components.toolbar.ToolbarConfig
import com.app.findback.ui.components.toolbar.ToolbarConfigProvider
import com.app.findback.ui.viewmodel.PostViewModel
import com.app.findback.ui.viewmodels.NotificationViewModel
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), ToolbarConfigProvider {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeAdapter: HomeAdapter
    private lateinit var allPost: List<Post>
    private lateinit var postViewModel: PostViewModel

    private val notificationViewModel: NotificationViewModel by viewModels()

    private var iconIB2 = R.drawable.ic_search
    private var currentUnreadCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setControl()
        setEvent()

        observeNotificationBadge()
    }

    private fun setControl() {
        postViewModel = ViewModelProvider(requireActivity())[PostViewModel::class.java]
        homeAdapter = HomeAdapter(requireContext(), mutableListOf())
    }

    private fun setEvent() {
        getDataFromViewModel()
        setRecyclerView()
        filterPosts()
    }

    private fun getDataFromViewModel() {
        postViewModel.postsShared.observe(viewLifecycleOwner) { posts ->
            allPost = posts
            homeAdapter.addNewData(allPost)
        }
    }

    private fun setEventClickSearch() {
        val intent = Intent(requireContext(), SearchPostActivity::class.java)
        startActivity(intent)
    }

    private fun filterPosts() {
        binding.cgChip.check(binding.cgChip.getChildAt(0).id)
        binding.cgChip.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                if (chip.id == checkedId) {
                    when (chip.text) {
                        "Tất cả" -> homeAdapter.addNewData(allPost)
                        "Thất lạc" -> homeAdapter.addNewData(allPost.filter { it.postType == "lost" })
                        "Tìm thấy" -> homeAdapter.addNewData(allPost.filter { it.postType == "found" })
                        "Gần tôi" -> { /* TODO */ }
                        else -> homeAdapter.addNewData(allPost)
                    }
                }
            }
        }
    }

    private fun setRecyclerView() {
        val layoutManager = GridLayoutManager(requireContext(), 1).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        binding.rvPosts.layoutManager = layoutManager
        binding.rvPosts.adapter = homeAdapter

        val layoutManagerVip = GridLayoutManager(requireContext(), 1).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
        binding.rvPostsSuggest.layoutManager = layoutManagerVip
        binding.rvPostsSuggest.adapter = homeAdapter

        homeAdapter.setOnItemClickListener(object : HomeAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val postId = allPost[position].postId
                Log.d("HomeFragment", "Clicked postId: $postId")
                val intent = Intent(requireContext(), PostDetailActivity::class.java)
                intent.putExtra("postId", postId)
                startActivity(intent)
            }

            override fun onItemClickShare(position: Int) {
                val postId = allPost[position].postId
                val link = "https://metalk-a52fb.web.app/post/$postId"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, link)
                }
                startActivity(Intent.createChooser(intent, "Chia sẻ"))
            }

            override fun onItemClickChat(position: Int) {
                val post = allPost[position]
                openChatWithPostOwner(post)
            }
        })
    }

    private fun observeNotificationBadge() {
        lifecycleScope.launch {
            notificationViewModel.unreadCount.collect { count ->
                currentUnreadCount = count
                refreshToolbar()
            }
        }
    }

    private fun refreshToolbar() {
        (requireActivity() as? BaseBottomNavActivity)?.refreshToolbarForActiveFragment()
    }

    override fun toolbarConfig(): ToolbarConfig {
        return ToolbarConfig(
            titleResId = R.string.app_name,
            isShowSearch = false,
            isBack = false,
            imageLogoRes = R.drawable.logo_tran,
            ib1Res = R.drawable.ic_notification,
            ib2Res = iconIB2,
            ib1Badge = currentUnreadCount,
            onIB1 = { openNotificationsScreen() },
            onIB2 = { setEventClickSearch() }
        )
    }

    private fun openNotificationsScreen() {
        val activity = requireActivity()
        if (activity is BaseBottomNavActivity) {
            activity.openNotificationsFragment()
        } else {
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, NotificationsFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun openChatWithPostOwner(post: Post) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (post.userId == currentUserId) {
            Toast.makeText(requireContext(), "Đây là bài viết của bạn", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra("from_post_detail", true)
            putExtra(ChatActivity.EXTRA_OTHER_USER_ID, post.userId)
            putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, post.userName ?: "Người dùng")
            putExtra(ChatActivity.EXTRA_OTHER_USER_AVATAR, post.userAvatar ?: "")
            putExtra(ChatActivity.EXTRA_SEND_POST_ID, post.postId)
            putExtra(ChatActivity.EXTRA_SEND_POST_TITLE, post.title)
            putExtra(ChatActivity.EXTRA_SEND_POST_IMAGE, post.imageUrl)
            putExtra(ChatActivity.EXTRA_SEND_POST_DESC, post.description)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}