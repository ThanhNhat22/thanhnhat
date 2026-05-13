package com.app.findback

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.app.findback.databinding.ActivityMainBinding
import com.app.findback.ui.activities.BaseBottomNavActivity

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateToMainScreen()
    }

    private fun navigateToMainScreen() {
        startActivity(Intent(this, BaseBottomNavActivity::class.java))
        finish()
    }


    private fun setToolbar() {
        setupToolbarCus(
            binding.toolbarLayout.toolbar,
            "Goc tim do",
            false,
            null,
            false,
            R.drawable.logo_tran,
            R.drawable.ic_notification,
            R.drawable.ic_search
        )
    }
}