package com.example.myapp.screens

/**
 * @file home.kt
 * @brief Màn hình chính (Home) của ứng dụng giao đồ ăn.
 *
 * Hiển thị danh sách món ăn dạng cuộn ngang (horizontal RecyclerView),
 * thanh tìm kiếm với debounce, và các nút điều hướng đến
 * Hồ sơ, Thông báo, Giỏ hàng, và Danh sách nhà hàng.
 */

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.adapters.MenuItemAdapter
import com.example.myapp.screens.api.MenuItem
import com.example.myapp.screens.api.RetrofitClient
import com.example.myapp.screens.profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity màn hình chính hiển thị danh sách món ăn.
 *
 * Chức năng chính:
 * - Hiển thị danh sách món ăn dạng RecyclerView cuộn ngang
 * - Tìm kiếm món ăn theo tên hoặc danh mục với debounce 500ms
 * - Điều hướng đến Hồ sơ, Thông báo, Giỏ hàng, Danh sách nhà hàng
 */
class home : AppCompatActivity() {
    /** Danh sách tất cả món ăn lấy từ API */
    private var menuItems: List<MenuItem> = emptyList()
    /** RecyclerView hiển thị danh sách món ăn */
    private lateinit var rvMenuItems: RecyclerView
    /** Adapter cho RecyclerView danh sách món ăn */
    private lateinit var menuItemAdapter: MenuItemAdapter
    /** EditText thanh tìm kiếm */
    private lateinit var edtSearch: EditText
    /** Call API tìm kiếm hiện tại (dùng để hủy khi có tìm kiếm mới) */
    private var searchJob: Call<List<MenuItem>>? = null
    /** Handler chạy trên main thread cho debounce tìm kiếm */
    private val searchHandler = Handler(Looper.getMainLooper())
    /** Runnable tìm kiếm hiện tại (dùng để hủy khi có tìm kiếm mới) */
    private var searchRunnable: Runnable? = null
    /** Thời gian chờ debounce tìm kiếm (500ms) */
    private val SEARCH_DELAY = 500L // 500ms delay for debouncing

    /**
     * Khởi tạo màn hình chính.
     *
     * Thiết lập layout, gán sự kiện click cho các nút điều hướng,
     * thiết lập RecyclerView, TextWatcher cho thanh tìm kiếm với debounce,
     * và gọi API lấy danh sách món ăn.
     *
     * @param savedInstanceState Trạng thái đã lưu của Activity (nếu có)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

//        click btnProfile
        val btnProfile: ImageView = findViewById(R.id.icProfile)
        btnProfile.setOnClickListener {
            val intent = Intent(this, profile::class.java)
            startActivity(intent)
        }
        
//      click textDanhSachNhaHang
        val tvDanhSachNhaHang: TextView = findViewById(R.id.tvDanhSachNhaHang)
        tvDanhSachNhaHang.setOnClickListener {
            val intent = Intent(this, list_restaurant::class.java)
            startActivity(intent)
        }

//        click btnNotification
        val btnNotification: ImageView = findViewById(R.id.imgNotification)
        btnNotification.setOnClickListener {
            val intent = Intent(this, notification::class.java)
            startActivity(intent)
        }

        val btnCart: ImageView = findViewById(R.id.icCart)
        btnCart.setOnClickListener {
            val intent = Intent(this, cart::class.java)
            startActivity(intent)
        }

        // Setup RecyclerView
        rvMenuItems = findViewById(R.id.rvMenuItems)
        rvMenuItems.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Setup Search - search as user types with debouncing
        edtSearch = findViewById(R.id.edtSearch)
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Cancel any pending search
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                
                // Create new search runnable with delay
                searchRunnable = Runnable {
                    val query = s?.toString()?.trim() ?: ""
                    if (query.isEmpty()) {
                        fetchMenuItems()
                    } else {
                        searchMenuItems(query)
                    }
                }
                
                // Post the search with delay
                searchHandler.postDelayed(searchRunnable!!, SEARCH_DELAY)
            }
        })

        // Fetch menu items from API
        fetchMenuItems()
    }

    /**
     * Gọi API lấy tất cả món ăn từ server.
     *
     * Hủy yêu cầu tìm kiếm đang chạy trước khi gửi yêu cầu mới.
     * Cập nhật danh sách menuItems và hiển thị lên RecyclerView.
     * Hiển thị Toast lỗi nếu kết nối thất bại.
     */
    private fun fetchMenuItems() {
        // Cancel any ongoing search
        searchJob?.cancel()
        
        RetrofitClient.apiService.getAllMenuItems().enqueue(object : Callback<List<MenuItem>> {
            override fun onResponse(call: Call<List<MenuItem>>, response: Response<List<MenuItem>>) {
                if (response.isSuccessful) {
                    menuItems = response.body() ?: emptyList()
                    displayMenuItems()
                } else {
                    Toast.makeText(this@home, "Lỗi khi tải danh sách món ăn", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MenuItem>>, t: Throwable) {
                if (!call.isCanceled) {
                    Toast.makeText(this@home, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Tìm kiếm món ăn theo tên.
     *
     * Hủy yêu cầu tìm kiếm cũ, gửi yêu cầu mới với từ khóa.
     * Nếu không tìm thấy theo tên, tự động chuyển sang tìm theo danh mục.
     *
     * @param query Từ khóa tìm kiếm
     */
    private fun searchMenuItems(query: String) {
        // Cancel any ongoing search
        searchJob?.cancel()
        
        // First search by name
        searchJob = RetrofitClient.apiService.searchMenuItemsByName(query)
        searchJob?.enqueue(object : Callback<List<MenuItem>> {
            override fun onResponse(call: Call<List<MenuItem>>, response: Response<List<MenuItem>>) {
                if (response.isSuccessful) {
                    val results = response.body() ?: emptyList()
                    if (results.isNotEmpty()) {
                        menuItems = results
                        displayMenuItems()
                    } else {
                        // If no results by name, search by category
                        searchByCategory(query)
                    }
                } else {
                    Toast.makeText(this@home, "Lỗi khi tìm kiếm", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MenuItem>>, t: Throwable) {
                if (!call.isCanceled) {
                    Toast.makeText(this@home, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Tìm kiếm món ăn theo danh mục.
     *
     * Được gọi khi tìm kiếm theo tên không có kết quả.
     * Hiển thị Toast nếu không tìm thấy kết quả nào.
     *
     * @param query Từ khóa tìm kiếm danh mục
     */
    private fun searchByCategory(query: String) {
        searchJob = RetrofitClient.apiService.searchMenuItemsByCategory(query)
        searchJob?.enqueue(object : Callback<List<MenuItem>> {
            override fun onResponse(call: Call<List<MenuItem>>, response: Response<List<MenuItem>>) {
                if (response.isSuccessful) {
                    val results = response.body() ?: emptyList()
                    menuItems = results
                    displayMenuItems()
                    if (results.isEmpty()) {
                        Toast.makeText(this@home, "Không tìm thấy món ăn", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@home, "Lỗi khi tìm kiếm", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MenuItem>>, t: Throwable) {
                if (!call.isCanceled) {
                    Toast.makeText(this@home, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Hiển thị danh sách món ăn lên RecyclerView.
     *
     * Tạo MenuItemAdapter mới với dữ liệu menuItems hiện tại
     * và gán cho RecyclerView.
     */
    private fun displayMenuItems() {
        menuItemAdapter = MenuItemAdapter(this, menuItems)
        rvMenuItems.adapter = menuItemAdapter
    }
}
