package com.app.findback.data.repositories

import android.net.Uri

import com.app.findback.domain.models.Post
import com.app.findback.domain.repository.PostRepository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostRepositoryImpl : PostRepository {

    // ===================================================
    // FIREBASE
    // ===================================================

    private val postsRef =
        FirebaseDatabase.getInstance()
            .reference
            .child("posts")

    private var listener:
            ValueEventListener? = null

    // ===================================================
    // LOAD POSTS
    // ===================================================

    override fun getPosts(
        onData: (List<Post>) -> Unit
    ) {

        listener =
            object : ValueEventListener {

                override fun onDataChange(
                    snapshot: DataSnapshot
                ) {

                    val posts =
                        snapshot.children.mapNotNull { child ->

                            val map =
                                child.value
                                        as? Map<String, Any?>
                                    ?: return@mapNotNull null

                            Post.fromMap(map)
                        }

                            .sortedByDescending {
                                it.createdAt
                            }

                    onData(posts)
                }

                override fun onCancelled(
                    error: DatabaseError
                ) {

                    onData(emptyList())
                }
            }

        postsRef.addValueEventListener(listener!!)
    }

    // ===================================================
    // CREATE POST
    // ===================================================

    override fun createPost(
        post: Post,
        imageUris: List<Uri>,
        onSuccess: (Boolean) -> Unit
    ) {

        val postToSave =

            if (post.postId.isBlank()) {

                val newId =
                    postsRef.push().key
                        ?: return onSuccess(false)

                post.copy(
                    postId = newId
                )

            } else {
                post
            }

        postsRef
            .child(postToSave.postId)

            .setValue(postToSave)

            .addOnSuccessListener {

                onSuccess(true)
            }

            .addOnFailureListener {

                onSuccess(false)
            }
    }

    // ===================================================
    // REMOVE LISTENER
    // ===================================================

    override fun removeListener() {

        listener?.let {

            postsRef.removeEventListener(it)
        }

        listener = null
    }
}