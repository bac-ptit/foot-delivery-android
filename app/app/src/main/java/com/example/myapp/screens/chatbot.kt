package com.example.myapp.screens
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.screens.adapters.ChatMessage
import com.example.myapp.screens.adapters.ChatMessageAdapter
import com.example.myapp.screens.api.RetrofitClient
import com.example.myapp.screens.api.ChatMessageRequest
import com.example.myapp.screens.api.ChatMessageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class chatbot : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatMessageAdapter
    private lateinit var edtMessage: EditText
    private var sessionId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chatbot)

        //        click btnBack
        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish() // quay lại màn hình trước (start)
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.chatRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Tin nhắn mới hiện ở dưới
        }

        // Khởi tạo adapter với tin nhắn chào mừng
        val initialMessages = mutableListOf<ChatMessage>(
            ChatMessage(
                message = "Chào bạn, mình là trợ lý ảo của ứng dụng đặt món. Mình có thể giúp gì cho đơn hàng của bạn hôm nay?",
                isUser = false
            )
        )
        adapter = ChatMessageAdapter(initialMessages)
        recyclerView.adapter = adapter

        // Setup input và send button
        edtMessage = findViewById(R.id.edtMessage)
        val btnSend: ImageView = findViewById(R.id.btnSend)
        btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val messageText = edtMessage.text.toString().trim()
        if (messageText.isEmpty()) {
            return
        }

        // Thêm tin nhắn người dùng vào RecyclerView
        adapter.addMessage(ChatMessage(message = messageText, isUser = true))
        recyclerView.scrollToPosition(adapter.itemCount - 1)

        // Xóa input
        edtMessage.text.clear()

        // Gọi API chat
        val request = ChatMessageRequest(message = messageText)
        RetrofitClient.apiService.sendMessage(request, sessionId).enqueue(object : Callback<ChatMessageResponse> {
            override fun onResponse(call: Call<ChatMessageResponse>, response: Response<ChatMessageResponse>) {
                if (response.isSuccessful) {
                    val chatResponse = response.body()
                    chatResponse?.let {
                        // Lưu session_id cho các tin nhắn tiếp theo
                        if (sessionId == null) {
                            sessionId = it.sessionid
                        }
                        // Thêm tin nhắn bot vào RecyclerView
                        adapter.addMessage(ChatMessage(message = it.message, isUser = false))
                        recyclerView.scrollToPosition(adapter.itemCount - 1)
                    }
                } else {
                    Toast.makeText(this@chatbot, 
                        "Lỗi: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ChatMessageResponse>, t: Throwable) {
                Toast.makeText(this@chatbot, 
                    "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}