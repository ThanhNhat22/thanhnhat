package com.app.findback.ui.fragments

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.findback.databinding.BottomSheetEditPostBinding
import com.app.findback.domain.models.Post
import com.app.findback.ui.adapters.ImagePickerAdapter
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.FirebaseDatabase

class EditPostBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEditPostBinding? = null
    private val binding get() = _binding!!

    private val database = FirebaseDatabase.getInstance()

    private var onDismissListener: (() -> Unit)? = null

    private lateinit var imageAdapter: ImagePickerAdapter

    private val selectedImages = mutableListOf<Uri>()

    private val uploadedImageUrls = mutableListOf<String>()

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

    companion object {

        fun newInstance(post: Post): EditPostBottomSheet {

            return EditPostBottomSheet().apply {

                arguments = Bundle().apply {

                    putString("postId", post.postId)
                    putString("title", post.title)
                    putString("description", post.description)
                    putString("locationText", post.locationText)
                    putString("postType", post.postType)

                    putStringArrayList(
                        "imageUrls",
                        ArrayList(post.imageUrls)
                    )
                }
            }
        }
    }

    // =========================================================
    // IMAGE PICKER
    // =========================================================

    private val pickImagesLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetMultipleContents()
        ) { uris ->

            if (uris.isNotEmpty()) {

                imageAdapter.addImages(uris)
            }
        }

    // =========================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            BottomSheetEditPostBinding.inflate(
                inflater,
                container,
                false
            )

        setupImages()

        loadPostData()

        setupClickEvents()

        return binding.root
    }

    // =========================================================
    // LOAD DATA
    // =========================================================

    private fun loadPostData() {

        arguments?.let {

            binding.edtTitle.setText(
                it.getString("title") ?: ""
            )

            binding.edtDescription.setText(
                it.getString("description") ?: ""
            )

            binding.edtLocation.setText(
                it.getString("locationText") ?: ""
            )

            // ===== POST TYPE =====
            when (it.getString("postType")) {

                "lost" ->
                    binding.rbLost.isChecked = true

                "found" ->
                    binding.rbFound.isChecked = true
            }

            // ===== LOAD OLD IMAGES =====
            val oldImages =
                it.getStringArrayList("imageUrls")
                    ?: arrayListOf()

            oldImages.forEach { url ->

                selectedImages.add(Uri.parse(url))
            }

            imageAdapter.notifyDataSetChanged()
        }
    }

    // =========================================================
    // SETUP IMAGES
    // =========================================================

    private fun setupImages() {

        imageAdapter = ImagePickerAdapter(

            selectedImages,

            onAddClick = {

                pickImagesLauncher.launch("image/*")
            },

            onRemoveClick = { position ->

                imageAdapter.removeImage(position)
            }
        )

        binding.rvImages.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            adapter = imageAdapter
        }
    }

    // =========================================================
    // CLICK EVENTS
    // =========================================================

    private fun setupClickEvents() {

        binding.btnSave.setOnClickListener {

            savePost()
        }
    }

    // =========================================================
    // SAVE POST
    // =========================================================

    private fun savePost() {

        val postId =
            arguments?.getString("postId")
                ?: return

        val title =
            binding.edtTitle.text.toString().trim()

        val description =
            binding.edtDescription.text.toString().trim()

        val locationText =
            binding.edtLocation.text.toString().trim()

        val postType =
            if (binding.rbLost.isChecked)
                "lost"
            else
                "found"

        if (title.isEmpty()) {

            binding.edtTitle.error =
                "Vui lòng nhập tiêu đề"

            return
        }

        binding.btnSave.isEnabled = false

        val imageUris =
            imageAdapter.getImages()

        // ===== KHÔNG CÓ ẢNH =====
        if (imageUris.isEmpty()) {

            updateFirebase(
                postId,
                title,
                description,
                locationText,
                postType,
                emptyList()
            )

            return
        }

        uploadedImageUrls.clear()

        imageUris.forEach { uri ->

            // ===== ẢNH CŨ =====
            if (uri.toString().startsWith("http")) {

                uploadedImageUrls.add(uri.toString())

                checkUploadComplete(
                    postId,
                    title,
                    description,
                    locationText,
                    postType,
                    imageUris.size
                )

            } else {

                // ===== UPLOAD CLOUDINARY =====
                MediaManager.get()
                    .upload(uri)
                    .callback(object : UploadCallback {

                        override fun onStart(requestId: String?) {
                        }

                        override fun onProgress(
                            requestId: String?,
                            bytes: Long,
                            totalBytes: Long
                        ) {
                        }

                        override fun onSuccess(
                            requestId: String?,
                            resultData: MutableMap<Any?, Any?>?
                        ) {

                            val imageUrl =
                                resultData?.get("secure_url")
                                    .toString()

                            uploadedImageUrls.add(imageUrl)

                            checkUploadComplete(
                                postId,
                                title,
                                description,
                                locationText,
                                postType,
                                imageUris.size
                            )
                        }

                        override fun onError(
                            requestId: String?,
                            error: ErrorInfo?
                        ) {

                            binding.btnSave.isEnabled = true

                            Toast.makeText(
                                requireContext(),
                                "Upload ảnh thất bại",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onReschedule(
                            requestId: String?,
                            error: ErrorInfo?
                        ) {
                        }
                    })
                    .dispatch()
            }
        }
    }

    // =========================================================
    // CHECK COMPLETE
    // =========================================================

    private fun checkUploadComplete(
        postId: String,
        title: String,
        description: String,
        locationText: String,
        postType: String,
        totalImages: Int
    ) {

        if (uploadedImageUrls.size == totalImages) {

            updateFirebase(
                postId,
                title,
                description,
                locationText,
                postType,
                uploadedImageUrls
            )
        }
    }

    // =========================================================
    // UPDATE FIREBASE
    // =========================================================

    private fun updateFirebase(
        postId: String,
        title: String,
        description: String,
        locationText: String,
        postType: String,
        imageUrls: List<String>
    ) {

        val updates = hashMapOf<String, Any>(

            "title" to title,

            "description" to description,

            "locationText" to locationText,

            "postType" to postType,

            "imageUrls" to imageUrls,

            "updatedAt" to System.currentTimeMillis()
        )

        database.getReference("posts")
            .child(postId)
            .updateChildren(updates)
            .addOnSuccessListener {

                Toast.makeText(
                    requireContext(),
                    "Cập nhật bài đăng thành công",
                    Toast.LENGTH_SHORT
                ).show()

                dismiss()
            }
            .addOnFailureListener {

                binding.btnSave.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "Lỗi: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // =========================================================

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}