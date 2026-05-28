package com.example.myapp.adapters

/**
 * @file NotificationAdapter.kt
 * @brief Adapter hiển thị danh sách thông báo, hỗ trợ phân biệt trạng thái đã đọc/chưa đọc.
 */

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.screens.api.Notification

/**
 * Adapter hiển thị danh sách thông báo trong RecyclerView.
 * Hỗ trợ phân biệt trạng thái đã đọc/chưa đọc bằng indicator và màu nền.
 *
 * @property notifications Danh sách thông báo cần hiển thị.
 * @property onItemClick Callback được gọi khi người dùng nhấn vào một thông báo.
 */
class NotificationAdapter(
    private val notifications: List<Notification>,
    private val onItemClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    /**
     * ViewHolder chứa các view hiển thị thông tin của một thông báo.
     *
     * @property tvTitle TextView hiển thị tiêu đề thông báo.
     * @property tvContent TextView hiển thị nội dung thông báo.
     * @property tvTime TextView hiển thị thời gian gửi thông báo.
     * @property unreadIndicator View đánh dấu trạng thái chưa đọc.
     * @property container View gốc của item, dùng để thay đổi màu nền theo trạng thái.
     */
    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvNotificationContent)
        val tvTime: TextView = itemView.findViewById(R.id.tvNotificationTime)
        val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)
        val container: View = itemView.findViewById(R.id.notificationContainer)
    }

    /**
     * Tạo ViewHolder mới bằng cách inflate layout item_notification.
     *
     * @param parent ViewGroup cha chứa RecyclerView.
     * @param viewType Loại view (không sử dụng trong trường hợp này).
     * @return Đối tượng NotificationViewHolder mới được tạo.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    /**
     * Gán dữ liệu thông báo vào ViewHolder tại vị trí cho trước.
     * Hiển thị tiêu đề, nội dung, thời gian và trạng thái đã đọc/chưa đọc.
     *
     * @param holder ViewHolder cần gán dữ liệu.
     * @param position Vị trí của item trong danh sách.
     */
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        holder.tvTitle.text = notification.title
        holder.tvContent.text = notification.content
        holder.tvTime.text = formatTime(notification.createdat)

        // Hiển thị indicator nếu chưa đọc
        if (!notification.isread) {
            holder.unreadIndicator.visibility = View.VISIBLE
            holder.container.setBackgroundColor(android.graphics.Color.parseColor("#FFF5F5F5"))
        } else {
            holder.unreadIndicator.visibility = View.GONE
            holder.container.setBackgroundColor(android.graphics.Color.WHITE)
        }

        holder.itemView.setOnClickListener {
            onItemClick(notification)
        }
    }

    /**
     * Trả về tổng số thông báo trong danh sách.
     *
     * @return Số lượng thông báo.
     */
    override fun getItemCount(): Int = notifications.size

    /**
     * Chuyển đổi định dạng thời gian từ API (yyyy-MM-dd'T'HH:mm:ss)
     * sang định dạng hiển thị (dd/MM/yyyy HH:mm).
     *
     * @param dateTime Chuỗi thời gian đầu vào theo định dạng ISO 8601.
     * @return Chuỗi thời gian đã được định dạng, hoặc chuỗi gốc nếu parse thất bại.
     */
    private fun formatTime(dateTime: String): String {
        // Chuyển đổi định dạng thời gian từ API sang định dạng hiển thị
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateTime)
            outputFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            dateTime
        }
    }
}
