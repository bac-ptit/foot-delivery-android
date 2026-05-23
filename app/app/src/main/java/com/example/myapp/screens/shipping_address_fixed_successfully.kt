package com.example.myapp.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Import thêm Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.example.myapp.screens.home

class shipping_address_fixed_successfully : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shipping_address_fixed_successfully)

        // Tìm nút "Trở về trang chủ" bằng ID đã đặt trong XML
        val btnReturnHome: Button = findViewById(R.id.btnReturnHome)

        // Cài đặt sự kiện click
        btnReturnHome.setOnClickListener {
            // Tạo Intent chuyển về trang Home
            val intent = Intent(this, home::class.java)
            // Xóa các Activity trước đó để không bị quay lại trang thông báo này
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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
}