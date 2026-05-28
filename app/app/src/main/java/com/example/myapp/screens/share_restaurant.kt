package com.example.myapp.screens

/** @file share_restaurant.kt
 * @brief Màn hình chia sẻ thông tin nhà hàng.
 *
 * Hiển thị chi tiết nhà hàng và cho phép người dùng chia sẻ
 * lên các mạng xã hội (Facebook, Zalo, Twitter, Messenger).
 */

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.squareup.picasso.Picasso

/**
 * Màn hình chia sẻ thông tin nhà hàng lên mạng xã hội.
 *
 * Hiển thị tên, đánh giá, địa chỉ, giờ mở cửa và hình ảnh nhà hàng.
 * Cung cấp nút chia sẻ nhanh lên Facebook, Zalo, Twitter và Messenger
 * thông qua Intent.ACTION_SEND.
 */
class share_restaurant : AppCompatActivity() {
    /** ID của nhà hàng cần chia sẻ */
    private var restaurantId: Int = -1
    /** Tên của nhà hàng cần chia sẻ */
    private var restaurantName: String = ""
    /** Địa chỉ của nhà hàng cần chia sẻ */
    private var restaurantAddress: String = ""

    /**
     * Khởi tạo màn hình chia sẻ nhà hàng.
     *
     * Nhận thông tin nhà hàng từ Intent (id, tên, địa chỉ, đánh giá,
     * hình ảnh, giờ mở/đóng cửa), hiển thị lên giao diện và thiết lập
     * các nút chia sẻ mạng xã hội.
     *
     * @param savedInstanceState Trạng thái đã lưu trước đó (nếu có).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_restaurant)

        restaurantId = intent.getIntExtra("RESTAURANT_ID", -1)
        restaurantName = intent.getStringExtra("restaurant_name") ?: ""
        restaurantAddress = intent.getStringExtra("restaurant_address") ?: ""
        val rating = intent.getIntExtra("restaurant_rating", 0)
        val imageUrl = intent.getStringExtra("restaurant_image_url") ?: ""
        val openTime = intent.getStringExtra("restaurant_open_time") ?: ""
        val closeTime = intent.getStringExtra("restaurant_close_time") ?: ""

        val tvRestName: TextView = findViewById(R.id.tvRestName)
        val tvRating: TextView = findViewById(R.id.tvRating)
        val tvAddress: TextView = findViewById(R.id.tvAddress)
        val tvOpenTime: TextView = findViewById(R.id.tvOpenTime)
        val imgRestaurant: ImageView = findViewById(R.id.imgRestaurant)

        tvRestName.text = restaurantName
        tvRating.text = "$rating ★"
        tvAddress.text = restaurantAddress
        tvOpenTime.text = "Mở cửa: $openTime - $closeTime"

        if (imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_loading)
                .error(R.drawable.pngwing)
                .into(imgRestaurant)
        }

        findViewById<LinearLayout>(R.id.btnFacebook).setOnClickListener { shareToApp("com.facebook.katana") }
        findViewById<LinearLayout>(R.id.btnZalo).setOnClickListener { shareToApp("com.zing.zalo") }
        findViewById<LinearLayout>(R.id.btnTwitter).setOnClickListener { shareToApp("com.twitter.android") }
        findViewById<LinearLayout>(R.id.btnMessenger).setOnClickListener { shareToApp("com.facebook.orca") }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    /**
     * Chia sẻ thông tin nhà hàng đến ứng dụng được chỉ định.
     *
     * Tạo nội dung chia sẻ bao gồm tên nhà hàng, địa chỉ và link chi tiết.
     * Sử dụng Intent.ACTION_SEND với package chỉ định. Nếu ứng dụng không
     * được cài đặt, hiển thị chooser mặc định làm fallback.
     *
     * @param packageName Tên package của ứng dụng nhận chia sẻ (ví dụ: com.facebook.katana).
     */
    private fun shareToApp(packageName: String) {
        val shareLink = "https://yourapp.com/restaurant?id=$restaurantId"
        val shareText = "Khám phá ngay nhà hàng $restaurantName tại MyApp!\nĐịa chỉ: $restaurantAddress\nXem thêm tại: $shareLink"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            setPackage(packageName)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }, "Chia sẻ qua")
            startActivity(chooser)
        }
    }
}
