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
import com.example.myapp.screens.api.AddressUpdateRequest
import com.example.myapp.screens.api.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class edit_shipping_address : AppCompatActivity() {
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var btnEdit: Button
    private var addressId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_shipping_address)

        // Initialize views
        edtPhone = findViewById(R.id.edtPhone)
        edtAddress = findViewById(R.id.edtAddress)
        btnEdit = findViewById(R.id.btnAdd)

        // Get address_id from intent
        addressId = intent.getIntExtra("address_id", -1)
        if (addressId == -1) {
            Toast.makeText(this, "Không tìm thấy địa chỉ", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load current address data
        loadAddressData()

        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish() // quay lại màn hình trước
        }

        val btnCart: ImageView = findViewById(R.id.icCart)
        btnCart.setOnClickListener {
            val intent = Intent(this, cart::class.java)
            startActivity(intent)
        }
        // Set up click listener for edit button
        btnEdit.setOnClickListener {
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

            // Call API to update address
            updateAddress(phone, address)
        }

        // click icProfile
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
    }

    private fun loadAddressData() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // Android emulator localhost
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        // Get userId from SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)

        apiService.getUserAddresses(userId).enqueue(object : Callback<List<Address>> {
            override fun onResponse(call: Call<List<Address>>, response: Response<List<Address>>) {
                if (response.isSuccessful) {
                    val addresses = response.body() ?: emptyList()
                    val address = addresses.find { it.id == addressId }
                    if (address != null) {
                        edtPhone.setText(address.phone ?: "")
                        edtAddress.setText(address.detail)
                    } else {
                        Toast.makeText(this@edit_shipping_address, "Không tìm thấy địa chỉ", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@edit_shipping_address, "Không thể tải thông tin địa chỉ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Address>>, t: Throwable) {
                Toast.makeText(this@edit_shipping_address, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateAddress(phone: String, address: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // Android emulator localhost
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val addressUpdateRequest = AddressUpdateRequest(
            detail = address,
            phone = phone
        )

        apiService.updateAddress(addressId, addressUpdateRequest).enqueue(object : Callback<Address> {
            override fun onResponse(call: Call<Address>, response: Response<Address>) {
                if (response.isSuccessful) {
                    // Thành công - chuyển sang màn hình thông báo
                    Toast.makeText(this@edit_shipping_address, "Cập nhật địa chỉ thành công!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@edit_shipping_address, shipping_address_fixed_successfully::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@edit_shipping_address, "Không thể cập nhật địa chỉ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Address>, t: Throwable) {
                Toast.makeText(this@edit_shipping_address, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
