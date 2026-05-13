package com.app.findback.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.findback.BaseActivity
import com.app.findback.R
import com.app.findback.data.repositories.MessageRepositoryImpl
import com.app.findback.data.source.remote.FirebaseMessageDataSource
import com.app.findback.databinding.ActivityChatBinding
import com.app.findback.domain.models.MessageLocation
import com.app.findback.domain.models.MessagePost
import com.app.findback.ui.adapters.ChatAdapter
import com.app.findback.ui.viewmodel.MessageViewModel
import com.app.findback.ui.viewmodel.MessageViewModelFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.LocationServices
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class ChatActivity : BaseActivity() {

    companion object {
        const val EXTRA_CONVERSATION_ID = "conv_id"
        const val EXTRA_OTHER_USER_ID = "other_user_id"
        const val EXTRA_OTHER_USER_NAME = "other_user_name"
        const val EXTRA_OTHER_USER_AVATAR = "other_user_avatar"
    }

    private lateinit var binding: ActivityChatBinding

    private val viewModel: MessageViewModel by viewModels {
        MessageViewModelFactory(MessageRepositoryImpl(FirebaseMessageDataSource()))
    }

    private val otherUserId by lazy { intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: "" }
    private val otherUserName by lazy { intent.getStringExtra(EXTRA_OTHER_USER_NAME) ?: "Chat" }
    private val otherUserAvatar by lazy { intent.getStringExtra(EXTRA_OTHER_USER_AVATAR) ?: "" }
    private val conversationId by lazy {
        intent.getStringExtra(EXTRA_CONVERSATION_ID)
            ?: viewModel.getConversationId(otherUserId)
    }
    private val currentUserId: String by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    private val chatAdapter by lazy {
        // Phai dam bao currentUserId da co truoc khi tao adapter
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        ChatAdapter(
            currentUserId = uid,
            onLocationClick = { lat, lng -> openGoogleMaps(lat, lng) },
            onPostClick = { postId -> openPostDetail(postId) }
        )
    }

    // ─── Launchers ────────────────────────────────────────────────────────────

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) fetchAndSendLocation()
        else showToast("Cannot access location")
    }

    private val postPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val post = MessagePost(
                postId = data?.getStringExtra("post_id") ?: "",
                title = data?.getStringExtra("post_title") ?: "",
                imageUrl = data?.getStringExtra("post_image") ?: "",
                description = data?.getStringExtra("post_desc") ?: ""
            )
            if (post.postId.isNotEmpty()) {
                viewModel.sendPostMessage(otherUserId, post)
                binding.layoutExtraActions.isVisible = false
            }
        }
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setKeybroad()

        setupToolbar()
        setupRecyclerView()
        setupInput()
        observeMessages()
        observeSendState()


        viewModel.loadMessages(conversationId)

    }

    // ─── Setup ────────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        binding.tvToolbarName.text = otherUserName
        Glide.with(applicationContext)
            .load(otherUserAvatar.ifEmpty { null })
            .apply(
                RequestOptions.circleCropTransform()
                    .error(R.drawable.ic_default_avatar)
            )
            .into(binding.ivToolbarAvatar)
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupInput() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotBlank()) {
                viewModel.sendTextMessage(otherUserId, text)
                binding.etMessage.setText("")
            }
        }

        binding.btnSendLocation.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fetchAndSendLocation()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        binding.btnSendPost.setOnClickListener {
            postPickerLauncher.launch(Intent(this, PostPickerActivity::class.java))
        }

        binding.btnAttach.setOnClickListener {
            binding.layoutExtraActions.isVisible = !binding.layoutExtraActions.isVisible
        }
    }

    // ─── Observe ──────────────────────────────────────────────────────────────

    private fun observeMessages() {
        lifecycleScope.launch {
            viewModel.messages.collectLatest { messages ->
                chatAdapter.submitList(messages) {
                    if (messages.isNotEmpty()) {
                        binding.rvMessages.scrollToPosition(messages.size - 1)
                        // markAsRead chi khi co tin nhan thuc su
                        viewModel.markAsRead(conversationId)
                    }
                }
            }
        }
    }

    private fun observeSendState() {
        lifecycleScope.launch {
            viewModel.sendState.collect { state ->
                if (state is MessageViewModel.SendState.Error) showToast(state.msg)
            }
        }
    }

    // ─── Location ─────────────────────────────────────────────────────────────

    private fun fetchAndSendLocation() {
        val client = LocationServices.getFusedLocationProviderClient(this)
        try {
            client.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val msgLocation = MessageLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        address = String.format(
                            Locale.getDefault(),
                            "Chia sẻ vị trí hiện tại.",
                            location.latitude,
                            location.longitude
                        )
                    )
                    viewModel.sendLocationMessage(otherUserId, msgLocation)
                    binding.layoutExtraActions.isVisible = false
                } else {
                    showToast("Could not get location")
                }
            }
        } catch (_: SecurityException) {
            showToast("Location permission error")
        }
    }

    private fun openGoogleMaps(lat: Double, lng: Double) {
        val geoUri = "geo:$lat,$lng?q=$lat,$lng".toUri()
        val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (mapsIntent.resolveActivity(packageManager) != null) {
            startActivity(mapsIntent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, "https://maps.google.com/?q=$lat,$lng".toUri()))
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun openPostDetail(postId: String) {
        showToast("Post: $postId")
    }

    private fun showToast(msg: String) =
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
}