package com.app.findback.domain.repository

import android.net.Uri
import com.app.findback.domain.models.Post

interface PostRepository {

    fun getPosts(
        onData: (List<Post>) -> Unit
    )

    fun createPost(
        post: Post,
        imageUris: List<Uri>,
        onSuccess: (Boolean) -> Unit
    )

    fun removeListener()
}