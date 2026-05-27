package com.example.myapp.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import android.widget.Toast
import com.example.myapp.R
import com.example.myapp.screens.api.OrderResponse
import com.example.myapp.screens.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Màn hình chi tiết tích điểm khách hàng.
 *
 * Hiển thị tối đa 4 đơn hàng đã hoàn thành gần nhất.
 * Mỗi đơn hiển thị mã đơn và điểm tích lũy (totalprice / 1000).
 * Nhấn vào đơn → chuyển đến [OrderTrackingDetail].
 *
 * Cách tính điểm:
 * - Điểm mỗi đơn = totalprice / 1000 (hiển thị trên client)
 * - Tổng điểm = total_spent / 10000 (tính từ backend)
 *
 * @see OrderTrackingDetail
 * @see OrderTracking
 */
class PointsDetail : AppCompatActivity() {
    private var firstOrderId: Int? = null
    private var secondOrderId: Int? = null
    private var thirdOrderId: Int? = null
    private var fourthOrderId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.points_detail)

        loadOrders()

        // Back Button - Return to previous screen
        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        // Points Card 1 - Click to view order detail
        val pointsCard1: CardView = findViewById(R.id.pointsCard1)
        val tvDishName1: TextView = findViewById(R.id.tvDishName1)
        pointsCard1.setOnClickListener {
            navigateToOrderDetail(tvDishName1.text.toString(), firstOrderId)
        }

        // Points Card 2 - Click to view order detail
        val pointsCard2: CardView = findViewById(R.id.pointsCard2)
        val tvDishName2: TextView = findViewById(R.id.tvDishName2)
        pointsCard2.setOnClickListener {
            navigateToOrderDetail(tvDishName2.text.toString(), secondOrderId)
        }

        // Points Card 3 - Click to view order detail
        val pointsCard3: CardView = findViewById(R.id.pointsCard3)
        val tvDishName3: TextView = findViewById(R.id.tvDishName3)
        pointsCard3.setOnClickListener {
            navigateToOrderDetail(tvDishName3.text.toString(), thirdOrderId)
        }

        // Points Card 4 - Click to view order detail
        val pointsCard4: CardView = findViewById(R.id.pointsCard4)
        val tvDishName4: TextView = findViewById(R.id.tvDishName4)
        pointsCard4.setOnClickListener {
            navigateToOrderDetail(tvDishName4.text.toString(), fourthOrderId)
        }

        // Bottom Navigation - Home
        val icHome: ImageView = findViewById(R.id.icHome)
        icHome.setOnClickListener {
            startActivity(Intent(this, home::class.java))
        }

        // Bottom Navigation - Wishlist
        val icHeart: ImageView = findViewById(R.id.icHeart)
        icHeart.setOnClickListener {
            android.util.Log.d("PointsDetail", "Wishlist clicked")
        }

        // Bottom Navigation - Cart
        val icCart: ImageView = findViewById(R.id.icCart)
        icCart.setOnClickListener {
            startActivity(Intent(this, activity_cart::class.java))
        }

        // Bottom Navigation - Profile
        val icProfile: ImageView = findViewById(R.id.icProfile)
        icProfile.setOnClickListener {
            startActivity(Intent(this, profile::class.java))
        }
    }

    private fun loadOrders() {
        val userId = resolveUserIdForPoints()
        RetrofitClient.apiService.getUserOrders(userId).enqueue(object : Callback<List<OrderResponse>> {
            override fun onResponse(call: Call<List<OrderResponse>>, response: Response<List<OrderResponse>>) {
                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    Toast.makeText(this@PointsDetail, "Không có đơn hàng để đánh giá", Toast.LENGTH_SHORT).show()
                    return
                }

                val completedOrders = response.body()!!
                    .filter { it.status == "completed" }
                    .sortedByDescending { it.createdat }

                firstOrderId = completedOrders.getOrNull(0)?.id
                secondOrderId = completedOrders.getOrNull(1)?.id
                thirdOrderId = completedOrders.getOrNull(2)?.id
                fourthOrderId = completedOrders.getOrNull(3)?.id

                bindOrderCard(R.id.tvDishName1, R.id.tvPoints1, completedOrders.getOrNull(0), "Đơn hàng #")
                bindOrderCard(R.id.tvDishName2, R.id.tvPoints2, completedOrders.getOrNull(1), "Đơn hàng #")
                bindOrderCard(R.id.tvDishName3, R.id.tvPoints3, completedOrders.getOrNull(2), "Đơn hàng #")
                bindOrderCard(R.id.tvDishName4, R.id.tvPoints4, completedOrders.getOrNull(3), "Đơn hàng #")
            }

            override fun onFailure(call: Call<List<OrderResponse>>, t: Throwable) {
                Toast.makeText(this@PointsDetail, "Không tải được đơn hàng", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun bindOrderCard(titleId: Int, pointsId: Int, order: OrderResponse?, fallbackTitlePrefix: String) {
        val titleView: TextView = findViewById(titleId)
        val pointsView: TextView = findViewById(pointsId)

        if (order == null) {
            titleView.text = "Chưa có đơn hàng"
            pointsView.text = "0"
            return
        }

        titleView.text = "$fallbackTitlePrefix${order.id}"
        pointsView.text = (order.totalprice / 1000).toString()
    }

    private fun resolveUserIdForPoints(): Int {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        if (userId > 0) return userId

        val userName = sharedPref.getString("user_name", "") ?: ""
        if (userName.contains("Trung", ignoreCase = true) || userName.isBlank()) {
            return 1
        }
        return 1
    }

    private fun navigateToOrderDetail(dishName: String, orderId: Int?) {
        if (orderId == null) {
            Toast.makeText(this, "Thiếu mã đơn hàng để đánh giá", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, OrderTrackingDetail::class.java)
        intent.putExtra("order_name", dishName)
        intent.putExtra("order_id", orderId)
        startActivity(intent)
    }
}
