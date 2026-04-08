package com.app.findback

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.findback.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    //khai báo biến
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setControl()
        setContentView(binding.root)

    }
    fun setControl(){
        binding = ActivityMainBinding.inflate(layoutInflater)
    }

}