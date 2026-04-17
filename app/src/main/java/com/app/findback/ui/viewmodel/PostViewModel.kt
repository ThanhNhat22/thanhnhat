package com.app.findback.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.findback.data.repositories.PostRepositoryImpl
import com.app.findback.domain.repositories.PostRepository
import com.app.findback.domain.repositories.model.Post

class PostViewModel : ViewModel() {
    private val postRepository: PostRepository = PostRepositoryImpl()
    private val _posts = MutableLiveData<List<Post>>(emptyList())
    val postsShared: LiveData<List<Post>> = _posts

   //get list post
    fun getPosts(){
        postRepository.getPosts { newPosts ->
            _posts.postValue(newPosts)
        }
    }
    //remove listener
    fun removeListener() {
        postRepository.removeListener()
    }
    //create post
    fun createPost(post: Post, onResult: (Boolean) -> Unit) {
        Log.d("test","createPost: ${post.toMap()}")

        postRepository.createPost(onResult , post)
    }

    override fun onCleared() {
        postRepository.removeListener()
        super.onCleared()
    }
}