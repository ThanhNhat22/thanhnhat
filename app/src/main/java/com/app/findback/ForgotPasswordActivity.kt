package com.app.findback

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.findback.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    private val client = OkHttpClient()

    private val SERVICE_ID = "service_3v7um35"
    private val TEMPLATE_ID = "template_at9xpp9"
    private val PUBLIC_KEY = "afe6Bpob5V-3FRdZa"

    private var generatedOtp: String = ""
    private var otpExpiryTime: Long = 0L
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        setupUI()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSendOtp.setOnClickListener { sendOtp() }
        binding.btnResetPassword.setOnClickListener { verifyOtpAndSendResetLink() }
    }

    // ==================== BƯỚC 1: GỬI OTP ====================
    private fun sendOtp() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            binding.etEmail.error = "Vui lòng nhập email"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email không hợp lệ"
            return
        }

        showLoading(true)
        doSendOtp(email)
    }

    private fun doSendOtp(email: String) {
        generatedOtp = (100000..999999).random().toString()
        otpExpiryTime = System.currentTimeMillis() + 5 * 60 * 1000

        val jsonBody = JSONObject().apply {
            put("service_id", SERVICE_ID)
            put("template_id", TEMPLATE_ID)
            put("user_id", PUBLIC_KEY)
            put("template_params", JSONObject().apply {
                put("to_email", email)
                put("otp_code", generatedOtp)
            })
        }

        val request = Request.Builder()
            .url("https://api.emailjs.com/api/v1.0/email/send")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .header("origin", "http://localhost")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Gửi OTP thất bại: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    showLoading(false)
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Đã gửi mã OTP đến $email\nMã có hiệu lực trong 5 phút",
                            Toast.LENGTH_LONG
                        ).show()
                        startCountDown()
                        binding.layoutOtp.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Gửi OTP thất bại: ${response.body?.string()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    // ==================== BƯỚC 2: XÁC NHẬN OTP → GỬI RESET LINK ====================
    private fun verifyOtpAndSendResetLink() {
        val email = binding.etEmail.text.toString().trim()
        val inputOtp = binding.etOtp.text.toString().trim()

        if (inputOtp.length != 6) {
            binding.etOtp.error = "Nhập mã OTP 6 số"
            return
        }

        when {
            generatedOtp.isEmpty() -> {
                Toast.makeText(this, "Vui lòng nhấn 'Gửi mã' trước", Toast.LENGTH_SHORT).show()
                return
            }
            System.currentTimeMillis() > otpExpiryTime -> {
                Toast.makeText(this, "Mã OTP đã hết hạn, vui lòng gửi lại", Toast.LENGTH_SHORT).show()
                return
            }
            inputOtp != generatedOtp -> {
                binding.etOtp.error = "Mã OTP không đúng"
                return
            }
        }

        // OTP đúng → gửi link đặt lại mật khẩu qua Firebase
        showLoading(true)

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    generatedOtp = ""
                    Toast.makeText(
                        this,
                        "✅ Xác minh thành công!\nLink đặt lại mật khẩu đã gửi đến $email\nVui lòng kiểm tra hộp thư và làm theo hướng dẫn",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    val errorMsg = when {
                        task.exception?.message?.contains("no user record") == true ->
                            "Email chưa được đăng ký"
                        task.exception?.message?.contains("badly formatted") == true ->
                            "Email không hợp lệ"
                        else -> task.exception?.message ?: "Lỗi không xác định"
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ==================== ĐẾM NGƯỢC ====================
    private fun startCountDown() {
        binding.btnSendOtp.isEnabled = false
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(60_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.btnSendOtp.text = "Gửi lại (${millisUntilFinished / 1000}s)"
            }

            override fun onFinish() {
                binding.btnSendOtp.isEnabled = true
                binding.btnSendOtp.text = "Gửi mã OTP"
            }
        }.start()
    }

    // ==================== LOADING ====================
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSendOtp.isEnabled = !isLoading
        binding.btnResetPassword.isEnabled = !isLoading
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}