package com.app.findback

import android.graphics.Color
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.app.findback.utils.extentions.ExtensionDp.Companion.dpToPx
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView

//Cấu hình chung cho toàn app
open class BaseActivity : AppCompatActivity() {
    override fun onContentChanged() {
        super.onContentChanged()
        setMode()
        //bật edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
    //Cấu hình toolbar ko che statusBar
    public fun setupToolbarCus(toolbar: Toolbar, title: String? = null, backgroudResId: Int? = null, isBack: Boolean = false,imageLogo:Int?=null,ib1:Int?=null,ib2:Int?=null, onBack: (() -> Unit)? = null,onIB1: (() -> Unit)? = null,onIB2: (() -> Unit)? = null) {
        setSupportActionBar(toolbar)

        //set title và back
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(isBack)
        }

        val logoView = toolbar.findViewById<ImageView>(R.id.ivToolbarLogo)
        val titleView = toolbar.findViewById<TextView>(R.id.tvToolbarTitle)
        val imageButton1 = toolbar.findViewById<ImageButton>(R.id.ib1)
        val imageButton2 = toolbar.findViewById<ImageButton>(R.id.ib2)



        titleView.text = title.orEmpty()

        //setBackground trong suốt
        toolbar.setBackgroundColor(backgroudResId ?: Color.WHITE)
        toolbar.elevation = 0f

        //set imagelogo
        if(imageLogo!=null){
            logoView.setImageResource(imageLogo)
            logoView.visibility = View.VISIBLE
        } else {
            logoView.visibility = View.GONE
        }
        //set ib1
        if(ib1!=null){
            imageButton1.setImageResource(ib1)
            imageButton1.visibility = View.VISIBLE
        } else {
            imageButton1.visibility = View.GONE
        }
        //set ib2
        if(ib2!=null){
            imageButton2.setImageResource(ib2)
            imageButton2.visibility = View.VISIBLE
        } else {
            imageButton2.visibility = View.GONE
        }



        //back
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // fix status bar
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top

            view.setPadding(0, topInset, 0, 0)

            val height = 64.dpToPx()
            view.layoutParams.height = height + topInset

            view.requestLayout()
            insets
        }
    }
    //cấu hình responsive cho bottom nav
    fun setupBottomNavInsertCus(bottomNav: BottomNavigationView) {

        val initialPaddingStart = bottomNav.paddingStart
        val initialPaddingTop = bottomNav.paddingTop
        val initialPaddingEnd = bottomNav.paddingEnd
        val initialPaddingBottom = bottomNav.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { view, insets ->

            val inset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            // clamp để không bị quá cao
            val safeInset = when {
            inset > 40.dpToPx() -> 20.dpToPx()
            inset > 0 -> inset / 2
            else -> 0
        }

            view.setPadding(
                initialPaddingStart,
                initialPaddingTop,
                initialPaddingEnd,
                initialPaddingBottom + safeInset
            )

            insets
        }

        ViewCompat.requestApplyInsets(bottomNav)
    }
    //Cấu hình mode light cho toàn app
    fun setMode(){
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
