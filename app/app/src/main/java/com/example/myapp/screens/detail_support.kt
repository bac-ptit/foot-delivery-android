package com.example.myapp.screens

/**
 * @file detail_support.kt
 * @brief Màn hình chi tiết hỗ trợ khách hàng.
 *
 * Hiển thị thông tin chi tiết về một vấn đề hỗ trợ cụ thể.
 * Cung cấp nút quay lại và nút truy cập nhanh đến chatbot AI
 * để được hỗ trợ thêm.
 */

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import com.example.myapp.R

/**
 * Activity hiển thị chi tiết một mục hỗ trợ.
 *
 * Màn hình đơn giản với nút quay lại và nút chuyển đến chatbot AI.
 * Layout sử dụng resource `detailsupport`.
 */
class detail_support : AppCompatActivity() {
    /**
     * Khởi tạo Activity, thiết lập giao diện và các sự kiện click.
     *
     * Thiết lập nút quay lại (đóng Activity) và nút chuyển đến chatbot.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detailsupport)

        //        click btnBack
        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish() // quay lại màn hình trước (start)
        }

        //        click btn_chatbot
        val btnchatbot: View = findViewById(R.id.btnChatbot)
        btnchatbot.setOnClickListener {
            val intent = Intent(this, chatbot::class.java)
            startActivity(intent)
        }
    }
}