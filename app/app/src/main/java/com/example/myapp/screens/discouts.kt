package com.example.myapp.screens

/**
 * @file discouts.kt
 * @brief Màn hình mã giảm giá (voucher).
 *
 * Hiển thị danh sách mã giảm giá khả dụng từ API backend.
 * Cho phép người chọn một mã giảm giá và trả kết quả về
 * Activity gọi thông qua setResult(). Hiển thị mã, loại giảm giá
 * (phần trăm hoặc số tiền cố định) và giá trị đơn hàng tối thiểu.
 */

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

/**
 * Activity hiển thị danh sách mã giảm giá.
 *
 * Tải danh sách khuyến mãi từ API, ưu tiên mã có trạng thái "active".
 * Khi người dùng chọn mã, trả kết quả về Activity gọi với:
 * - `promotion_id`: ID khuyến mãi
 * - `promotion_code`: Mã giảm giá
 * - `discount_type`: Loại giảm giá (percent/fixed)
 * - `discount_value`: Giá trị giảm giá
 *
 * @property selectedPromotion Khuyến mãi đang được chọn
 */
class discouts : AppCompatActivity() {
    private var selectedPromotion: PromotionResponse? = null

    /**
     * Khởi tạo Activity, thiết lập giao diện và tải danh sách khuyến mãi.
     *
     * Thiết lập các nút điều hướng (quay lại, home, profile, giỏ hàng),
     * hiển thị trạng thái "Đang tải voucher..." và gọi API loadPromotions.
     * Thiết lập sự kiện chọn mã giảm giá để trả kết quả về Activity gọi.
     */
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

    /**
     * Tải danh sách khuyến mãi từ API và hiển thị lên giao diện.
     *
     * Gọi API getPromotions, chọn khuyến mãi active đầu tiên (hoặc phần tử đầu).
     * Hiển thị mã giảm giá, loại giảm (phần trăm/số tiền) và giá trị đơn tối thiểu.
     * Vô hiệu hóa nút chọn nếu không có khuyến mãi khả dụng.
     *
     * @param tvVoucherDesc1 TextView hiển thị mô tả voucher
     * @param tvSelect1 TextView/nút chọn voucher
     */
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