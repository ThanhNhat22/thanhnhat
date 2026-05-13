package com.app.findback.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.app.findback.BaseActivity
import com.app.findback.R
import com.app.findback.databinding.ActivityPostDetailBinding
import com.app.findback.domain.models.MessagePost
import com.app.findback.domain.models.Post
import com.app.findback.ui.viewmodel.PostViewModel
import com.app.findback.utils.extentions.ConvertTime
import com.google.firebase.auth.FirebaseAuth
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class PostDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var postId: String
    private lateinit var postViewModel: PostViewModel
    private var currentPost: Post? = null
    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setControl()
        setEvent()
    }

    private fun setControl() {
        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]
        postId = getPostId()
        map = binding.map
    }

    private fun getPostId(): String {
        return intent.getStringExtra("postId") ?: ""
    }

    private fun setEvent() {
        setupToolbarCus(
            toolbar = binding.toolbarLayout.toolbar,
            title = getString(R.string.post_detail_title),
            isShowSearch = false,
            isBack = true,
            onBack = { setKeybroad() }
        )

        getDataFromViewModel()
        setOnClickListeners()
    }

    private fun getDataFromViewModel() {
        postViewModel.postsShared.observe(this) { posts ->
            val found = posts.find { it.postId == postId }
            found?.let {
                currentPost = it
                setDataToUI(it)
            }
        }
    }

    private fun setDataToUI(post: Post) {
        binding.btnChat.isVisible = post.userId != FirebaseAuth.getInstance().currentUser?.uid
        binding.apply {
            tvTitle.text = post.title ?: ""
            tvTime.text = ConvertTime.formatTime(post.createdAt.toString()) ?: ""
            tvDescription.text = post.description ?: ""
            tvTimePerfrom.text = ConvertTime.formatTime(post.incidentDatetime) ?: ""

            if (post.postType == "lost") {
                tvStatus.text = "Thất lạc"
                tvStatus.setTextColor(resources.getColor(R.color.primary_red))
            } else {
                tvStatus.text = "Tìm thấy"
                tvStatus.setTextColor(resources.getColor(R.color.primary_green))
            }
        }
        zoomMap()
    }



    private fun setOnClickListeners() {
        binding.btnShare.setOnClickListener {
            currentPost?.let { post ->
                val link = "https://metalk-a52fb.web.app/post/${post.postId}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, link)
                }
                startActivity(Intent.createChooser(intent, "Chia sẻ"))
            }
        }

        // Nút Nhắn tin
        binding.btnChat.setOnClickListener {
            currentPost?.let { post ->
                openChatWithPostOwner(post)
            }
        }
    }

    private fun openChatWithPostOwner(post: Post) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("from_post_detail", true)
            putExtra(ChatActivity.EXTRA_OTHER_USER_ID, post.userId)
            putExtra(ChatActivity.EXTRA_OTHER_USER_NAME, post.userName ?: "Người dùng")
            putExtra(ChatActivity.EXTRA_OTHER_USER_AVATAR, post.userAvatar ?: "")
            // Truyền thông tin bài post để hiển thị preview
            putExtra(ChatActivity.EXTRA_SEND_POST_ID, post.postId)
            putExtra(ChatActivity.EXTRA_SEND_POST_TITLE, post.title)
            putExtra(ChatActivity.EXTRA_SEND_POST_IMAGE, post.imageUrl)
            putExtra(ChatActivity.EXTRA_SEND_POST_DESC, post.description)
        }

        chatLauncher.launch(intent)

    }
    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private val chatLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK &&
                result.data?.getBooleanExtra("open_message_tab", false) == true
            ) {

                val intent = Intent(
                    this,
                    BaseBottomNavActivity::class.java
                ).apply {

                    putExtra("open_message_tab", true)

                    flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                }

                startActivity(intent)

            }
        }
    private fun zoomMap() {
        map.setMultiTouchControls(false)
        map.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                currentPost?.let {
                    val uriString = "https://metalk-a52fb.web.app/map/${it.longitude}/${it.latitude}"
                    val uri = android.net.Uri.parse(uriString)

                    val intent = Intent(this@PostDetailActivity, com.app.findback.ui.activities.BaseBottomNavActivity::class.java).apply {
                        data = uri
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    finish()
                }
            }
            true
        }

        currentPost?.let {
            val geoPoint = GeoPoint(it.latitude, it.longitude)

            map.post {
                map.controller.setZoom(15.0)
                map.controller.setCenter(geoPoint)

                val marker = Marker(map)
                marker.position = geoPoint
                marker.icon = if (it.postType.equals("Found", ignoreCase = true) || it.postType.equals("found", ignoreCase = true))
                    getDrawable(R.drawable.ic_location_green)
                else
                    getDrawable(R.drawable.ic_location_red)

                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                map.overlays.clear()
                map.overlays.add(marker)
                map.invalidate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}