package com.app.findback.data.source.gemini

import com.app.findback.data.source.remote.FirebaseChatAiDataSource
import com.app.findback.domain.models.ChatMessage
import com.app.findback.domain.models.ChatSession
import com.app.findback.domain.models.Post
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiChatService {
    private val firebase = FirebaseChatAiDataSource()
    private val apiKey = ""
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    suspend fun sendMessage(
        session: ChatSession,
        userMessage: String,
        posts: List<Post>
    ){

        // chạy toàn bộ logic nặng trong IO dispatcher
        withContext(Dispatchers.IO) {
            try {
                val time = System.currentTimeMillis()

                // user message
                val userMsg = ChatMessage(
                    id = time.toString(),
                    content = userMessage,
                    isUser = true,
                    timestamp = time
                )

                // lưu Firebase (non-blocking callback)
                firebase.sendMessage(session.userId, userMsg)

                // giữ local
                session.messages.add(userMsg)

                // BUILD PROMPT
                val prompt = buildChatPrompt(session, posts)

                // CALL
                val aiText: String = try {
                    val response = model.generateContent(prompt)
                    response.text ?: "Xin lỗi, tôi chưa hiểu câu hỏi."
                } catch (e: Exception) {
                    e.printStackTrace()
                    "Xin lỗi, hiện tại không thể kết nối tới dịch vụ AI. Vui lòng thử lại sau. ${e.toString()}"
                }

                // AI MESSAGE
                val aiTime = System.currentTimeMillis()
                val aiMsg = ChatMessage(
                    id = aiTime.toString(),
                    content = aiText,
                    isUser = false,
                    timestamp = aiTime
                )

                // lưu Firebase cho AI message (non-blocking)
                firebase.sendMessage(session.userId, aiMsg)

            } catch (e: Exception) {
                // bắt mọi lỗi bất ngờ để tránh crash
                e.printStackTrace()
                // (tuỳ) bạn có thể post lỗi lên ViewModel để UI thông báo
            }
        }
    }
    //xây dựng prompt chuẩn
    private fun buildChatPrompt(
        session: ChatSession,
        posts: List<Post>
    ): String {

        val history = session.messages.joinToString("\n") {
            if (it.isUser) {
                "User: ${it.content}"
            } else {
                "AI: ${it.content}"
            }
        }

        val postsJson = Gson().toJson(posts)

        return """
        Bạn là AI của app FindBack (tìm đồ thất lạc).
        
        DỮ LIỆU các bài đăng : 
        $postsJson
        
        LỊCH SỬ CHAT:
        $history
        
        NHIỆM VỤ:
        - Trả lời thân thiện bằng tiếng Việt
        - Gợi ý các bài viết phù hợp nếu có
        - Nếu không có, vẫn trả lời tự nhiên
        
        - nếu user hỏi thấy bài đăng nào đăng sẩn gì so sách với dữ liệu user đưa vào thì trả về id bài đăng đó
        
        KHÔNG cần trả JSON nữa, chỉ trả lời như chat bình thường.
    """.trimIndent()
    }
}