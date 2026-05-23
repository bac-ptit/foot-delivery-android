package com.example.myapp.screens

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

class payment_successful : AppCompatActivity() {
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