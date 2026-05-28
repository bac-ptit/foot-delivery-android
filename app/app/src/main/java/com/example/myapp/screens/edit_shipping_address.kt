package com.example.myapp.screens

/** @file edit_shipping_address.kt
 * @brief Màn hình chỉnh sửa địa chỉ giao hàng.
 *
 * Cho phép người dùng cập nhật số điện thoại và địa chỉ giao hàng hiện có.
 * Tải thông tin địa chỉ từ API và gọi API cập nhật khi người dùng lưu thay đổi.
 */

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

/**
 * Màn hình chỉnh sửa địa chỉ giao hàng.
 *
 * Nhận address_id từ Intent, tải thông tin địa chỉ hiện tại từ API,
 * hiển thị lên form cho người dùng chỉnh sửa. Sau khi cập nhật thành công,
 * chuyển sang màn hình thông báo sửa địa chỉ thành công.
 */
class edit_shipping_address : AppCompatActivity() {
    /** EditText nhập số điện thoại liên hệ */
    private lateinit var edtPhone: EditText
    /** EditText nhập địa chỉ giao hàng */
    private lateinit var edtAddress: EditText
    /** Nút lưu chỉnh sửa địa chỉ */
    private lateinit var btnEdit: Button
    /** ID của địa chỉ cần chỉnh sửa */
    private var addressId: Int = -1

    /**
     * Khởi tạo màn hình chỉnh sửa địa chỉ giao hàng.
     *
     * Lấy address_id từ Intent, tải dữ liệu địa chỉ hiện tại,
     * thiết lập form chỉnh sửa và các sự kiện nhấn.
     *
     * @param savedInstanceState Trạng thái đã lưu trước đó (nếu có).
     */
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

    /**
     * Tải thông tin địa chỉ giao hàng hiện tại từ API.
     *
     * Lấy danh sách địa chỉ theo userId, tìm địa chỉ có ID trùng khớp
     * và hiển thị số điện thoại, địa chỉ lên form. Đóng màn hình nếu không tìm thấy.
     */
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

    /**
     * Gọi API cập nhật địa chỉ giao hàng.
     *
     * Tạo Retrofit client, xây dựng request với số điện thoại và địa chỉ mới,
     * gọi API updateAddress. Chuyển sang màn hình thành công nếu cập nhật thành công.
     *
     * @param phone Số điện thoại liên hệ mới.
     * @param address Địa chỉ giao hàng mới.
     */
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
