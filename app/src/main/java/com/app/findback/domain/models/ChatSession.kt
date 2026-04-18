package com.app.findback.domain.models

data class ChatSession(
    val userId: String = "",
    val messages: MutableList<ChatMessage>  = mutableListOf()
)