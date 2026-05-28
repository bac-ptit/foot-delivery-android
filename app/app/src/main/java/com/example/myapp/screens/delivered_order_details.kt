package com.example.myapp.screens

/**
 * @file delivered_order_details.kt
 * @brief Màn hình chi tiết đơn hàng đã giao thành công.
 *
 * Cho phép người dùng xem thông tin chi tiết của một đơn hàng đã hoàn thành,
 * bao gồm danh sách món ăn, tổng số lượng, địa chỉ giao hàng và thành tiền.
 * Người dùng có thể đặt lại đơn hàng cũ từ màn hình này.
 */

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.example.myapp.screens.api.OrderDetailResponse
import com.example.myapp.screens.api.OrderCreateRequest
import com.example.myapp.screens.api.OrderItemRequest
import com.example.myapp.screens.api.OrderResponse
import com.example.myapp.screens.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * Activity hiển thị chi tiết đơn hàng đã giao.
 *
 * Nhận `order_id` từ Intent, tải chi tiết đơn hàng từ API và hiển thị
 * danh sách món ăn, tổng tiền, địa chỉ giao hàng. Hỗ trợ chức năng
 * "Đặt lại" để chuyển sang màn hình đặt hàng với các món từ đơn cũ.
 *
 * @property orderId ID của đơn hàng cần hiển thị, mặc định -1 nếu không hợp lệ
 * @property loadedOrderDetail Chi tiết đơn hàng đã tải từ API, dùng cho chức năng đặt lại
 */
class delivered_order_details : AppCompatActivity() {
    private var orderId: Int = -1
    private var loadedOrderDetail: OrderDetailResponse? = null

    /**
     * Khởi tạo Activity, thiết lập giao diện và các sự kiện click.
     *
     * Tải chi tiết đơn hàng từ API nếu order_id hợp lệ.
     * Thiết lập các nút điều hướng: quay lại, đặt lại, profile, home, giỏ hàng.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.delivered_order_details)

        orderId = intent.getIntExtra("order_id", -1)
        if (orderId > 0) {
            loadOrderDetail(orderId)
        }


        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish() // quay lại màn hình trước
        }
        // Tìm nút "Đặt lại" theo ID trong XML
        val btnReorder: Button = findViewById(R.id.btnReorder)


        // Thiết lập sự kiện click
        btnReorder.setOnClickListener {
            reorderCurrentOrder()
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
     * Tải chi tiết đơn hàng từ API theo ID.
     *
     * Gọi API getOrderDetail và hiển thị dữ liệu lên giao diện.
     * Hiển thị Toast lỗi nếu tải thất bại.
     *
     * @param orderId ID của đơn hàng cần tải
     */
    private fun loadOrderDetail(orderId: Int) {
        RetrofitClient.apiService.getOrderDetail(orderId).enqueue(object : Callback<OrderDetailResponse> {
            override fun onResponse(call: Call<OrderDetailResponse>, response: Response<OrderDetailResponse>) {
                if (!response.isSuccessful || response.body() == null) {
                    Toast.makeText(this@delivered_order_details, "Không tải được chi tiết đơn", Toast.LENGTH_SHORT).show()
                    return
                }
                bindOrder(response.body()!!)
            }

            override fun onFailure(call: Call<OrderDetailResponse>, t: Throwable) {
                Toast.makeText(this@delivered_order_details, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Liên kết dữ liệu đơn hàng lên giao diện.
     *
     * Hiển thị tiêu đề với mã đơn hàng và ngày tạo, danh sách món ăn,
     * tổng số lượng, địa chỉ giao hàng và thành tiền.
     *
     * @param order Chi tiết đơn hàng từ API
     */
    private fun bindOrder(order: OrderDetailResponse) {
        loadedOrderDetail = order
        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        val title = findViewById<TextView>(R.id.tvTitle)
        val content = findViewById<TextView>(R.id.tvOrderContent)

        val totalQuantity = order.order_items.sumOf { it.quantity }
        val itemSummary = if (order.order_items.isEmpty()) {
            "(Không có chi tiết món)"
        } else {
            order.order_items.joinToString("\n") {
                val itemName = it.menuitem_name ?: "Món #${it.menuitemid}"
                if (it.image_url.isNullOrBlank()) {
                    "- $itemName x${it.quantity}"
                } else {
                    "- $itemName x${it.quantity}"
                }
            }
        }

        title.text = "Chi tiết đơn hàng #${order.id} ngày ${formatDate(order.createdat)}"
        content.text = "${itemSummary}\nTổng số món: $totalQuantity\nĐịa chỉ: ${order.address_detail ?: "N/A"}\nPhương thức thanh toán: Thanh toán online/cash\nVoucher: Không có\nThành tiền: ${fmt.format(order.totalprice)}đ"
    }

    /**
     * Xử lý chức năng đặt lại đơn hàng.
     *
     * Chuyển sang màn hình đặt hàng (order) với `reorder_order_id` để
     * tự động tải lại các món ăn từ đơn hàng cũ. Hiển thị Toast nếu
     * đơn hàng chưa được tải xong.
     */
    private fun reorderCurrentOrder() {
        val sourceOrder = loadedOrderDetail
        if (sourceOrder == null) {
            Toast.makeText(this, "Đơn hàng chưa tải xong", Toast.LENGTH_SHORT).show()
            return
        }

        // Thay vì gọi API tạo đơn ngay, ta chuyển sang màn hình Thanh toán (Order)
        // Màn hình Order đã có sẵn logic nhận "reorder_order_id" để tự tải lại các món cũ
        val intent = Intent(this, order::class.java)
        intent.putExtra("reorder_order_id", sourceOrder.id)
        startActivity(intent)
    }

    /**
     * Định dạng chuỗi ngày từ ISO 8601 sang định dạng dd/MM/yyyy.
     *
     * @param raw Chuỗi ngày ở định dạng "yyyy-MM-dd'T'HH:mm:ss"
     * @return Chuỗi ngày đã định dạng hoặc 10 ký tự đầu nếu parse thất bại
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

