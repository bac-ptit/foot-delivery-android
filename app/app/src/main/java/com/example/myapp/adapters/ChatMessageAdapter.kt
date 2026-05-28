package com.example.myapp.screens.adapters

/**
 * @file ChatMessageAdapter.kt
 * @brief Adapter hiển thị tin nhắn chat, hỗ trợ phân biệt tin nhắn người dùng và bot.
 */

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R

/**
 * Data class đại diện cho một tin nhắn trong cuộc trò chuyện.
 *
 * @property message Nội dung tin nhắn.
 * @property isUser True nếu tin nhắn của người dùng, false nếu của bot.
 * @property timestamp Thời gian gửi tin nhắn (định dạng chuỗi, mặc định rỗng).
 */
data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val timestamp: String = ""
)

/**
 * Adapter hiển thị danh sách tin nhắn chat, phân biệt layout giữa tin nhắn
 * của người dùng và tin nhắn của bot.
 *
 * @property messages Danh sách có thể thay đổi chứa các tin nhắn chat.
 */
class ChatMessageAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    /** Hằng số xác định loại view cho tin nhắn người dùng và bot. */
    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
    }

    /**
     * ViewHolder chứa các view hiển thị một tin nhắn chat.
     *
     * @property tvMessage TextView hiển thị nội dung tin nhắn.
     * @property ivAvatar ImageView hiển thị avatar của người gửi.
     */
    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
    }

    /**
     * Xác định loại view dựa trên tin nhắn tại vị trí cho trước.
     *
     * @param position Vị trí của tin nhắn trong danh sách.
     * @return VIEW_TYPE_USER nếu là tin nhắn người dùng, VIEW_TYPE_BOT nếu là tin nhắn bot.
     */
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    /**
     * Tạo ViewHolder mới với layout phù hợp dựa trên loại view.
     *
     * @param parent ViewGroup cha chứa RecyclerView.
     * @param viewType Loại view (VIEW_TYPE_USER hoặc VIEW_TYPE_BOT).
     * @return Đối tượng MessageViewHolder mới được tạo.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_USER) {
            R.layout.item_chat_user
        } else {
            R.layout.item_chat_bot
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    /**
     * Gán nội dung tin nhắn vào ViewHolder tại vị trí cho trước.
     *
     * @param holder ViewHolder cần gán dữ liệu.
     * @param position Vị trí của tin nhắn trong danh sách.
     */
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.tvMessage.text = message.message
    }

    /**
     * Trả về tổng số tin nhắn trong danh sách.
     *
     * @return Số lượng tin nhắn.
     */
    override fun getItemCount() = messages.size

    /**
     * Thêm một tin nhắn mới vào cuối danh sách và thông báo cho RecyclerView
     * để hiển thị item mới.
     *
     * @param message Tin nhắn cần thêm vào danh sách.
     */
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}
