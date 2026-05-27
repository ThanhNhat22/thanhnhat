package com.app.findback

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HandleResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Không cần làm gì
        // Firebase tự xử lý reset password qua trình duyệt
        finish()
    }
}