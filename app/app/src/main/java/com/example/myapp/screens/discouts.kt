package com.example.myapp.screens

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.example.myapp.screens.api.PromotionResponse
import com.example.myapp.screens.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class discouts : AppCompatActivity() {
    private var selectedPromotion: PromotionResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.discouts)

        val icBack = findViewById<ImageView>(R.id.icBack)
        val icHome = findViewById<ImageView>(R.id.icHome)
        val icProfile = findViewById<ImageView>(R.id.icProfile)
        val icCart = findViewById<ImageView>(R.id.icCart)
        val tvVoucherDesc1 = findViewById<TextView>(R.id.tvVoucherDesc1)
        val tvSelect1 = findViewById<TextView>(R.id.tvSelect1)

        icBack.setOnClickListener { finish() }
        icHome.setOnClickListener { startActivity(Intent(this, home::class.java)) }
        icProfile.setOnClickListener { startActivity(Intent(this, profile::class.java)) }
        icCart.setOnClickListener { startActivity(Intent(this, cart::class.java)) }

        tvVoucherDesc1.text = "Đang tải voucher..."
        tvSelect1.isEnabled = false
        loadPromotions(tvVoucherDesc1, tvSelect1)

        tvSelect1.setOnClickListener {
            val promotion = selectedPromotion ?: return@setOnClickListener
            val resultIntent = Intent()
            resultIntent.putExtra("promotion_id", promotion.id)
            resultIntent.putExtra("promotion_code", promotion.code)
            resultIntent.putExtra("discount_type", promotion.discounttype)
            resultIntent.putExtra("discount_value", promotion.discountvalue)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun loadPromotions(tvVoucherDesc1: TextView, tvSelect1: TextView) {
        RetrofitClient.apiService.getPromotions().enqueue(object : Callback<List<PromotionResponse>> {
            override fun onResponse(call: Call<List<PromotionResponse>>, response: Response<List<PromotionResponse>>) {
                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    tvVoucherDesc1.text = "Hiện chưa có voucher khả dụng"
                    tvSelect1.isEnabled = false
                    return
                }

                val promotions = response.body()!!
                selectedPromotion = promotions.firstOrNull { it.status.equals("active", ignoreCase = true) } ?: promotions.first()
                val promotion = selectedPromotion!!
                val discountText = if (promotion.discounttype.equals("percent", ignoreCase = true)) {
                    "${promotion.discountvalue}%"
                } else {
                    "${promotion.discountvalue}đ"
                }

                tvVoucherDesc1.text = "${promotion.code}: Giảm $discountText - ĐH tối thiểu ${promotion.minordervalue}đ"
                tvSelect1.isEnabled = true
            }

            override fun onFailure(call: Call<List<PromotionResponse>>, t: Throwable) {
                tvVoucherDesc1.text = "Không tải được voucher"
                tvSelect1.isEnabled = false
                Toast.makeText(this@discouts, "Lỗi kết nối khi tải voucher", Toast.LENGTH_SHORT).show()
            }
        })
    }
}