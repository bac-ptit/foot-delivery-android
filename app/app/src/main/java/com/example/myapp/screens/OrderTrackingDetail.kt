package com.example.myapp.screens

/**
 * @file OrderTrackingDetail.kt
 * @brief Màn hình chi tiết theo dõi đơn hàng.
 *
 * Hiển thị thông tin chi tiết của một đơn hàng bao gồm: mã đơn, tổng tiền,
 * nhà hàng, trạng thái, địa chỉ giao hàng, ngày đặt, danh sách món ăn.
 * Cho phép đánh giá đơn hàng (1-5 sao và phản hồi) khi đơn đã hoàn thành.
 */

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.EditText
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import com.example.myapp.R
import com.example.myapp.screens.api.OrderDetailResponse
import com.example.myapp.screens.api.ReviewCreateRequest
import com.example.myapp.screens.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity màn hình chi tiết theo dõi đơn hàng.
 *
 * Chức năng chính:
 * - Hiển thị chi tiết đơn hàng: mã đơn, tổng tiền, nhà hàng, trạng thái,
 *   địa chỉ, ngày đặt, danh sách món ăn
 * - Hiển thị timeline trạng thái đơn hàng
 * - Đánh giá đơn hàng (1-5 sao + phản hồi) khi đã hoàn thành
 * - Điều hướng: Trang chủ, Yêu thích, Giỏ hàng, Hồ sơ
 */
class OrderTrackingDetail : AppCompatActivity() {
    /** Số sao đánh giá đã chọn (0-5) */
    private var selectedRating = 0

