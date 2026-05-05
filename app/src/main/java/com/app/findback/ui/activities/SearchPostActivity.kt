package com.app.findback.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.findback.BaseActivity
import com.app.findback.R
import com.app.findback.databinding.ActivitySearchPostBinding

class SearchPostActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchPostBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setControl()
        setEvent()
    }
    private fun setControl(){

    }
    private fun setEvent(){
        setUpToolbar()
    }
    //thiết lập toolbar cho màn hình SreachPost
    private fun setUpToolbar() {
        setupToolbarCus(
            toolbar = binding.toolbarLayout.toolbar,
            title = getString(R.string.search_post_title),
            isShowSearch = true,
            isBack = true,
            onBack = {
                setKeybroad()
            }
        )
    }
}