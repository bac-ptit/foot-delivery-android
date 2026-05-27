package com.example.myapp.screens


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

/**
 * Màn hình chọn phương thức thanh toán.
 *
 * Hỗ trợ 2 chế độ:
 * - Chọn (select): Trả phương thức đã chọn về [order]
 * - Thử (action): Thực hiện thanh toán ngay
 *
 * COD: Cập nhật trạng thái đơn → "confirmed" → [payment_successful]
 * VNPay: Tạo URL thanh toán → mở [VNPayActivity]
 *
 * @see order
 * @see VNPayActivity
 * @see payment_successful
 */
import retrofit2.Callback
import retrofit2.Response


class payment_methods : AppCompatActivity() {

    private var isRequestingVnPay: Boolean = false


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

