package com.example.myapp.screens

/** @file PointsDetail.kt
 * @brief Màn hình chi tiết điểm tích lũy của người dùng.
 *
 * Hiển thị danh sách đơn hàng đã hoàn thành và cho phép người dùng
 * nhấn vào từng đơn hàng để xem chi tiết đánh giá.
 */

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
 * Màn hình chi tiết điểm tích lũy.
 *
 * Hiển thị tối đa 4 đơn hàng đã hoàn thành dưới dạng thẻ (CardView).
 * Mỗi thẻ hiển thị tên đơn hàng và điểm tích lũy tương ứng.
 * Người dùng có thể nhấn vào thẻ để xem chi tiết đơn hàng.
 * Thanh điều hướng dưới cùng cho phép chuyển đến Trang chủ, Yêu thích, Giỏ hàng và Cá nhân.
 */
class PointsDetail : AppCompatActivity() {
    /** ID của đơn hàng thứ nhất */
    private var firstOrderId: Int? = null
    /** ID của đơn hàng thứ hai */
    private var secondOrderId: Int? = null
    /** ID của đơn hàng thứ ba */
    private var thirdOrderId: Int? = null
    /** ID của đơn hàng thứ tư */
    private var fourthOrderId: Int? = null

    /**
     * Khởi tạo màn hình chi tiết điểm tích lũy.
     *
     * Tải danh sách đơn hàng, thiết lập sự kiện nhấn cho các thẻ điểm
     * và thanh điều hướng dưới cùng.
     *
     * @param savedInstanceState Trạng thái đã lưu trước đó (nếu có).
     */
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

    /**
     * Tải danh sách đơn hàng từ API.
     *
     * Gọi API lấy đơn hàng theo userId, lọc các đơn đã hoàn thành,
     * sắp xếp theo thời gian tạo giảm dần, và hiển thị lên 4 thẻ.
     */
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

    /**
     * Gán dữ liệu đơn hàng vào thẻ giao diện.
     *
     * Hiển thị tên đơn hàng và điểm tích lũy (tổng giá / 1000).
     * Nếu đơn hàng là null, hiển thị giá trị mặc định.
     *
     * @param titleId ID resource của TextView hiển thị tên đơn hàng.
     * @param pointsId ID resource của TextView hiển thị điểm tích lũy.
     * @param order Đối tượng đơn hàng cần hiển thị, có thể null.
     * @param fallbackTitlePrefix Tiền tố tiêu đề khi đơn hàng hợp lệ.
     */
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

    /**
     * Xác định ID người dùng cho màn hình điểm tích lũy.
     *
     * Đọc user_id từ SharedPreferences. Nếu không hợp lệ, trả về 1 làm mặc định.
     *
     * @return ID của người dùng hiện tại.
     */
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

    /**
     * Chuyển đến màn hình chi tiết đơn hàng.
     *
     * Truyền tên đơn hàng và mã đơn hàng sang màn hình OrderTrackingDetail.
     * Hiển thị Toast nếu thiếu mã đơn hàng.
     *
     * @param dishName Tên món ăn hoặc đơn hàng.
     * @param orderId ID đơn hàng, có thể null.
     */
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
