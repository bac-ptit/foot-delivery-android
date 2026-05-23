package com.example.myapp.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.adapters.AddressAdapter
import com.example.myapp.screens.api.Address
import com.example.myapp.screens.api.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class savedeliveryaddress : AppCompatActivity() {
    private lateinit var rvAddresses: RecyclerView
    private lateinit var addressAdapter: AddressAdapter
    private val addressList = mutableListOf<Address>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.savedeliveryaddress)

        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish() // quay lại màn hình trước
        }
        // 1. Click icon home để về trang chủ
        val ic_Home: ImageView = findViewById(R.id.icHome)
        ic_Home.setOnClickListener {
            val intent = Intent(this, home::class.java)
            startActivity(intent)
        }

        val btnCart: ImageView = findViewById(R.id.icCart)
        btnCart.setOnClickListener {
            val intent = Intent(this, cart::class.java)
            startActivity(intent)
        }
        // 2. Click vào nút "Thêm địa chỉ giao hàng" (Button) sang màn hình thêm
        val btnAddAddress: Button = findViewById(R.id.btnAddAddress)
        btnAddAddress.setOnClickListener {
            val intent = Intent(this, add_shipping_address::class.java)
            startActivity(intent)
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

        // Setup RecyclerView
        rvAddresses = findViewById(R.id.rvAddresses)
        rvAddresses.layoutManager = LinearLayoutManager(this)
        addressAdapter = AddressAdapter(addressList) { address ->
            // Handle edit click
            val intent = Intent(this, edit_shipping_address::class.java)
            intent.putExtra("address_id", address.id)
            startActivity(intent)
        }
        rvAddresses.adapter = addressAdapter

        // Fetch addresses from API
        fetchUserAddresses()
    }

    private fun fetchUserAddresses() {
        // Lấy userId từ SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 1)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // Android emulator localhost
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.getUserAddresses(userId).enqueue(object : Callback<List<Address>> {
            override fun onResponse(call: Call<List<Address>>, response: Response<List<Address>>) {
                if (response.isSuccessful) {
                    val addresses = response.body() ?: emptyList()
                    addressList.clear()
                    addressList.addAll(addresses)
                    addressAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@savedeliveryaddress, "Không thể tải danh sách địa chỉ", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Address>>, t: Throwable) {
                Toast.makeText(this@savedeliveryaddress, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
