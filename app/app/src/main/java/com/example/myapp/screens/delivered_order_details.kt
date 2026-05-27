package com.example.myapp.screens


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

/**
 * Màn hình chi tiết đơn hàng đã giao.
 *
 * Hiển thị thông tin chi tiết đơn hàng đã hoàn thành:
 * tên món, số lượng, địa chỉ, phương thức thanh toán, tổng tiền.
 *
 * Có nút "Đặt lại" (reorder) → chuyển đến [order] với
 * Intent extra "reorder_order_id" để tạo đơn mới từ đơn cũ.
 *
 * @see order_history
 * @see order
 */
import com.example.myapp.screens.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class delivered_order_details : AppCompatActivity() {
    private var orderId: Int = -1
    private var loadedOrderDetail: OrderDetailResponse? = null

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

