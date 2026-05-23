package com.example.myapp.screens


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


class VNPayActivity : AppCompatActivity() {
    private var isHandlingReturn = false

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



