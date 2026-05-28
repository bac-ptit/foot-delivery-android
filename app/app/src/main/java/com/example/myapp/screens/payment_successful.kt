package com.example.myapp.screens

/**
 * @file payment_successful.kt
 * @brief Màn hình thanh toán thành công.
 *
 * Hiển thị thông báo thanh toán thành công sau khi hoàn tất đơn hàng.
 * Nếu là thanh toán COD, tự động cập nhật trạng thái đơn hàng thành "confirmed"
 * để backend gửi thông báo. Cung cấp nút trở về trang chủ.
 */

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.example.myapp.screens.api.OrderResponse
import com.example.myapp.screens.api.OrderStatusUpdateRequest
import com.example.myapp.screens.api.RetrofitClient
import com.example.myapp.screens.home
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity hiển thị màn hình thanh toán thành công.
 *
 * Nhận `order_id` từ Intent. Nếu là đơn COD, cập nhật trạng thái
 * đơn hàng thành "confirmed" để kích hoạt gửi thông báo từ backend.
 * Cung cấp nút "Trở về trang chủ" xóa toàn bộ Activity stack.
 */
class payment_successful : AppCompatActivity() {
    /**
     * Khởi tạo Activity, cập nhật trạng thái đơn COD và thiết lập giao diện.
     *
     * Nếu order_id hợp lệ, gọi API cập nhật trạng thái thành "confirmed".
     * Thiết lập nút trở về trang chủ với FLAG_ACTIVITY_CLEAR_TASK,
     * các nút điều hướng (profile, home, giỏ hàng).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payment_successful)

        val orderId = intent.getIntExtra("order_id", -1)
        
        // Nếu là COD (payment từ order.kt), cập nhật status thành "confirmed" để gửi thông báo
        if (orderId > 0) {
            updateOrderStatusToCODConfirmed(orderId)
        }

        // Tìm nút "Trở về trang chủ" theo ID btnReturnHome
        val btnReturnHome: Button = findViewById(R.id.btnReturnHome)

        // Thiết lập sự kiện click
        btnReturnHome.setOnClickListener {
            // Chuyển về màn hình Home
            val intent = Intent(this, home::class.java)
            // Xóa hết các Activity cũ để khi nhấn Back ở Home không bị quay lại trang thành công
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        //        click icProfile
        val icProfile: ImageView = findViewById(R.id.icProfile)
        icProfile.setOnClickListener {
            val intent = Intent(this, profile::class.java)
            startActivity(intent)
        }

        val icHome: ImageView = findViewById(R.id.icHome)
        icHome.setOnClickListener {
            val intent = Intent(this, home::class.java)
            startActivity(intent)
        }

        val btnCart: ImageView = findViewById(R.id.icCart)
        btnCart.setOnClickListener {
            val intent = Intent(this, cart::class.java)
            startActivity(intent)
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng COD thành "confirmed".
     *
     * Gọi API updateOrderStatus để xác nhận đơn hàng COD,
     * từ đó backend sẽ gửi thông báo đến khách hàng.
     * Hiển thị Toast nếu cập nhật thất bại.
     *
     * @param orderId ID đơn hàng cần cập nhật trạng thái
     */
    private fun updateOrderStatusToCODConfirmed(orderId: Int) {
        RetrofitClient.apiService.updateOrderStatus(orderId, OrderStatusUpdateRequest("confirmed"))
            .enqueue(object : Callback<OrderResponse> {
                override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                    if (response.isSuccessful) {
                        // Thông báo sẽ được gửi từ backend
                    } else {
                        Toast.makeText(this@payment_successful, "Cập nhật đơn hàng thất bại", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                    Toast.makeText(this@payment_successful, "Lỗi mạng", Toast.LENGTH_SHORT).show()
                }
            })
    }
}