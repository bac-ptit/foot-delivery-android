package com.example.myapp.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.myapp.R
import com.example.myapp.screens.api.RetrofitClient
import com.example.myapp.screens.api.UserProfileSummary
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        val tvToi: TextView = findViewById(R.id.tvToi)
        val tvPoints: TextView = findViewById(R.id.tvPoints)
        val imgCoin: ImageView = findViewById(R.id.imgCoin)
        
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("user_name", "Tôi")
        val userId = sharedPref.getInt("user_id", -1)
        
        tvToi.text = userName

        // Load points from API
        if (userId > 0) {
            RetrofitClient.apiService.getProfileSummary(userId).enqueue(object : Callback<UserProfileSummary> {
                override fun onResponse(call: Call<UserProfileSummary>, response: Response<UserProfileSummary>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            tvToi.text = it.user_name
                            tvPoints.text = it.points.toString()
                        }
                    }
                }
                override fun onFailure(call: Call<UserProfileSummary>, t: Throwable) {}
            })
        }

        // Click points or coin to open order tracking
        val openTracking = View.OnClickListener {
            val intent = Intent(this, OrderTracking::class.java)
            startActivity(intent)
        }
        tvPoints.setOnClickListener(openTracking)
        imgCoin.setOnClickListener(openTracking)

        // Navigation & Other buttons
        findViewById<LinearLayout>(R.id.btnlogout).setOnClickListener {
            getSharedPreferences("user", Context.MODE_PRIVATE).edit { clear() }
            val intent = Intent(this, signin::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        findViewById<ImageView>(R.id.icHome).setOnClickListener {
            startActivity(Intent(this, home::class.java))
        }

        findViewById<ImageView>(R.id.icCart).setOnClickListener {
            startActivity(Intent(this, cart::class.java))
        }

        findViewById<View>(R.id.btnSupport).setOnClickListener {
            startActivity(Intent(this, customer_support::class.java))
        }

        findViewById<LinearLayout>(R.id.btnAddress).setOnClickListener {
            startActivity(Intent(this, savedeliveryaddress::class.java))
        }

        findViewById<LinearLayout>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, order_history::class.java))
        }
    }
}