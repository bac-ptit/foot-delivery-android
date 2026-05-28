package com.example.myapp.screens

/** @file shipping_address_added_successfully.kt
 * @brief Màn hình thông báo thêm địa chỉ giao hàng thành công.
 *
 * Hiển thị thông báo thành công và cho phép người dùng quay về trang chủ.
 */

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Import Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.example.myapp.screens.home

/**
 * Màn hình thông báo thêm địa chỉ giao hàng thành công.
 *
 * Hiển thị thông báo thành công sau khi người dùng thêm địa chỉ mới.
 * Cung cấp nút "Trở về trang chủ" xóa ngăn xếp Activity và quay về Home.
 * Thanh điều hướng dưới cùng cho phép chuyển đến Giỏ hàng, Trang chủ và Cá nhân.
 */
class shipping_address_added_successfully : AppCompatActivity() {
    /**
     * Khởi tạo màn hình thông báo thêm địa chỉ thành công.
     *
     * Thiết lập nút "Trở về trang chủ" với FLAG_ACTIVITY_CLEAR_TASK
     * để xóa ngăn xếp Activity, cùng thanh điều hướng dưới cùng.
     *
     * @param savedInstanceState Trạng thái đã lưu trước đó (nếu có).
     */
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