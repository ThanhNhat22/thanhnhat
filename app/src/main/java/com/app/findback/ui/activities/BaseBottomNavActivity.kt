package com.app.findback.ui.activities

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.app.findback.BaseActivity
import com.app.findback.R
import com.app.findback.data.repositories.PostRepositoryImpl
import com.app.findback.databinding.ActivityBaseBottomNavBinding
import com.app.findback.domain.models.CircleZone
import com.app.findback.domain.models.Post
import com.app.findback.ui.fragments.HomeFragment
import com.app.findback.ui.fragments.MapFragment
import com.app.findback.ui.fragments.MessageFragment
import com.app.findback.ui.fragments.ProfileFragment
import com.app.findback.ui.components.toolbar.ToolbarConfig
import com.app.findback.ui.components.toolbar.ToolbarConfigProvider
import com.app.findback.ui.viewmodel.CircleZoneViewModel
import com.app.findback.ui.viewmodel.PostViewModel

class BaseBottomNavActivity : BaseActivity() {
    private lateinit var binding: ActivityBaseBottomNavBinding
    val _binding get() = binding
    private val homeFragment = HomeFragment()
    private val mapFragment = MapFragment()
    private val messageFragment = MessageFragment()
    private val profileFragment = ProfileFragment()
    private var activeFragment: Fragment = homeFragment
    private val fragmentByItemId: Map<Int, Fragment> by lazy {
        mapOf(
            R.id.nav_home to homeFragment,
            R.id.nav_map to mapFragment,
            R.id.nav_message to messageFragment,
            R.id.nav_profile to profileFragment
        )
    }
    //viewmodel post
    private lateinit var postViewModel: PostViewModel
    private lateinit var circleZoneViewModel: CircleZoneViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setControl()
        setContentView(binding.root)
        setBottomNav()
        setBottomNavInsert()
        getPosts()
    }
    //get data
    val getToolbar get() = binding.toolbarLayout.toolbar
    //set control
    fun setControl() {
        binding = ActivityBaseBottomNavBinding.inflate(layoutInflater)
        postViewModel = PostViewModel()
        circleZoneViewModel = CircleZoneViewModel()
    }

    //set bottomnav
    private fun setBottomNav() {
        binding.bottomNav.itemIconTintList = null
        binding.bottomNav.itemTextColor = createBottomNavTextColors()

        setBottomNavIcons(R.id.nav_home)

        //làm cấu trúc bottomnav lần đầu
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, homeFragment, "homeFragment")
            .add(R.id.fragmentContainer, mapFragment, "mapFragment").hide(mapFragment)
            .add(R.id.fragmentContainer, messageFragment, "messageFragment").hide(messageFragment)
            .add(R.id.fragmentContainer, profileFragment, "profileFragment").hide(profileFragment)
            .commit()

        //bắt sự kiện bottom
        binding.bottomNav.setOnItemSelectedListener { item ->
            val targetFragment = fragmentByItemId[item.itemId] ?: return@setOnItemSelectedListener false
            if (targetFragment === activeFragment) return@setOnItemSelectedListener true

            setBottomNavIcons(item.itemId)
            switchFragment(targetFragment)
            applyToolbarForFragment(targetFragment)
            updateToolbarScrollBehavior(targetFragment)
            true
        }

        applyToolbarForFragment(homeFragment)
        updateToolbarScrollBehavior(homeFragment)
        binding.bottomNav.selectedItemId = R.id.nav_home
    }

    //switch fragment
    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(fragment)
            .commit()
        activeFragment = fragment
    }

    private fun applyToolbarForFragment(fragment: Fragment) {
        val config = (fragment as? ToolbarConfigProvider)?.toolbarConfig()
            ?: ToolbarConfig(titleResId = R.string.app_name)

        setupToolbarCus(
            toolbar = getToolbar,
            title = getString(config.titleResId),
            isShowSearch = config.isShowSearch,
            backgroudResId = config.backgroudResId,
            isBack = config.isBack,
            imageLogo = config.imageLogoRes,
            ib1 = config.ib1Res,
            ib2 = config.ib2Res,
            onIB1 = config.onIB1,
            onIB2 = config.onIB2
        )
    }

    fun refreshToolbarForActiveFragment() {
        applyToolbarForFragment(activeFragment)
    }

    fun getToolbarSearchInput(): EditText = binding.toolbarLayout.etSearch

    private fun updateToolbarScrollBehavior(fragment: Fragment) {
        val toolbarLayoutParams = getToolbar.layoutParams as? AppBarLayout.LayoutParams ?: return
        val appBarLayout = binding.toolbarLayout.root as? AppBarLayout ?: return

        val isHome = fragment === homeFragment
        val targetFlags = if (isHome) {
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        } else {
            0
        }

        if (toolbarLayoutParams.scrollFlags != targetFlags) {
            toolbarLayoutParams.scrollFlags = targetFlags
            getToolbar.layoutParams = toolbarLayoutParams
        }

        if (!isHome) {
            // Reset collapsed state when moving away from Home.
            appBarLayout.setExpanded(true, true)
        }
    }

    private fun createBottomNavTextColors(): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.primary_blue),
            ContextCompat.getColor(this, R.color.bottom_nav_unselected)
        )
        return ColorStateList(states, colors)
    }

    private fun setBottomNavIcons(selectedItemId: Int) {
        binding.bottomNav.menu.findItem(R.id.nav_home).setIcon(
            if (selectedItemId == R.id.nav_home) R.drawable.ic_home else R.drawable.ic_home_grey
        )
        binding.bottomNav.menu.findItem(R.id.nav_map).setIcon(
            if (selectedItemId == R.id.nav_map) R.drawable.ic_map else R.drawable.ic_map_grey
        )
        binding.bottomNav.menu.findItem(R.id.nav_message).setIcon(
            if (selectedItemId == R.id.nav_message) R.drawable.ic_message else R.drawable.ic_message_grey
        )
        binding.bottomNav.menu.findItem(R.id.nav_profile).setIcon(
            if (selectedItemId == R.id.nav_profile) R.drawable.ic_profile else R.drawable.ic_profile_grey
        )
    }
    private fun setBottomNavInsert(){
        setupBottomNavInsertCus(binding.bottomNav)
    }
    //lấy post
    private fun getPosts(){
       postViewModel.getPosts()
    }

    //tạo giả data cho posts
     fun createPostsDemo(){
        posts().forEach { post ->
            Log.d("test", "Tạo post ${post.toMap()}")
            postViewModel.createPost(post) { result ->
                Log.d("test", "Tạo post ${post.postId} thành công: $result")
            }
        }
     }
    //danh sách data posts demo
    fun posts(): List<Post> {
        return listOf(
            Post(
                postId = "1",
                userId = 101,
                postType = "lost",
                title = "Mất ví da màu đen",
                description = "Mất ví gần công viên, bên trong có CCCD và tiền",
                itemCategory = "Ví",
                incidentDatetime = "2026-04-15 18:30",
                locationText = "Công viên Lê Văn Tám",
                latitude = 10.7870,
                longitude = 106.6920,
                createdAt = System.currentTimeMillis()
            ),

            Post(
                postId = "2",
                userId = 102,
                postType = "found",
                title = "Nhặt được điện thoại iPhone",
                description = "Nhặt được iPhone 13 gần quán cafe",
                itemCategory = "Điện thoại",
                incidentDatetime = "2026-04-14 10:00",
                locationText = "Quận 1, TP.HCM",
                latitude = 10.7769,
                longitude = 106.7009,
                createdAt = System.currentTimeMillis()
            ),

            Post(
                postId = "3",
                userId = 103,
                postType = "lost",
                title = "Mất balo laptop",
                description = "Balo chứa laptop Dell, tài liệu quan trọng",
                itemCategory = "Balo",
                incidentDatetime = "2026-04-13 08:00",
                locationText = "ĐH Bách Khoa",
                latitude = 10.7735,
                longitude = 106.6593,
                createdAt = System.currentTimeMillis()
            ),

            Post(
                postId = "4",
                userId = 104,
                postType = "found",
                title = "Nhặt được chìa khóa xe",
                description = "Chìa khóa xe Honda, có móc khóa Pikachu",
                itemCategory = "Chìa khóa",
                incidentDatetime = "2026-04-12 20:00",
                locationText = "Quận 10",
                latitude = 10.7700,
                longitude = 106.6670,
                createdAt = System.currentTimeMillis()
            ),

            Post(
                postId = "5",
                userId = 105,
                postType = "lost",
                title = "Mất tai nghe AirPods",
                description = "Rơi mất tai nghe ở trung tâm thương mại",
                itemCategory = "Tai nghe",
                incidentDatetime = "2026-04-11 15:00",
                locationText = "Vincom Đồng Khởi",
                latitude = 10.7798,
                longitude = 106.6992,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}