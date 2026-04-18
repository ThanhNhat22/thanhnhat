package com.app.findback.domain.repository

import com.app.findback.domain.models.Post

interface PostRepository {
    fun getPosts(onData: (List<Post>) -> Unit)
    fun removeListener()
    fun createPost(onSuccess: (Boolean) -> Unit, post: Post)
}