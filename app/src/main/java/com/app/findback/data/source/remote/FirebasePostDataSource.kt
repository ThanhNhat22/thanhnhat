package com.app.findback.data.source.remote

import android.util.Log
import com.app.findback.domain.repositories.model.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebasePostDataSource {
    private val database = FirebaseDatabase.getInstance().getReference("posts")
    private var listener: ValueEventListener? = null

    //hàm tạo post
    fun createPost(onSuccess: (Boolean) -> Unit, post: Post) {

        val postKey = post.postId?.takeIf { it.isNotBlank() }
            ?: database.push().key
            ?: System.currentTimeMillis().toString()

        val postToSave = post.copy(postId = postKey)

        database.child(postKey)
            .setValue(postToSave.toMap())
            .addOnSuccessListener {
                onSuccess(true)
            }
            .addOnFailureListener { error ->
                onSuccess(false)
            }
    }
    //lấy post realtime
    fun getPosts(onData: (List<Post>) -> Unit) {
        //tránh gọi nhiều lần
        if (listener != null) return
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children.mapNotNull {
                    (it.value as? Map<String, Any?>)?.let(Post::fromMap)
                }
                //trả data về
                onData(posts)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebasePostDataSource", "Listen posts cancelled: ${error.message}", error.toException())
            }
        }
        database.addValueEventListener(listener!!)
    }

    //Dừng lắng nghe sự kiện thay đổi dữ liệu
    fun stopListening() {
        listener?.let {
            database.removeEventListener(it)
            listener = null
        }
    }
}