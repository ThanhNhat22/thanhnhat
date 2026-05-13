package com.app.findback.ui.activities

import android.content.Intent
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
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class PostDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var postId: String
    private lateinit var postViewModel: PostViewModel
    private var post: Post? = null
    private lateinit var map: MapView

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
        map = binding.map
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
        setOnClick()
    }
    //đổ data ra ui
    private fun setDataToUI(post: Post) {
        this.post = post
        binding.apply {
            tvTitle.text = post.title ?: ""
            tvTime.text = ConvertTime.formatTime(post.createdAt) ?: ""
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
    //setOnClick ở đây
    private fun setOnClick() {
        binding.btnShare.setOnClickListener {
            post?.let{
                val postId = it.postId
                Log.d("BaseBottomNavActivity",postId)
                val link = "https://metalk-a52fb.web.app/post/$postId"

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, link)
                }
                startActivity(Intent.createChooser(intent, "Chia sẻ"))
            }
        }
    }
    //zoom tới vị trí mất đồ trong map
    private fun zoomMap() {
        // CHẶN TOÀN BỘ TƯƠNG TÁC
        map.setMultiTouchControls(false)
        map.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {

                post?.let {
                    // Tạo URI giống format bạn đang xử lý ở Base:
                    // uri pathSegments chứa: [0]:"map", [1]:lng, [2]:lat
                    // Scheme và Host bạn có thể đặt tuỳ ý (ví dụ findback://app)
                    val uriString = "https://metalk-a52fb.web.app/map/${it.longitude}/${it.latitude}"
                    val uri = android.net.Uri.parse(uriString)

                    // Tạo Intent trỏ về màn hình chứa Bottom Nav
                    val intent = android.content.Intent(this@PostDetailActivity, com.app.findback.ui.activities.BaseBottomNavActivity::class.java)
                    intent.data = uri
                    // Cờ khôi phục Activity nếu nó đang nằm dưới
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP

                    startActivity(intent)
                    finish() // Đóng Detail Activity
                }
            }
            true // Trả về true để tiếp tục chặn vuốt
        }
        post?.let {
            val geoPoint = GeoPoint(it.latitude, it.longitude)

            map.post {
                map.controller.setZoom(15.0)
                map.controller.setCenter(geoPoint)

                val marker = Marker(map)
                marker.position = geoPoint

                marker.icon = if (it.postType == "Found" || it.postType == "found")
                    getDrawable(R.drawable.ic_location_green)
                else
                    getDrawable(R.drawable.ic_location_red)

                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                map.overlays.clear() //tránh duplicate
                map.overlays.add(marker)
                map.invalidate() // Yêu cầu map vẽ lại
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