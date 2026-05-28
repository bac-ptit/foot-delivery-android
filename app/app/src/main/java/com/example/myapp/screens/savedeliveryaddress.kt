package com.example.myapp.screens

/** @file savedeliveryaddress.kt
 * @brief Màn hình danh sách địa chỉ giao hàng đã lưu.
 *
 * Hiển thị tất cả địa chỉ giao hàng của người dùng trong RecyclerView.
 * Cho phép thêm địa chỉ mới hoặc chỉnh sửa địa chỉ hiện có.
 */

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

/**
 * Màn hình quản lý địa chỉ giao hàng đã lưu.
 *
 * Tải danh sách địa chỉ từ API và hiển thị trong RecyclerView.
 * Người dùng có thể nhấn vào địa chỉ để chỉnh sửa hoặc thêm địa chỉ mới.
 * Thanh điều hướng dưới cùng cho phép chuyển đến Trang chủ, Giỏ hàng và Cá nhân.
 */
class savedeliveryaddress : AppCompatActivity() {
    /** RecyclerView hiển thị danh sách địa chỉ */
    private lateinit var rvAddresses: RecyclerView
    /** Adapter quản lý hiển thị danh sách địa chỉ */
    private lateinit var addressAdapter: AddressAdapter
    /** Danh sách địa chỉ giao hàng của người dùng */
    private val addressList = mutableListOf<Address>()

    /**
     * Khởi tạo màn hình danh sách địa chỉ giao hàng.
     *
     * Thiết lập RecyclerView, đăng ký sự kiện cho các nút điều hướng
     * và nút thêm địa chỉ, sau đó tải danh sách địa chỉ từ API.
     *
     * @param savedInstanceState Trạng thái đã lưu trước đó (nếu có).
     */
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

    /**
     * Tải danh sách địa chỉ giao hàng của người dùng từ API.
     *
     * Lấy userId từ SharedPreferences, gọi API getUserAddresses,
     * cập nhật danh sách và thông báo cho adapter.
     */
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
