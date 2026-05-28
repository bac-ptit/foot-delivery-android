package com.example.myapp.screens

/**
 * @file order_history.kt
 * @brief Màn hình lịch sử đơn hàng của ứng dụng giao đồ ăn.
 *
 * Hiển thị 2 đơn hàng gần nhất đã hoàn thành (completed) của người dùng.
 * Cho phép nhấn vào "Xem chi tiết" để mở trang chi tiết đơn hàng.
 * Hỗ trợ điều hướng đến Giỏ hàng, Hồ sơ và Trang chủ.
 */

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.example.myapp.screens.api.OrderResponse
import com.example.myapp.screens.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity màn hình lịch sử đơn hàng.
 *
 * Chức năng chính:
 * - Tải danh sách đơn hàng đã hoàn thành từ API
 * - Hiển thị 2 đơn hàng gần nhất với ngày, mã đơn, trạng thái
 * - Nhấn "Xem chi tiết" để mở chi tiết đơn hàng
 * - Điều hướng đến Giỏ hàng, Hồ sơ, Trang chủ
 */
class order_history : AppCompatActivity() {
    /** ID của đơn hàng đầu tiên (gần nhất) */
    private var firstOrderId: Int? = null
    /** ID của đơn hàng thứ hai */
    private var secondOrderId: Int? = null

    /**
     * Khởi tạo màn hình lịch sử đơn hàng.
     *
     * Gọi API tải đơn hàng, thiết lập sự kiện click cho nút quay lại,
     * giỏ hàng, hồ sơ, trang chủ, và nút xem chi tiết đơn hàng.
     *
     * @param savedInstanceState Trạng thái đã lưu của Activity (nếu có)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_history)

        loadOrders()


        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish() // quay lại màn hình trước
        }

        val btnCart: ImageView = findViewById(R.id.icCart)
        btnCart.setOnClickListener {
            val intent = Intent(this, cart::class.java)
            startActivity(intent)
        }
        val tvViewDetails1: TextView = findViewById(R.id.tvViewDetails1)
        tvViewDetails1.setOnClickListener {
            openOrderDetail(firstOrderId)
        }

        val tvViewDetails2: TextView = findViewById(R.id.tvViewDetails2)
        tvViewDetails2.setOnClickListener {
            openOrderDetail(secondOrderId)
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
    }

    /**
     * Tải danh sách đơn hàng từ API.
     *
     * Lấy userId, gọi API getUserOrders, lọc các đơn có trạng thái
     * "completed", sắp xếp theo ngày giảm dần, và hiển thị 2 đơn gần nhất.
     */
    private fun loadOrders() {
        val userId = resolveUserIdForHistory()

        RetrofitClient.apiService.getUserOrders(userId).enqueue(object : Callback<List<OrderResponse>> {
            override fun onResponse(call: Call<List<OrderResponse>>, response: Response<List<OrderResponse>>) {
                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    bindTopTwoOrders(emptyList())
                    return
                }
                val sorted = response.body()!!
                    .filter { it.status == "completed" }
                    .sortedByDescending { it.createdat }
                bindTopTwoOrders(sorted)
            }

            override fun onFailure(call: Call<List<OrderResponse>>, t: Throwable) {
                Toast.makeText(this@order_history, "Không tải được lịch sử đơn", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Gán dữ liệu 2 đơn hàng gần nhất lên giao diện.
     *
     * Hiển thị ngày, mã đơn, trạng thái cho mỗi đơn.
     * Lưu firstOrderId và secondOrderId để mở chi tiết.
     * Làm mờ nút "Xem chi tiết" nếu không có đơn hàng.
     *
     * @param orders Danh sách đơn hàng đã sắp xếp
     */
    private fun bindTopTwoOrders(orders: List<OrderResponse>) {
        val tvDate1: TextView = findViewById(R.id.tvDate1)
        val tvDate2: TextView = findViewById(R.id.tvDate2)
        val tvViewDetails1: TextView = findViewById(R.id.tvViewDetails1)
        val tvViewDetails2: TextView = findViewById(R.id.tvViewDetails2)

        val first = orders.getOrNull(0)
        val second = orders.getOrNull(1)

        firstOrderId = first?.id
        secondOrderId = second?.id

        tvDate1.text = first?.let { "${formatDate(it.createdat)} - #${it.id} - ${it.status}" } ?: "Chưa có đơn hàng"
        tvDate2.text = second?.let { "${formatDate(it.createdat)} - #${it.id} - ${it.status}" } ?: "Chưa có đơn hàng"

        tvViewDetails1.alpha = if (first != null) 1f else 0.4f
        tvViewDetails2.alpha = if (second != null) 1f else 0.4f
    }

    /**
     * Mở màn hình chi tiết đơn hàng.
     *
     * @param orderId ID đơn hàng cần xem chi tiết. Nếu null, hiển thị Toast lỗi.
     */
    private fun openOrderDetail(orderId: Int?) {
        if (orderId == null) {
            Toast.makeText(this, "Không có đơn để xem", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, delivered_order_details::class.java)
        intent.putExtra("order_id", orderId)
        startActivity(intent)
    }

    /**
     * Xác định userId cho việc tải lịch sử đơn hàng.
     *
     * Đọc user_id từ SharedPreferences. Nếu không hợp lệ,
     * trả về 1 làm giá trị mặc định.
     *
     * @return ID người dùng
     */
    private fun resolveUserIdForHistory(): Int {
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
     * Định dạng chuỗi ngày từ ISO 8601 sang định dạng dd/MM/yyyy.
     *
     * @param raw Chuỗi ngày dạng "yyyy-MM-ddTHH:mm:ss"
     * @return Chuỗi ngày đã định dạng hoặc 10 ký tự đầu nếu lỗi
     */
    private fun formatDate(raw: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val parsed: Date = parser.parse(raw) ?: return raw
            SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN")).format(parsed)
        } catch (e: Exception) {
            raw.take(10)
        }
    }
}
