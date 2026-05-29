package com.app.findback.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.app.findback.data.repositories.PostRepositoryImpl
import com.app.findback.domain.models.Post

import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.FirebaseDatabase

class PostViewModel : ViewModel() {

    private val repository = PostRepositoryImpl()
    private val _posts = MutableLiveData<List<Post>>(emptyList())


    init {
        getPosts()
    }
    fun getPosts(){
        repository.getPosts { newPosts ->
            _posts.postValue(newPosts)
        }
    }
    private val database =
        FirebaseDatabase.getInstance()
            .getReference("posts")


    private val _uploadState =
        MutableLiveData<UploadState>()

    val uploadState: LiveData<UploadState>
            = _uploadState

    private val _postsShared =
        MutableLiveData<List<Post>>()

    val postsShared: LiveData<List<Post>>
            = _postsShared

    // =========================================================
    // UPLOAD STATE
    // =========================================================

    sealed class UploadState {

        object Loading : UploadState()

        object Success : UploadState()

        data class Error(
            val message: String
        ) : UploadState()
    }

    // =========================================================
    // UPLOAD POST
    // =========================================================

    fun uploadPost(
        post: Post,
        imageUris: List<Uri>
    ) {

        _uploadState.value =
            UploadState.Loading

        // Không có ảnh
        if (imageUris.isEmpty()) {

            savePostToDatabase(post)

            return
        }

        // Có ảnh
        uploadImagesToCloudinary(
            imageUris = imageUris,

            onComplete = { uploadedUrls ->

                val postWithImages =
                    post.copy(
                        imageUrls = uploadedUrls
                    )

                savePostToDatabase(postWithImages)
            }
        )
    }

    // =========================================================
// UPLOAD IMAGES
// =========================================================

    private fun uploadImagesToCloudinary(
        imageUris: List<Uri>,
        onComplete: (List<String>) -> Unit
    ) {
        if (imageUris.isEmpty()) {
            onComplete(emptyList())
            return
        }

        val uploadedUrls = mutableListOf<String>()
        var successCount = 0
        val total = imageUris.size

        imageUris.forEach { uri ->
            MediaManager.get()
                .upload(uri)
                .option("folder", "findback/posts")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val imageUrl = resultData?.get("secure_url").toString()
                        uploadedUrls.add(imageUrl)
                        successCount++

                        if (successCount == total) {
                            onComplete(uploadedUrls)
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.e("CLOUDINARY", error?.description ?: "Upload failed")
                        _uploadState.value = UploadState.Error("Upload ảnh thất bại")
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                })
                .dispatch()
        }
    }

// =========================================================
// SAVE POST
// =========================================================

    private fun savePostToDatabase(post: Post) {
        var postToSave = post

        // Nếu có imageUrls thì gán imageUrl = ảnh đầu tiên
        if (post.imageUrls.isNotEmpty() && post.imageUrl.isEmpty()) {
            postToSave = post.copy(imageUrl = post.imageUrls.first())
        }

        // Lấy avatar nếu chưa có
        if (postToSave.userAvatar.isEmpty()) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            postToSave = postToSave.copy(
                userAvatar = currentUser?.photoUrl?.toString() ?: ""
            )
        }

        database.child(post.postId)
            .setValue(postToSave.toMap())
            .addOnSuccessListener {
                _uploadState.value = UploadState.Success
                loadPosts()
            }
            .addOnFailureListener { exception ->
                _uploadState.value = UploadState.Error(
                    "Lưu bài viết thất bại: ${exception.message}"
                )
            }
    }
    fun loadPosts() {
        repository.getPosts { posts: List<Post> ->

            Log.d("PostViewModel", "Load thành công ${posts.size} bài viết")

            posts.forEach { post ->
                Log.d(
                    "PostViewModel",
                    "Post: ${post.title} | Image: ${post.imageUrl} | Avatar: ${post.userAvatar}"
                )
            }

            val validPosts = posts.map { post ->
                if (post.imageUrl.isEmpty() || post.userAvatar.isEmpty()) {
                    Log.w("PostViewModel", "Post ${post.title} thiếu ảnh/avatar")
                }
                post
            }

            _postsShared.value = validPosts
        }
    }

    fun refreshPosts() {
        loadPosts()
    }

    // =========================================================
    // CLEAN UP
    // =========================================================

    override fun onCleared() {
        super.onCleared()

        repository.removeListener()
    }

    fun removeListener() {
        repository.removeListener()
    }
}