    /**
     * Khởi tạo màn hình chi tiết theo dõi đơn hàng.
     *
     * Nhận thông tin đơn hàng từ Intent, hiển thị timeline và đánh giá.
     * Vô hiệu hóa nút đánh giá nếu đơn chưa hoàn thành.
     * Tải chi tiết đơn hàng từ API và thiết lập các nút điều hướng.
     *
     * @param savedInstanceState Trạng thái đã lưu của Activity (nếu có)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_tracking_detail)

        // Get order information from Intent
        val orderName = intent.getStringExtra("order_name") ?: "Chi tiết đơn hàng"
        val orderId = intent.getIntExtra("order_id", -1)
        val orderStatus = intent.getStringExtra("order_status") ?: ""
        
        val tvTitle: TextView = findViewById(R.id.tvTitle)
        tvTitle.text = orderName

        val timelineContainer: View = findViewById(R.id.timelineContainer)
        val ratingContainer: View = findViewById(R.id.starratingContainer)
        val etFeedbackView: View = findViewById(R.id.etFeedback)
        val btnRateAction: AppCompatButton = findViewById(R.id.btnRate)

        val isCompleted = isCompletedStatus(orderStatus)
        
        // Luôn hiển thị đầy đủ các thành phần như trong file thiết kế XML
        timelineContainer.visibility = View.VISIBLE
        ratingContainer.visibility = View.VISIBLE
        etFeedbackView.visibility = View.VISIBLE
        btnRateAction.visibility = View.VISIBLE

        // Nếu đơn hàng chưa hoàn thành, có thể làm mờ hoặc vô hiệu hóa nút đánh giá (tùy chọn)
        if (!isCompleted) {
            btnRateAction.alpha = 0.5f
            btnRateAction.isEnabled = false
            btnRateAction.text = "Chờ giao hàng để đánh giá"
        }

        if (orderId > 0) {
            loadOrderSummary(orderId)
        }

        // Back Button - Return to previous screen
        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        val stars = listOf(
            findViewById<ImageView>(R.id.star1),
            findViewById<ImageView>(R.id.star2),
            findViewById<ImageView>(R.id.star3),
            findViewById<ImageView>(R.id.star4),
            findViewById<ImageView>(R.id.star5)
        )
        updateStarColors(stars)
        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                selectedRating = index + 1
                updateStarColors(stars)
            }
        }

        // Rate Button - Submit review/rating
        btnRateAction.setOnClickListener {
            val etFeedback: EditText = findViewById(R.id.etFeedback)
            val feedbackText = etFeedback.text.toString()
            val resolvedOrderId = intent.getIntExtra("order_id", -1)
            val userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1)

            if (resolvedOrderId <= 0) {
                android.widget.Toast.makeText(
                    this,
                    "Thiếu mã đơn hàng để gửi đánh giá",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (selectedRating <= 0) {
                android.widget.Toast.makeText(
                    this,
                    "Vui lòng chọn số sao",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (feedbackText.isBlank()) {
                android.widget.Toast.makeText(
                    this,
                    "Vui lòng nhập phản hồi",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (userId <= 0) {
                android.widget.Toast.makeText(
                    this,
                    "Không tìm thấy thông tin người dùng",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            btnRateAction.isEnabled = false
            RetrofitClient.apiService.createReview(
                ReviewCreateRequest(
                    rating = selectedRating,
                    comment = feedbackText,
                    orderid = resolvedOrderId,
                    userid = userId
                )
            ).enqueue(object : Callback<com.example.myapp.screens.api.ReviewResponse> {
                override fun onResponse(
                    call: Call<com.example.myapp.screens.api.ReviewResponse>,
                    response: Response<com.example.myapp.screens.api.ReviewResponse>
                ) {
                    btnRateAction.isEnabled = true
                    if (!response.isSuccessful) {
                        android.widget.Toast.makeText(
                            this@OrderTrackingDetail,
                            "Gửi đánh giá thất bại",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    android.widget.Toast.makeText(
                        this@OrderTrackingDetail,
                        "Cảm ơn bạn đã đánh giá!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                override fun onFailure(call: Call<com.example.myapp.screens.api.ReviewResponse>, t: Throwable) {
                    btnRateAction.isEnabled = true
                    android.widget.Toast.makeText(
                        this@OrderTrackingDetail,
                        "Không thể gửi đánh giá",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        // Bottom Navigation - Home
        val icHome: ImageView = findViewById(R.id.icHome)
        icHome.setOnClickListener {
            startActivity(Intent(this, home::class.java))
        }

        // Bottom Navigation - Wishlist
        val icHeart: ImageView = findViewById(R.id.icHeart)
        icHeart.setOnClickListener {
            // TODO: Navigate to favorites/wishlist page
            android.util.Log.d("OrderTracking", "Wishlist clicked")
        }

        // Bottom Navigation - Cart
        val icCart: ImageView = findViewById(R.id.icCart)
        icCart.setOnClickListener {
            startActivity(Intent(this, cart::class.java))
        }

        // Bottom Navigation - Profile
        val icProfile: ImageView = findViewById(R.id.icProfile)
        icProfile.setOnClickListener {
            startActivity(Intent(this, profile::class.java))
        }
    }

    /**
     * Tải tóm tắt đơn hàng từ API.
     *
     * Gọi API getOrderDetail, hiển thị mã đơn và danh sách món ăn
     * tóm tắt, sau đó gọi populateOrderDetails để điền chi tiết.
     *
     * @param orderId ID đơn hàng cần tải
     */
    private fun loadOrderSummary(orderId: Int) {
        val summaryView: TextView = findViewById(R.id.tvOrderSummary)
        RetrofitClient.apiService.getOrderDetail(orderId).enqueue(object : Callback<OrderDetailResponse> {
            override fun onResponse(call: Call<OrderDetailResponse>, response: Response<OrderDetailResponse>) {
                if (!response.isSuccessful || response.body() == null) {
                    summaryView.text = "Không tải được chi tiết đơn hàng"
                    return
                }

                val order = response.body()!!
                val totalQuantity = order.order_items.sumOf { it.quantity }
                val itemLines = order.order_items.joinToString(", ") { item ->
                    "${item.menuitem_name ?: "Món #${item.menuitemid}"} (x${item.quantity})"
                }
                summaryView.text = "Mã đơn: #${order.id} | Số món: $totalQuantity\n$itemLines"

                // Populate order details in timeline container
                populateOrderDetails(order)
            }

            override fun onFailure(call: Call<OrderDetailResponse>, t: Throwable) {
                summaryView.text = "Không tải được chi tiết đơn hàng"
            }
        })
    }

