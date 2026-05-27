package com.example.myapp.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.LayoutInflater
import com.example.myapp.R
import com.example.myapp.screens.api.OrderDetailResponse
import com.example.myapp.screens.api.OrderResponse
import com.example.myapp.screens.api.RetrofitClient
import com.example.myapp.screens.api.UserProfileSummary
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

/**
 * Màn hình theo dõi đơn hàng.
 *
 * Hiển thị tất cả đơn hàng của người dùng, phân thành 2 phần:
 * - "Chưa giao" (đỏ): trạng thái pending, paid, confirmed, delivering
 * - "Đã giao" (xanh): trạng thái completed, delivered, done
 *
 * Hỗ trợ phân trang (5 đơn mỗi trang, tự động tải thêm khi cuộn đến cuối).
 * Hiển thị tổng quan điểm tích lũy ở đầu trang.
 *
 * Luồng hoạt động:
 * 1. Lấy userId từ SharedPreferences
 * 2. Gọi API lấy tất cả đơn hàng
 * 3. Tải chi tiết từng đơn (tên món, số lượng)
 * 4. Phân loại pending/completed
 * 5. Hiển thị 5 đơn đầu tiên mỗi phần
 * 6. Cuộn đến cuối → tải thêm 5 đơn tiếp
 *
 * Nhấn vào đơn → chuyển đến [OrderTrackingDetail].
 *
 * @see OrderTrackingDetail
 * @see PointsDetail
 */
class OrderTracking : AppCompatActivity() {
    private var allPendingOrders = listOf<OrderDetailResponse>()
    private var allCompletedOrders = listOf<OrderDetailResponse>()
    private var displayedPendingCount = 0
    private var displayedCompletedCount = 0
    private val itemsPerPage = 5
    
