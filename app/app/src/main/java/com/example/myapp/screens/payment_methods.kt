package com.example.myapp.screens

/**
 * @file payment_methods.kt
 * @brief Màn hình chọn phương thức thanh toán.
 *
 * Cho phép người dùng chọn giữa thanh toán khi nhận hàng (COD)
 * và thanh toán online qua VNPay. Hỗ trợ hai chế độ: chọn phương thức
 * (select_mode) để trả kết quả về Activity gọi, hoặc thanh toán trực tiếp.
 * Tự động gọi VNPay khi auto_pay_now được bật.
 */

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.example.myapp.screens.api.OrderResponse
import com.example.myapp.screens.api.OrderStatusUpdateRequest
import com.example.myapp.screens.api.RetrofitClient
import com.example.myapp.screens.api.VNPayResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Activity hiển thị giao diện chọn phương thức thanh toán.
 *
 * Hỗ trợ hai chế độ hoạt động:
 * - **Select mode** (`select_mode = true`): trả kết quả phương thức đã chọn về Activity gọi
 * - **Direct mode**: thực hiện thanh toán ngay (COD hoặc VNPay)
 *
 * Nhận các Intent extras:
 * - `select_mode`: Boolean - bật chế độ chọn phương thức
 * - `auto_pay_now`: Boolean - tự động bắt đầu thanh toán VNPay
 * - `order_id`: Int - ID đơn hàng cần thanh toán
 * - `order_total`: Int - Tổng tiền đơn hàng (VND)
 *
 * @property isRequestingVnPay Cờ ngăn chặn gọi API VNPay nhiều lần
 */
class payment_methods : AppCompatActivity() {

    private var isRequestingVnPay: Boolean = false


    /**
     * Khởi tạo Activity, thiết lập giao diện chọn phương thức thanh toán.
     *
     * Thiết lập sự kiện click cho nút thanh toán khi nhận hàng (COD)
     * và thanh toán online (VNPay). Nếu auto_pay_now = true, tự động
     * bắt đầu thanh toán VNPay.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payment_methods)

        val isSelectMode = intent.getBooleanExtra("select_mode", false)
        val autoPayNow = intent.getBooleanExtra("auto_pay_now", false)

        val tvPayOnDelivery = findViewById<TextView>(R.id.tvPayOnDelivery)
        val tvPayNow = findViewById<TextView>(R.id.tvPayNow)
        val icBack = findViewById<ImageView>(R.id.icBack)

        tvPayOnDelivery.setOnClickListener {
            if (isSelectMode) {
                val resultIntent = Intent()
                resultIntent.putExtra("payment_method", "cod")
                resultIntent.putExtra("payment_method_label", "Thanh toán khi nhận hàng")
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                val orderId = intent.getIntExtra("order_id", -1)
                if (orderId > 0) {
                    updateOrderStatusAndGoSuccess(orderId, "confirmed")
                } else {
                    Toast.makeText(this, "Đã chọn thanh toán khi nhận hàng", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, payment_successful::class.java))
                }
            }
        }


        tvPayNow.setOnClickListener {

            if (isSelectMode) {
                val resultIntent = Intent()
                resultIntent.putExtra("payment_method", "online")
                resultIntent.putExtra("payment_method_label", "Thanh toán ngay")
                setResult(RESULT_OK, resultIntent)
                finish()
                return@setOnClickListener
            }


            startVnPayPayment()
        }

        if (!isSelectMode && autoPayNow) {
            startVnPayPayment()
        }


        // nút quay lại
        icBack.setOnClickListener {
            finish()
        }
    }

    /**
     * Bắt đầu quy trình thanh toán qua VNPay.
     *
     * Gọi API getVNPayUrl để lấy link thanh toán, sau đó mở VNPayActivity
     * với URL thanh toán. Sử dụng cờ isRequestingVnPay để tránh gọi API nhiều lần.
     * Định dạng mã đơn hàng dạng "DH{orderId}" hoặc "DH{timestamp}" nếu không có ID.
     */
    private fun startVnPayPayment() {
        if (isRequestingVnPay) return
        isRequestingVnPay = true

        val orderId = intent.getIntExtra("order_id", -1).let {
            if (it > 0) "DH$it" else "DH" + System.currentTimeMillis()
        }
        val amount = intent.getIntExtra("order_total", 70000)

        RetrofitClient.apiService.getVNPayUrl(amount, orderId)
            .enqueue(object : Callback<VNPayResponse> {
                override fun onResponse(
                    call: Call<VNPayResponse>,
                    response: Response<VNPayResponse>
                ) {
                    isRequestingVnPay = false
                    if (response.isSuccessful && response.body() != null) {
                        val paymentUrl = response.body()!!.payment_url
                        val intent = Intent(this@payment_methods, VNPayActivity::class.java)
                        intent.putExtra("URL", paymentUrl)
                        intent.putExtra("order_id", this@payment_methods.intent.getIntExtra("order_id", -1))
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@payment_methods,
                            "Lấy link thanh toán thất bại",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<VNPayResponse>, t: Throwable) {
                    isRequestingVnPay = false
                    Toast.makeText(
                        this@payment_methods,
                        "Không kết nối được server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /**
     * Cập nhật trạng thái đơn hàng và chuyển đến màn hình thanh toán thành công.
     *
     * Gọi API updateOrderStatus với trạng thái mới (ví dụ: "confirmed" cho COD).
     * Nếu thành công, mở payment_successful với order_id. Hiển thị Toast nếu thất bại.
     *
     * @param orderId ID đơn hàng cần cập nhật
     * @param status Trạng thái mới của đơn hàng
     */
    private fun updateOrderStatusAndGoSuccess(orderId: Int, status: String) {
        RetrofitClient.apiService.updateOrderStatus(orderId, OrderStatusUpdateRequest(status))
            .enqueue(object : Callback<OrderResponse> {
                override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@payment_methods, "Không cập nhật được trạng thái đơn", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val successIntent = Intent(this@payment_methods, payment_successful::class.java)
                    successIntent.putExtra("order_id", orderId)
                    startActivity(successIntent)
                    finish()
                }

                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                    Toast.makeText(this@payment_methods, "Lỗi mạng khi cập nhật đơn", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