    /**
     * Điền thông tin chi tiết đơn hàng vào các view.
     *
     * Cập nhật: mã đơn, tổng tiền (định dạng VNĐ), tên nhà hàng,
     * trạng thái (dịch sang tiếng Việt), địa chỉ giao hàng, ngày đặt,
     * và danh sách chi tiết các món ăn.
     *
     * @param order Đối tượng OrderDetailResponse chứa chi tiết đơn hàng
     */
    private fun populateOrderDetails(order: OrderDetailResponse) {
        try {
            // Order ID
            findViewById<TextView>(R.id.tvOrderId).text = "#${order.id}"

            // Total Price - Format with thousands separator
            val priceFormatted = String.format("%,d", order.totalprice).replace(",", ".")
            findViewById<TextView>(R.id.tvTotalPrice).text = "${priceFormatted}đ"

            // Restaurant Name
            findViewById<TextView>(R.id.tvRestaurantName).text = order.restaurant_name ?: "--"

            // Order Status - Convert to Vietnamese
            val statusText = when (order.status.lowercase()) {
                "pending" -> "Chờ xác nhận"
                "confirmed" -> "Đã xác nhận"
                "paid" -> "Đã thanh toán"
                "delivering" -> "Đang giao"
                "completed" -> "Đã giao"
                "cancelled" -> "Đã hủy"
                else -> order.status
            }
            findViewById<TextView>(R.id.tvOrderStatus).text = statusText

            // Delivery Address
            findViewById<TextView>(R.id.tvDeliveryAddress).text = order.address_detail ?: "--"

            // Order Date - Format the date
            val dateFormatted = formatOrderDate(order.createdat)
            findViewById<TextView>(R.id.tvOrderDate).text = dateFormatted

            // Populate Items List
            populateItemsList(order.order_items)

        } catch (e: Exception) {
            android.util.Log.e("OrderTrackingDetail", "Error populating order details: ${e.message}")
        }
    }

    /**
     * Tạo và hiển thị danh sách chi tiết các món ăn trong đơn hàng.
     *
     * Tạo LinearLayout động cho mỗi item với tên món (x số lượng)
     * và giá tiền (định dạng VNĐ, màu cam).
     *
     * @param items Danh sách chi tiết các món ăn trong đơn hàng
     */
    private fun populateItemsList(items: List<com.example.myapp.screens.api.OrderItemDetailResponse>) {
        val itemsListContainer = findViewById<LinearLayout>(R.id.itemsListContainer)
        itemsListContainer.removeAllViews()

        for (item in items) {
            val itemLayout = LinearLayout(this)
            itemLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
            itemLayout.orientation = LinearLayout.HORIZONTAL
            itemLayout.gravity = android.view.Gravity.CENTER_VERTICAL

            // Item Name and Quantity
            val itemNameView = TextView(this)
            itemNameView.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            itemNameView.text = "${item.menuitem_name ?: "Món #${item.menuitemid}"} (x${item.quantity})"
            itemNameView.textSize = 13f
            itemNameView.setTextColor(Color.parseColor("#1f1f1f"))
            itemNameView.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)

            // Item Price
            val itemPriceView = TextView(this)
            itemPriceView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val price = item.price * item.quantity
            val priceFormatted = String.format("%,d", price).replace(",", ".")
            itemPriceView.text = "${priceFormatted}đ"
            itemPriceView.textSize = 13f
            itemPriceView.setTextColor(Color.parseColor("#ff8001"))
            itemPriceView.typeface = android.graphics.Typeface.DEFAULT_BOLD
            itemPriceView.setPadding(16, 0, 0, 0)

            itemLayout.addView(itemNameView)
            itemLayout.addView(itemPriceView)
            itemsListContainer.addView(itemLayout)
        }
    }

    /**
     * Định dạng chuỗi ngày đặt hàng từ ISO sang dd/MM/yyyy.
     *
     * @param dateString Chuỗi ngày dạng "yyyy-MM-ddTHH:mm:ss" hoặc null
     * @return Chuỗi ngày đã định dạng hoặc "--" nếu rỗng
     */
    private fun formatOrderDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "--"
        return try {
            // Expected format from backend: 2025-04-06T10:30:45
            val parts = dateString.split("T")
            if (parts.size >= 1) {
                val dateParts = parts[0].split("-")
                if (dateParts.size == 3) {
                    // Convert from 2025-04-06 to 06/04/2025
                    "${dateParts[2]}/${dateParts[1]}/${dateParts[0]}"
                } else {
                    dateString
                }
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Cập nhật màu sắc cho các ngôi sao đánh giá.
     *
     * Sao được chọn có màu vàng (#FFC107), sao chưa chọn có màu xám (#CFCFCF).
     *
     * @param stars Danh sách 5 ImageView ngôi sao
     */
    private fun updateStarColors(stars: List<ImageView>) {
        stars.forEachIndexed { index, star ->
            val color = if (index < selectedRating) "#FFC107" else "#CFCFCF"
            star.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN)
        }
    }

    /**
     * Kiểm tra trạng thái đơn hàng có phải đã hoàn thành hay không.
     *
     * @param status Chuỗi trạng thái (không phân biệt hoa thường)
     * @return true nếu trạng thái là "completed", "delivered" hoặc "done"
     */
    private fun isCompletedStatus(status: String?): Boolean {
        val normalized = (status ?: "").lowercase(java.util.Locale.ROOT)
        return normalized == "completed" || normalized == "delivered" || normalized == "done"
    }
}