    private lateinit var ordersContainer: android.widget.LinearLayout
    private lateinit var btnLoadMore: android.widget.Button
    private lateinit var scrollViewOrders: android.widget.ScrollView
    private var isAutoLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_tracking)

        ordersContainer = findViewById(R.id.ordersContainer)
        btnLoadMore = findViewById(R.id.btnLoadMore)
        scrollViewOrders = findViewById(R.id.scrollViewOrders)

        loadPointsSummary()
        loadAllOrders()

        // Auto-load more orders when scrolling to the bottom
        scrollViewOrders.setOnScrollChangeListener { v, _, scrollY, _, _ ->
            val scrollView = v as android.widget.ScrollView
            val child = scrollView.getChildAt(0)
            if (child != null) {
                // Check if we reached the bottom (with 200px buffer)
                if (scrollY + scrollView.height >= child.height - 200) {
                    if (!isAutoLoading) {
                        isAutoLoading = true
                        loadMoreOrders()
                        // Reset flag after a delay to prevent multiple triggers
                        scrollView.postDelayed({ isAutoLoading = false }, 1000)
                    }
                }
            }
        }

        // Back Button - Return to previous screen
        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        // Bottom Navigation - Home
        val icHome: ImageView = findViewById(R.id.icHome)
        icHome.setOnClickListener {
            startActivity(Intent(this, home::class.java))
        }

        // Bottom Navigation - Wishlist
        val icHeart: ImageView = findViewById(R.id.icHeart)
        icHeart.setOnClickListener {
            android.util.Log.d("OrderTracking", "Wishlist clicked")
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

        // Load More Button
        btnLoadMore.setOnClickListener {
            loadMoreOrders()
        }
    }

    private fun loadPointsSummary() {
        val userId = resolveUserIdForTracking()
        val pointsView = findViewById<TextView>(R.id.tvPointsSummary)
        RetrofitClient.apiService.getProfileSummary(userId).enqueue(object : Callback<UserProfileSummary> {
            override fun onResponse(call: Call<UserProfileSummary>, response: Response<UserProfileSummary>) {
                if (response.isSuccessful) {
                    pointsView.text = "Điểm tích lũy: ${response.body()?.points ?: 0}"
                }
            }

            override fun onFailure(call: Call<UserProfileSummary>, t: Throwable) {
                pointsView.text = "Điểm tích lũy: 0"
            }
        })
    }

    private fun loadAllOrders() {
        val userId = resolveUserIdForTracking()
        RetrofitClient.apiService.getUserOrders(userId).enqueue(object : Callback<List<OrderResponse>> {
            override fun onResponse(call: Call<List<OrderResponse>>, response: Response<List<OrderResponse>>) {
                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    Toast.makeText(this@OrderTracking, "Không có đơn hàng", Toast.LENGTH_SHORT).show()
                    btnLoadMore.visibility = android.view.View.GONE
                    return
                }

                val allOrders = response.body()!!.sortedByDescending { it.createdat }
                
                // Tách riêng pending và completed
                val pendingOrderIds = allOrders.filter { !isCompletedStatus(it.status) }.map { it.id }
                val completedOrderIds = allOrders.filter { isCompletedStatus(it.status) }.map { it.id }

                // Load chi tiết từng order
                loadOrdersDetail(pendingOrderIds, completedOrderIds)
            }

            override fun onFailure(call: Call<List<OrderResponse>>, t: Throwable) {
                Toast.makeText(this@OrderTracking, "Không tải được đơn hàng", Toast.LENGTH_SHORT).show()
                btnLoadMore.visibility = android.view.View.GONE
            }
        })
    }

    private fun loadOrdersDetail(pendingOrderIds: List<Int>, completedOrderIds: List<Int>) {
        val pending = mutableListOf<OrderDetailResponse>()
        val completed = mutableListOf<OrderDetailResponse>()
        var completed_count = 0
        val total_count = pendingOrderIds.size + completedOrderIds.size

        // Load pending orders
        for (orderId in pendingOrderIds) {
            RetrofitClient.apiService.getOrderDetail(orderId).enqueue(object : Callback<OrderDetailResponse> {
                override fun onResponse(call: Call<OrderDetailResponse>, response: Response<OrderDetailResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        pending.add(response.body()!!)
                    }
                    completed_count++
                    if (completed_count == total_count) {
                        bindAllOrders(pending, completed)
                    }
                }

                override fun onFailure(call: Call<OrderDetailResponse>, t: Throwable) {
                    completed_count++
                    if (completed_count == total_count) {
                        bindAllOrders(pending, completed)
                    }
                }
            })
        }

        // Load completed orders
        for (orderId in completedOrderIds) {
            RetrofitClient.apiService.getOrderDetail(orderId).enqueue(object : Callback<OrderDetailResponse> {
                override fun onResponse(call: Call<OrderDetailResponse>, response: Response<OrderDetailResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        completed.add(response.body()!!)
                    }
                    completed_count++
                    if (completed_count == total_count) {
                        bindAllOrders(pending, completed)
                    }
                }

                override fun onFailure(call: Call<OrderDetailResponse>, t: Throwable) {
                    completed_count++
                    if (completed_count == total_count) {
                        bindAllOrders(pending, completed)
                    }
                }
            })
        }

        // Handle empty case
        if (total_count == 0) {
            bindAllOrders(emptyList(), emptyList())
        }
    }

    private fun bindAllOrders(pending: List<OrderDetailResponse>, completed: List<OrderDetailResponse>) {
        allPendingOrders = pending
        allCompletedOrders = completed
        displayedPendingCount = 0
        displayedCompletedCount = 0
        
        ordersContainer.removeAllViews()

        // Display initial orders
        displayNextOrders()
    }

    private fun displayNextOrders() {
        val inflater = LayoutInflater.from(this)

        // Pending orders section
        val pendingToDisplay = allPendingOrders.drop(displayedPendingCount).take(itemsPerPage)
        if (pendingToDisplay.isNotEmpty()) {
            if (displayedPendingCount == 0) {
                val labelView = android.widget.TextView(this).apply {
                    text = "Chưa giao"
                    setTextColor(android.graphics.Color.parseColor("#d32f2f"))
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setPadding(10, 20, 0, 16)
                }
                ordersContainer.addView(labelView)
            }
            pendingToDisplay.forEach { order ->
                addOrderCard(ordersContainer, order, order.status, inflater)
            }
            displayedPendingCount += pendingToDisplay.size
        }

        // Completed orders section
        val completedToDisplay = allCompletedOrders.drop(displayedCompletedCount).take(itemsPerPage)
        if (completedToDisplay.isNotEmpty()) {
            if (displayedCompletedCount == 0) {
                val labelView = android.widget.TextView(this).apply {
                    text = "Đã giao"
                    setTextColor(android.graphics.Color.parseColor("#34a853"))
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setPadding(10, 40, 0, 16)
                }
                ordersContainer.addView(labelView)
            }
            completedToDisplay.forEach { order ->
                addOrderCard(ordersContainer, order, order.status, inflater)
            }
            displayedCompletedCount += completedToDisplay.size
        }

        // Update Load More button visibility
        val hasMore = displayedPendingCount < allPendingOrders.size || displayedCompletedCount < allCompletedOrders.size
        btnLoadMore.visibility = if (hasMore) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun loadMoreOrders() {
        displayNextOrders()
    }

    private fun addOrderCard(container: android.widget.LinearLayout, order: OrderDetailResponse, status: String, inflater: LayoutInflater) {
        val isCompleted = isCompletedStatus(status)
        val statusColor = if (isCompleted) android.graphics.Color.parseColor("#34a853") else android.graphics.Color.parseColor("#d32f2f")
        val statusText = if (isCompleted) "Đã giao ✓" else "Chưa giao"
        
        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        val cardView = androidx.cardview.widget.CardView(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            radius = 30f
            cardElevation = 4f
        }

        val mainLayout = android.widget.LinearLayout(this).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.WHITE)
            setPadding(20, 20, 20, 20)
        }

        // Details section
        val detailsLayout = android.widget.LinearLayout(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
            orientation = android.widget.LinearLayout.VERTICAL
        }

        // Dish names
        val totalQty = order.order_items.sumOf { it.quantity }
        val itemSummary = if (order.order_items.isEmpty()) {
            "Đơn hàng #${order.id}"
        } else {
            order.order_items.joinToString("\n") { item ->
                "• ${item.menuitem_name ?: "Món #${item.menuitemid}"} x${item.quantity}"
            }
        }

        val dishNameView = android.widget.TextView(this).apply {
            text = itemSummary
            setTextColor(android.graphics.Color.BLACK)
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }
        detailsLayout.addView(dishNameView)

        // Quantity
        val qtyView = android.widget.TextView(this).apply {
            text = "SL: $totalQty"
            setTextColor(android.graphics.Color.BLACK)
            textSize = 14f
            setPadding(0, 0, 0, 8)
        }
        detailsLayout.addView(qtyView)

        // Restaurant
        val restaurantView = android.widget.TextView(this).apply {
            text = order.restaurant_name ?: "Nhà hàng #${order.restaurantid}"
            setTextColor(android.graphics.Color.BLACK)
            textSize = 14f
        }
        detailsLayout.addView(restaurantView)

        mainLayout.addView(detailsLayout)

        // Status and Price Row
        val statusRow = android.widget.LinearLayout(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val statusView = android.widget.TextView(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            text = statusText
            setTextColor(statusColor)
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        statusRow.addView(statusView)

        val priceView = android.widget.TextView(this).apply {
            text = "${fmt.format(order.totalprice)}đ"
            setTextColor(android.graphics.Color.BLACK)
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        statusRow.addView(priceView)

        mainLayout.addView(statusRow)

        // Detail button
        val detailView = android.widget.TextView(this).apply {
            text = "Chi tiết"
            setTextColor(android.graphics.Color.BLACK)
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                openOrderDetail(order.id, status, order.restaurant_name)
            }
        }
        mainLayout.addView(detailView)

        cardView.addView(mainLayout)
        
        // Also add click listener to card
        cardView.setOnClickListener {
            openOrderDetail(order.id, status, order.restaurant_name)
        }

        container.addView(cardView)
    }

    private fun resolveUserIdForTracking(): Int {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        if (userId > 0) return userId

        val userName = sharedPref.getString("user_name", "") ?: ""
        if (userName.contains("Trung", ignoreCase = true) || userName.isBlank()) {
            return 1
        }
        return 1
    }

    private fun isCompletedStatus(status: String?): Boolean {
        val normalized = (status ?: "").lowercase(Locale.ROOT)
        return normalized == "completed" || normalized == "delivered" || normalized == "done"
    }

    private fun openOrderDetail(orderId: Int?, status: String, restaurantName: String?) {
        if (orderId == null) {
            Toast.makeText(this, "Không có đơn để xem", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, OrderTrackingDetail::class.java)
        intent.putExtra("order_id", orderId)
        intent.putExtra("order_status", status)
        // Nếu không có tên quán, lấy tên tạm từ UI title (nếu có) hoặc dùng Mã đơn
        val titleText = restaurantName ?: "Đơn hàng #$orderId"
        intent.putExtra("order_name", titleText)
        startActivity(intent)
    }
}
