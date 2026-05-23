package com.example.myapp.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.squareup.picasso.Picasso

class share_restaurant : AppCompatActivity() {
    private var restaurantId: Int = -1
    private var restaurantName: String = ""
    private var restaurantAddress: String = ""

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
