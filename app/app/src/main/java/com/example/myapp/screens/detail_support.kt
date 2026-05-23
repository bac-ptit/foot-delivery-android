package com.example.myapp.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import com.example.myapp.R

class detail_support : AppCompatActivity() {
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