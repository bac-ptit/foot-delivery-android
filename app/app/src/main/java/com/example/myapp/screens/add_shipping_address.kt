package com.example.myapp.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.example.myapp.screens.api.Address
import com.example.myapp.screens.api.AddressCreateRequest
import com.example.myapp.screens.api.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class add_shipping_address : AppCompatActivity() {
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var btnAdd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_shipping_address)

        // Initialize views
        edtPhone = findViewById(R.id.edtPhone)
        edtAddress = findViewById(R.id.edtAddress)
        btnAdd = findViewById(R.id.btnAdd)

        // Click btnBack
        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish() // quay lại màn hình trước
        }

        // 2. Thiết lập sự kiện click cho nút "Thêm"
        btnAdd.setOnClickListener {
            val phone = edtPhone.text.toString().trim()
            val address = edtAddress.text.toString().trim()

            // Validate input
            if (phone.isEmpty()) {
                edtPhone.error = "Vui lòng nhập số điện thoại"
                edtPhone.requestFocus()
                return@setOnClickListener
            }

            if (address.isEmpty()) {
                edtAddress.error = "Vui lòng nhập địa chỉ"
                edtAddress.requestFocus()
                return@setOnClickListener
            }

            // Lấy userId từ SharedPreferences
            val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("user_id", 1)

            // Gọi API tạo địa chỉ
            createAddress(phone, address, userId)
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

    private fun createAddress(phone: String, address: String, userId: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // Android emulator localhost
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val addressRequest = AddressCreateRequest(
            detail = address,
            phone = phone,
            userid = userId
        )

        apiService.createAddress(addressRequest).enqueue(object : Callback<Address> {
            override fun onResponse(call: Call<Address>, response: Response<Address>) {
                if (response.isSuccessful) {
                    // Thành công - chuyển sang màn hình thông báo
                    Toast.makeText(this@add_shipping_address, "Thêm địa chỉ thành công!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@add_shipping_address, shipping_address_added_successfully::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@add_shipping_address, "Không thể thêm địa chỉ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Address>, t: Throwable) {
                Toast.makeText(this@add_shipping_address, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
