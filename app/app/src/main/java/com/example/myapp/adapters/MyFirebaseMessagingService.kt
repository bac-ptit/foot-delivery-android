package com.example.myapp.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapp.R
import com.example.myapp.screens.api.FcmTokenRegistrar
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Hàm này chạy khi có thông báo bay tới
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 1. Lấy dữ liệu tiêu đề và nội dung
        val title = remoteMessage.notification?.title ?: "Thông báo từ Trung"
        val body = remoteMessage.notification?.body ?: "Nội dung trống"

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "food_delivery_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 2. Tạo Kênh (Channel) cho Android đời mới
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Thông báo đơn hàng",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh nhận thông báo đặt đồ ăn"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 3. Xây dựng giao diện thông báo
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.pngwing) // Dùng icon pngwing bạn đã có
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // 4. Hiển thị lên màn hình
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // Hàm này chạy khi Firebase cấp Token mới (dùng để lưu vào Database)
    override fun onNewToken(token: String) {
        Log.d("FCM_TOKEN", "Token mới nè: $token")
        FcmTokenRegistrar.cacheToken(this, token)
        FcmTokenRegistrar.syncPendingToken(this, "onNewToken")
    }
}