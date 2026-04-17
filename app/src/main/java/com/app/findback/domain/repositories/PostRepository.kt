package com.app.findback.domain.repositories

import com.app.findback.domain.repositories.model.Post

interface PostRepository {
    fun getPosts(onData: (List<Post>) -> Unit)
    fun removeListener()
    fun createPost(onSuccess: (Boolean) -> Unit, post: Post)
}