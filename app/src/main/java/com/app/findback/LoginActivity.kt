package com.app.findback

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.findback.databinding.ActivityLoginBinding
import com.app.findback.ui.activities.BaseBottomNavActivity
import com.google.firebase.auth.FirebaseAuth
import com.onesignal.OneSignal

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Đã bật thông báo", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bạn đã từ chối quyền thông báo", Toast.LENGTH_SHORT).show()
            }
            navigateToMain()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener { login() }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun login() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) { binding.etEmail.error = "Vui lòng nhập email"; return }
        if (password.isEmpty()) { binding.etPassword.error = "Vui lòng nhập mật khẩu"; return }
        if (password.length < 6) { binding.etPassword.error = "Mật khẩu tối thiểu 6 ký tự"; return }

        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()

                    // Đăng ký thiết bị với OneSignal sau khi đăng nhập thành công
                    // OneSignal.initWithContext() đã được gọi trong Application class → gọi login() ở đây là đúng
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        OneSignal.login(userId)
                    }

                    // Xin quyền thông báo — MainActivity sẽ KHÔNG gọi requestPermission() nữa
                    requestNotificationPermissionIfNeeded()

                } else {
                    Toast.makeText(
                        this,
                        task.exception?.message ?: "Đăng nhập thất bại",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                navigateToMain()
            } else {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, BaseBottomNavActivity::class.java))
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }
}