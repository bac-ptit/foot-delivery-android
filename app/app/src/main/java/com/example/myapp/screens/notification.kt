package com.example.myapp.screens

/**
 * @file notification.kt
 * @brief Màn hình thông báo của ứng dụng giao đồ ăn.
 *
 * Hiển thị danh sách thông báo của người dùng dạng RecyclerView.
 * Hỗ trợ đánh dấu thông báo đã đọc khi người dùng nhấn vào.
 * Hiển thị trạng thái trống khi không có thông báo.
 */

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.adapters.NotificationAdapter
import com.example.myapp.screens.api.Notification
import com.example.myapp.screens.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity màn hình thông báo.
 *
 * Chức năng chính:
 * - Tải danh sách thông báo từ API theo userId
 * - Hiển thị thông báo dạng RecyclerView
 * - Đánh dấu thông báo đã đọc khi nhấn vào
 * - Hiển thị trạng thái trống hoặc lỗi khi cần
 */
class notification : AppCompatActivity() {

    /** RecyclerView hiển thị danh sách thông báo */
    private lateinit var rvNotifications: RecyclerView
    /** TextView hiển thị khi danh sách thông báo trống */
    private lateinit var tvEmpty: TextView
    /** Adapter cho RecyclerView thông báo */
    private lateinit var adapter: NotificationAdapter
    /** Danh sách mutable chứa các thông báo hiện tại */
    private val notifications = mutableListOf<Notification>()

    /**
     * Khởi tạo màn hình thông báo.
     *
     * Thiết lập RecyclerView với NotificationAdapter, gán sự kiện click
     * cho nút quay lại, và gọi API tải danh sách thông báo.
     *
     * @param savedInstanceState Trạng thái đã lưu của Activity (nếu có)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification)

        // Khởi tạo views
        rvNotifications = findViewById(R.id.rvNotifications)
        tvEmpty = findViewById(R.id.tvEmpty)

        // Setup RecyclerView
        adapter = NotificationAdapter(notifications) { notification ->
            // Xử lý khi click vào thông báo
            markAsRead(notification)
        }
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter

        // Click btnBack
        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish() // quay lại màn hình trước
        }

        // Load thông báo
        loadNotifications()
    }

    /**
     * Tải danh sách thông báo từ API.
     *
     * Lấy userId từ SharedPreferences, gọi API getUserNotifications.
     * Cập nhật danh sách và hiển thị trạng thái trống nếu không có thông báo.
     * Hiển thị lỗi nếu kết nối thất bại.
     */
    private fun loadNotifications() {
        // Lấy userId từ SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1) // Mặc định là 1 nếu không tìm thấy

        android.util.Log.d("Notification", "Loading notifications for userId: $userId")
        android.util.Log.d("Notification", "Base URL: ${RetrofitClient.apiService.javaClass.name}")

        RetrofitClient.apiService.getUserNotifications(userId).enqueue(object : Callback<List<Notification>> {
            override fun onResponse(call: Call<List<Notification>>, response: Response<List<Notification>>) {
                android.util.Log.d("Notification", "Response code: ${response.code()}")
                android.util.Log.d("Notification", "Response body: ${response.body()}")
                
                if (response.isSuccessful) {
                    val notificationList = response.body() ?: emptyList()
                    android.util.Log.d("Notification", "Notifications count: ${notificationList.size}")
                    
                    notifications.clear()
                    notifications.addAll(notificationList)
                    adapter.notifyDataSetChanged()

                    // Hiển thị thông báo trống nếu không có thông báo
                    if (notifications.isEmpty()) {
                        tvEmpty.text = "Không có thông báo"
                        tvEmpty.visibility = View.VISIBLE
                        rvNotifications.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        rvNotifications.visibility = View.VISIBLE
                    }
                } else {
                    android.util.Log.e("Notification", "Error: ${response.code()} - ${response.message()}")
                    tvEmpty.text = "Lỗi tải thông báo: ${response.code()}"
                    tvEmpty.visibility = View.VISIBLE
                    rvNotifications.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                android.util.Log.e("Notification", "Failure: ${t.message}", t)
                tvEmpty.text = "Lỗi kết nối: ${t.message}"
                tvEmpty.visibility = View.VISIBLE
                rvNotifications.visibility = View.GONE
            }
        })
    }

    /**
     * Đánh dấu một thông báo là đã đọc.
     *
     * Chỉ gửi yêu cầu API nếu thông báo chưa được đọc (isread == false).
     * Sau khi đánh dấu thành công, cập nhật trạng thái trong danh sách
     * và thông báo cho adapter.
     *
     * @param notification Thông báo cần đánh dấu đã đọc
     */
    private fun markAsRead(notification: Notification) {
        if (!notification.isread) {
            RetrofitClient.apiService.markNotificationAsRead(notification.id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        // Cập nhật trạng thái trong danh sách
                        val index = notifications.indexOfFirst { it.id == notification.id }
                        if (index != -1) {
                            notifications[index] = notification.copy(isread = true)
                            adapter.notifyItemChanged(index)
                        }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    // Xử lý lỗi nếu cần
                }
            })
        }
    }
}