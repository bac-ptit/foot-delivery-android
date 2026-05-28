package com.example.myapp.screens

/**
 * @file VNPayActivity.kt
 * @brief Activity xử lý thanh toán qua cổng VNPay.
 *
 * Hiển thị trang thanh toán VNPay trong WebView. Theo dõi URL callback
 * `vnpay_return` để xác nhận kết quả thanh toán. Nếu thanh toán thành công
 * (vnp_ResponseCode=00), cập nhật trạng thái đơn hàng thành "paid" và
 * chuyển đến màn hình thanh toán thành công.
 */

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.myapp.screens.api.OrderResponse
import com.example.myapp.screens.api.OrderStatusUpdateRequest
import com.example.myapp.screens.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * Activity xử lý thanh toán VNPay qua WebView.
 *
 * Nhận `URL` (link thanh toán VNPay) và `order_id` từ Intent.
 * Tải trang thanh toán VNPay trong WebView và theo dõi URL callback.
 * Khi phát hiện `vnpay_return` với ResponseCode=00, cập nhật trạng thái
 * đơn hàng thành "paid" rồi chuyển đến payment_successful.
 *
 * @property isHandlingReturn Cờ ngăn chặn xử lý callback VNPay nhiều lần
 */
class VNPayActivity : AppCompatActivity() {
    private var isHandlingReturn = false

    /**
     * Khởi tạo Activity, thiết lập WebView và bắt đầu tải trang thanh toán VNPay.
     *
     * Cấu hình WebView với JavaScript enabled, thiết lập WebViewClient
     * để theo dõi URL callback `vnpay_return`. Xử lý kết quả thanh toán:
     * - Thành công (ResponseCode=00): cập nhật trạng thái đơn → chuyển màn thành công
     * - Thất bại: đóng Activity
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        setContentView(webView)


        val paymentUrl = intent.getStringExtra("URL") ?: ""
        val orderId = intent.getIntExtra("order_id", -1)
        webView.settings.javaScriptEnabled = true


        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // Nếu thấy link vnpay_return từ server trả về thì đóng webview
                if (url != null && url.contains("vnpay_return") && !isHandlingReturn) {
                    isHandlingReturn = true
                    if (url.contains("vnp_ResponseCode=00")) {
                        if (orderId > 0) {
                            RetrofitClient.apiService.updateOrderStatus(orderId, OrderStatusUpdateRequest("paid"))
                                .enqueue(object : Callback<OrderResponse> {
                                    override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                                        if (!response.isSuccessful) {
                                            Toast.makeText(this@VNPayActivity, "Không cập nhật được trạng thái đơn", Toast.LENGTH_SHORT).show()
                                            finish()
                                            return
                                        }
                                        val successIntent = Intent(this@VNPayActivity, payment_successful::class.java)
                                        successIntent.putExtra("order_id", orderId)
                                        startActivity(successIntent)
                                        finish()
                                    }

                                    override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                                        Toast.makeText(this@VNPayActivity, "Lỗi mạng khi cập nhật đơn", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                })
                        } else {
                            startActivity(Intent(this@VNPayActivity, payment_successful::class.java))
                            finish()
                        }
                    } else {
                        finish()
                    }
                    return true
                }
                return false
            }
        }
        webView.loadUrl(paymentUrl)
    }
}



