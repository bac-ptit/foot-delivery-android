package com.example.myapp.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Import Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.example.myapp.screens.home

class shipping_address_added_successfully : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shipping_address_added_successfully)

        // Tìm nút "Trở về trang chủ" theo ID btnReturnHome
        val btnReturnHome: Button = findViewById(R.id.btnReturnHome)

        // Thiết lập sự kiện click
        btnReturnHome.setOnClickListener {
            // Quay về trang chủ (Home)
            val intent = Intent(this, home::class.java)
            // Xóa các trang trước đó để không bị quay lại trang thành công này khi nhấn Back
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