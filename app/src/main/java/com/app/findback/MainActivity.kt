package com.app.findback

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.findback.databinding.ActivityMainBinding
import com.app.findback.ui.activities.BaseBottomNavActivity

class MainActivity : BaseActivity() {
    //khai báo biến
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setControl()
        setContentView(binding.root)
        setToolbar()

        //chuyển màn hình
        val intent = Intent(this, BaseBottomNavActivity::class.java)
        startActivity(intent)

    }
    fun setControl(){
        binding = ActivityMainBinding.inflate(layoutInflater)
    }
    //setToolbar
    private fun setToolbar(){
        setupToolbarCus(binding.toolbarLayout.toolbar, "Góc tìm đồ",false,null, false, R.drawable.logo_tran,R.drawable.ic_notification,R.drawable.ic_search)
    }
}