package com.app.findback.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.findback.domain.repository.MessageRepository

class MessageViewModelFactory(
    private val repository: MessageRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessageViewModel::class.java))
            return MessageViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}