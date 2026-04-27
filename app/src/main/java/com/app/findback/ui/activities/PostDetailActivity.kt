package com.app.findback.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.app.findback.BaseActivity
import com.app.findback.R
import com.app.findback.databinding.ActivityPostDetailBinding
import com.app.findback.domain.models.Post
import com.app.findback.ui.viewmodel.PostViewModel
import com.app.findback.utils.extentions.ConvertTime
import android.widget.Toast

class PostDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var postId: String
    private lateinit var postViewModel: PostViewModel
    private var post: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setControl()
        setEvent()
    }
    //set control
    private fun setControl() {
        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]
        postId = getPostId()
    }
    //get data từ view model
    private fun getDataFromViewModel() {
        postViewModel.postsShared.observe(this) { posts ->
            val found = posts.find { it.postId == postId }
            found?.let {
                setDataToUI(found)
            }
        }
    }
    //get postId
    private fun getPostId(): String {
        return intent.getStringExtra("postId") ?: ""
    }
    //get data từ viewmodel
    //set sevetn
    private fun setEvent() {
        setupToolbarCus(
            toolbar = binding.toolbarLayout.toolbar,
            title = getString(R.string.post_detail_title),
            isShowSearch = false,
            isBack = true,
            onBack = {
                setKeybroad()
            }
        )
        // start observing posts and update when available
        getDataFromViewModel()
    }
    //đổ data ra ui
    private fun setDataToUI(post: Post) {
        binding.apply {
            tvTitle.text = post.title ?: ""
            tvTime.text = post.createdAt.toString() ?: ""
            tvDescription.text = post.description ?: ""
            tvTimePerfrom.text = ConvertTime.formatTime(post.incidentDatetime) ?: ""
            tvStatus.text = if (post.postType == "Found") getString(R.string.found) else getString(R.string.lost)
        }
    }
    //zoom tới vị trí mất đồ trong map
    private fun zoomMap() {

    }
}