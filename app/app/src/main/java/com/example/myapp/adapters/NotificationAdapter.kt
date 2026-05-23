package com.example.myapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.screens.api.Notification

class NotificationAdapter(
    private val notifications: List<Notification>,
    private val onItemClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvNotificationContent)
        val tvTime: TextView = itemView.findViewById(R.id.tvNotificationTime)
        val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)
        val container: View = itemView.findViewById(R.id.notificationContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

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

    override fun getItemCount(): Int = notifications.size

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
