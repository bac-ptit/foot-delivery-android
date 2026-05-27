package com.example.myapp.screens

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
 * Màn hình chính của ứng dụng.
 *
 * Hiển thị tất cả món ăn từ mọi nhà hàng trong RecyclerView ngang.
 * Hỗ trợ tìm kiếm theo tên món và danh mục với debounce 500ms.
 *
 * Luồng hoạt động:
 * - Tải tất cả món ăn từ API khi mở màn hình
 * - Người dùng nhập từ khóa → debounce 500ms → tìm theo tên món
 * - Nếu không kết quả → tìm theo danh mục
 * - Nhấn vào món → chuyển đến [food_detail]
 * - Nhấn icon giỏ hàng → chuyển đến [cart]
 * - Nhấn icon nhà hàng → chuyển đến [list_restaurant]
 *
 * @see MenuItemAdapter
 * @see food_detail
 * @see cart
 */
class home : AppCompatActivity() {
    private var menuItems: List<MenuItem> = emptyList()
    private lateinit var rvMenuItems: RecyclerView
    private lateinit var menuItemAdapter: MenuItemAdapter
    private lateinit var edtSearch: EditText
    private var searchJob: Call<List<MenuItem>>? = null
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val SEARCH_DELAY = 500L // 500ms delay for debouncing

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

    private fun displayMenuItems() {
        menuItemAdapter = MenuItemAdapter(this, menuItems)
        rvMenuItems.adapter = menuItemAdapter
    }
}
