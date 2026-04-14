package com.app.findback.ui.activities

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.app.findback.BaseActivity
import com.app.findback.R
import com.app.findback.databinding.ActivityBaseBottomNavBinding
import com.app.findback.ui.fragments.HomeFragment
import com.app.findback.ui.fragments.MapFragment
import com.app.findback.ui.fragments.MessageFragment
import com.app.findback.ui.fragments.ProfileFragment
import com.app.findback.ui.components.toolbar.ToolbarConfig
import com.app.findback.ui.components.toolbar.ToolbarConfigProvider

class BaseBottomNavActivity : BaseActivity() {
    private lateinit var binding: ActivityBaseBottomNavBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setControl()
        setContentView(binding.root)
        setBottomNav()
        setBottomNavInsert()
    }
    //get data
    val getToolbar get() = binding.toolbarLayout.toolbar
    //set control
    fun setControl() {
        binding = ActivityBaseBottomNavBinding.inflate(layoutInflater)
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
            isBack = config.isBack,
            imageLogo = config.imageLogoRes,
            ib1 = config.ib1Res,
            ib2 = config.ib2Res
        )
    }

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

//    private fun updateFragmentContainerBottomMargin() {
//        val params = binding.fragmentContainer.layoutParams as ViewGroup.MarginLayoutParams
//        val bottomNavHeight = binding.bottomNav.height
//        if (params.bottomMargin != bottomNavHeight) {
//            params.bottomMargin = bottomNavHeight
//            binding.fragmentContainer.layoutParams = params
//        }
//    }
}