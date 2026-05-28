package com.example.myapp.screens

/** @file share.kt
 * @brief Màn hình chia sẻ thông tin món ăn.
 *
 * Hiển thị chi tiết món ăn và cho phép người dùng chia sẻ
 * lên các mạng xã hội (Facebook, Zalo, Twitter, Messenger).
 */

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

/**
 * Màn hình chia sẻ thông tin món ăn lên mạng xã hội.
 *
 * Hiển thị tên, mô tả, giá và hình ảnh món ăn.
 * Cung cấp nút chia sẻ nhanh lên Facebook, Zalo, Twitter và Messenger
 * thông qua Intent.ACTION_SEND.
 */
class share : AppCompatActivity() {
    /** ID của món ăn cần chia sẻ */
    private var foodId: Int = -1
    /** Tên của món ăn cần chia sẻ */
    private var foodName: String = ""
    /** Mô tả của món ăn cần chia sẻ */
    private var foodDescription: String = ""

    /**
     * Khởi tạo màn hình chia sẻ món ăn.
     *
     * Nhận thông tin món ăn từ Intent (id, tên, giá, mô tả, hình ảnh),
     * hiển thị lên giao diện và thiết lập các nút chia sẻ mạng xã hội.
     *
     * @param savedInstanceState Trạng thái đã lưu trước đó (nếu có).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        foodId = intent.getIntExtra("food_id", -1)
        foodName = intent.getStringExtra("food_name") ?: ""
        val foodPrice = intent.getIntExtra("food_price", 0)
        foodDescription = intent.getStringExtra("food_description") ?: ""
        val foodImageUrl = intent.getStringExtra("food_image_url") ?: ""

        val tvFoodName: TextView = findViewById(R.id.tvFoodName)
        val tvFoodDescription: TextView = findViewById(R.id.tvFoodDescription)
        val tvFoodPrice: TextView = findViewById(R.id.tvFoodPrice)
        val imgFood: ImageView = findViewById(R.id.imgFood)

        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))

        tvFoodName.text = foodName
        tvFoodDescription.text = foodDescription
        tvFoodPrice.text = "Giá: " + fmt.format(foodPrice) + "đ"

        if (foodImageUrl.isNotEmpty()) {
            Picasso.get()
                .load(foodImageUrl)
                .placeholder(R.drawable.placeholder_loading)
                .error(R.drawable.pngwing)
                .into(imgFood)
        }

        findViewById<LinearLayout>(R.id.btnFacebook).setOnClickListener { shareToApp("com.facebook.katana") }
        findViewById<LinearLayout>(R.id.btnZalo).setOnClickListener { shareToApp("com.zing.zalo") }
        findViewById<LinearLayout>(R.id.btnTwitter).setOnClickListener { shareToApp("com.twitter.android") }
        findViewById<LinearLayout>(R.id.btnMessenger).setOnClickListener { shareToApp("com.facebook.orca") }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    /**
     * Chia sẻ thông tin món ăn đến ứng dụng được chỉ định.
     *
     * Tạo nội dung chia sẻ bao gồm tên món, mô tả và link chi tiết.
     * Sử dụng Intent.ACTION_SEND với package chỉ định. Nếu ứng dụng không
     * được cài đặt, hiển thị chooser mặc định làm fallback.
     *
     * @param packageName Tên package của ứng dụng nhận chia sẻ (ví dụ: com.facebook.katana).
     */
    private fun shareToApp(packageName: String) {
        val shareLink = "https://yourapp.com/food?id=$foodId"
        val shareText = "Thử ngay món $foodName cực ngon tại MyApp!\n$foodDescription\nXem chi tiết tại: $shareLink"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            setPackage(packageName)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback if app not installed
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }, "Chia sẻ qua")
            startActivity(chooser)
        }
    }
}
