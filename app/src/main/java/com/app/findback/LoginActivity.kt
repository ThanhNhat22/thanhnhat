package com.app.findback

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.findback.databinding.ActivityLoginBinding
import com.app.findback.ui.activities.BaseBottomNavActivity
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Firebase Auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupListeners()
    }

    private fun setupListeners() {

        // Nút đăng nhập
        binding.btnLogin.setOnClickListener {
            login()
        }

        // Chuyển sang màn hình đăng ký
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Quên mật khẩu
        binding.tvForgotPassword.setOnClickListener {

            startActivity(
                Intent(this, ForgotPasswordActivity::class.java)
            )
        }
    }

    private fun login() {

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validation
        if (email.isEmpty()) {
            binding.etEmail.error = "Vui lòng nhập email"
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Vui lòng nhập mật khẩu"
            return
        }

        if (password.length < 6) {
            binding.etPassword.error = "Mật khẩu tối thiểu 6 ký tự"
            return
        }

        showLoading(true)

        // Đăng nhập Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                showLoading(false)

                if (task.isSuccessful) {

                    Toast.makeText(
                        this,
                        "Đăng nhập thành công",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(
                        Intent(this, BaseBottomNavActivity::class.java)
                    )

                    finish()

                } else {

                    Toast.makeText(
                        this,
                        task.exception?.message ?: "Đăng nhập thất bại",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }


    private fun showLoading(isLoading: Boolean) {

        binding.progressBar.visibility =
            if (isLoading) View.VISIBLE else View.GONE

        binding.btnLogin.isEnabled = !isLoading
    }
}