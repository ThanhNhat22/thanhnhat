package com.app.findback.ui.fragments

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.app.findback.R
import com.app.findback.databinding.BottomSheetEditProfileBinding
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class EditProfileBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    private val database = FirebaseDatabase.getInstance()

    private var imageUri: Uri? = null

    // ✅ DismissListener để ProfileFragment reload
    private var onDismissListener: (() -> Unit)? = null

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

    private val pickImage =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                imageUri = uri
                binding.imgAvatar.setImageURI(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEditProfileBinding.inflate(
            inflater, container, false
        )

        auth = FirebaseAuth.getInstance()

        loadUserData()
        setupClickEvents()

        return binding.root
    }

    private fun setupClickEvents() {
        binding.btnPickImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        database.getReference("users")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->

                val fullName = snapshot.child("fullName")
                    .value?.toString() ?: ""

                val phoneNumber = snapshot.child("phoneNumber")
                    .value?.toString() ?: ""

                val avatar = snapshot.child("avatar")
                    .value?.toString() ?: ""

                binding.edtName.setText(
                    fullName.ifEmpty { user.displayName ?: "" }
                )

                binding.edtPhone.setText(phoneNumber)

                if (avatar.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(avatar)
                        .placeholder(R.drawable.logo_tran)
                        .circleCrop()
                        .into(binding.imgAvatar)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Lỗi tải dữ liệu: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateProfile() {
        val uid = auth.currentUser?.uid ?: return

        val name = binding.edtName.text.toString().trim()
        val phone = binding.edtPhone.text.toString().trim()

        if (name.isEmpty()) {
            binding.edtName.error = "Vui lòng nhập tên"
            return
        }

        if (phone.isEmpty()) {
            binding.edtPhone.error = "Vui lòng nhập số điện thoại"
            return
        }

        binding.btnSave.isEnabled = false

        if (imageUri != null) {
            uploadAvatarAndSave(uid, name, phone)
        } else {
            saveUserData(uid, name, phone, null)
        }
    }

    private fun uploadAvatarAndSave(
        uid: String,
        name: String,
        phone: String
    ) {
        MediaManager.get()
            .upload(imageUri)
            .option("folder", "findback/avatars")
            .option("public_id", uid)
            .callback(object : UploadCallback {

                override fun onStart(requestId: String?) {}

                override fun onProgress(
                    requestId: String?,
                    bytes: Long,
                    totalBytes: Long
                ) {}

                override fun onSuccess(
                    requestId: String?,
                    resultData: MutableMap<Any?, Any?>?
                ) {
                    val avatarUrl = resultData
                        ?.get("secure_url")
                        .toString()

                    saveUserData(uid, name, phone, avatarUrl)
                }

                override fun onError(
                    requestId: String?,
                    error: ErrorInfo?
                ) {
                    binding.btnSave.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        "Upload ảnh thất bại: ${error?.description}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onReschedule(
                    requestId: String?,
                    error: ErrorInfo?
                ) {}
            })
            .dispatch()
    }

    private fun saveUserData(
        uid: String,
        name: String,
        phone: String,
        avatarUrl: String?
    ) {
        val updates = hashMapOf<String, Any>(
            "fullName" to name,
            "phoneNumber" to phone
        )

        if (!avatarUrl.isNullOrEmpty()) {
            updates["avatar"] = avatarUrl
        }

        database.getReference("users")
            .child(uid)
            .updateChildren(updates)
            .addOnSuccessListener {

                val profileUpdates =
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                auth.currentUser?.updateProfile(profileUpdates)

                Toast.makeText(
                    requireContext(),
                    "Cập nhật thành công",
                    Toast.LENGTH_SHORT
                ).show()

                dismiss()
            }
            .addOnFailureListener {
                binding.btnSave.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    it.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}