package com.example.myapp.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.screens.adapters.FAQAdapter
import com.example.myapp.screens.api.RetrofitClient
import com.example.myapp.screens.api.FAQ
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class customer_support : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FAQAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.customer_support)

        //        click btnBack
        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish() // quay lại màn hình trước (start)
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.faqRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load FAQs from API
        loadFAQs()

//        click btn_chatbot
        val btnchatbot: View = findViewById(R.id.btnChatbot)
        btnchatbot.setOnClickListener {
            val intent = Intent(this, chatbot::class.java)
            startActivity(intent)
        }
    }

    private fun loadFAQs() {
        RetrofitClient.apiService.getFAQs().enqueue(object : Callback<List<FAQ>> {
            override fun onResponse(call: Call<List<FAQ>>, response: Response<List<FAQ>>) {
                if (response.isSuccessful) {
                    val faqs = response.body() ?: emptyList()
                    adapter = FAQAdapter(faqs)
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@customer_support, 
                        "Lỗi tải FAQ: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<FAQ>>, t: Throwable) {
                Toast.makeText(this@customer_support, 
                    "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}