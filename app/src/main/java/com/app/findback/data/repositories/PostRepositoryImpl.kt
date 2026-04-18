package com.app.findback.data.repositories


import com.app.findback.domain.models.Post
import com.app.findback.data.source.remote.FirebasePostDataSource
import com.app.findback.domain.repository.PostRepository

class PostRepositoryImpl(
    private val firebasePostDataSource: FirebasePostDataSource = FirebasePostDataSource()
) : PostRepository {
    override fun getPosts(onData: (List<Post>) -> Unit) {
        firebasePostDataSource.getPosts(onData)
    }
    override fun removeListener() {
        firebasePostDataSource.stopListening()
    }
    override fun createPost(onSuccess: (Boolean) -> Unit, post: Post){
        return firebasePostDataSource.createPost(onSuccess,post)
    }
}

