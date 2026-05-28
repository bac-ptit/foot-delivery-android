package com.example.myapp.screens

/**
 * @file list_restaurant.kt
 * @brief Màn hình danh sách nhà hàng.
 *
 * Hiển thị danh sách nhà hàng dạng cuộn ngang, hỗ trợ tìm kiếm
 * nhà hàng theo tên với debounce 500ms. Người dùng có thể chọn
 * nhà hàng để xem chi tiết hoặc điều hướng đến các màn hình khác
 * (trang chủ, profile, giỏ hàng, thông báo).
 */

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.adapters.RestaurantAdapter
import com.example.myapp.screens.api.RetrofitClient
import com.example.myapp.screens.api.Restaurant
import com.example.myapp.screens.profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity hiển thị danh sách nhà hàng.
 *
 * Tải danh sách nhà hàng từ API và hiển thị dạng RecyclerView cuộn ngang.
 * Hỗ trợ tìm kiếm nhà hàng theo tên với cơ chế debounce để tránh gọi API quá nhiều.
 *
 * @property restaurants Danh sách nhà hàng hiện tại
 * @property rvRestaurants RecyclerView hiển thị danh sách nhà hàng
 * @property restaurantAdapter Adapter cho RecyclerView
 * @property edtSearch Ô nhập liệu tìm kiếm
 * @property searchJob Call API tìm kiếm hiện tại (để hủy khi cần)
 * @property searchHandler Handler để trì hoãn tìm kiếm
 * @property searchRunnable Runnable tìm kiếm hiện tại
 * @property SEARCH_DELAY Thời gian trì hoãn tìm kiếm (500ms)
 */
class list_restaurant: AppCompatActivity() {
    private var restaurants: List<Restaurant> = emptyList()
    private lateinit var rvRestaurants: RecyclerView
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var edtSearch: EditText
    private var searchJob: Call<List<Restaurant>>? = null
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val SEARCH_DELAY = 500L // 500ms delay for debouncing

    /**
     * Khởi tạo Activity, thiết lập giao diện và các sự kiện.
     *
     * Thiết lập các nút điều hướng (profile, home, giỏ hàng, thông báo),
     * RecyclerView hiển thị danh sách nhà hàng, ô tìm kiếm với debounce
     * và tải danh sách nhà hàng từ API.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_restaurant)

//        click icProfile
        val icProfile: ImageView = findViewById(R.id.icProfile)
        icProfile.setOnClickListener {
            val intent = Intent(this, profile::class.java)
            startActivity(intent)
        }
//        click textMonAn
        val tvMonAn: TextView = findViewById(R.id.tvMonAn)
        tvMonAn.setOnClickListener {
            val intent = Intent(this, home::class.java)
            startActivity(intent)
        }
//        click icHome
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
//        click imgNotification
        val imgNotification: ImageView = findViewById(R.id.imgNotification)
        imgNotification.setOnClickListener {
            val intent = Intent(this, notification::class.java)
            startActivity(intent)
        }

        // Setup RecyclerView
        rvRestaurants = findViewById(R.id.rvRestaurants)
        rvRestaurants.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

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
                        fetchRestaurants()
                    } else {
                        searchRestaurants(query)
                    }
                }
                
                // Post the search with delay
                searchHandler.postDelayed(searchRunnable!!, SEARCH_DELAY)
            }
        })

        // Fetch restaurants from API
        fetchRestaurants()
    }

    /**
     * Tải toàn bộ danh sách nhà hàng từ API.
     *
     * Hủy bất kỳ tìm kiếm đang chạy trước khi gọi API getRestaurants().
     * Cập nhật RecyclerView khi nhận dữ liệu thành công.
     */
    private fun fetchRestaurants() {
        // Cancel any ongoing search
        searchJob?.cancel()
        
        RetrofitClient.apiService.getRestaurants().enqueue(object : Callback<List<Restaurant>> {
            override fun onResponse(call: Call<List<Restaurant>>, response: Response<List<Restaurant>>) {
                if (response.isSuccessful) {
                    restaurants = response.body() ?: emptyList()
                    displayRestaurants()
                } else {
                    Toast.makeText(this@list_restaurant, "Lỗi khi tải danh sách nhà hàng", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Restaurant>>, t: Throwable) {
                if (!call.isCanceled) {
                    Toast.makeText(this@list_restaurant, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Tìm kiếm nhà hàng theo tên từ API.
     *
     * Hủy tìm kiếm trước đó, gọi API searchRestaurantsByName với query.
     * Hiển thị Toast nếu không tìm thấy kết quả.
     *
     * @param query Từ khóa tìm kiếm tên nhà hàng
     */
    private fun searchRestaurants(query: String) {
        // Cancel any ongoing search
        searchJob?.cancel()
        
        searchJob = RetrofitClient.apiService.searchRestaurantsByName(query)
        searchJob?.enqueue(object : Callback<List<Restaurant>> {
            override fun onResponse(call: Call<List<Restaurant>>, response: Response<List<Restaurant>>) {
                if (response.isSuccessful) {
                    val results = response.body() ?: emptyList()
                    restaurants = results
                    displayRestaurants()
                    if (results.isEmpty()) {
                        Toast.makeText(this@list_restaurant, "Không tìm thấy nhà hàng", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@list_restaurant, "Lỗi khi tìm kiếm", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Restaurant>>, t: Throwable) {
                if (!call.isCanceled) {
                    Toast.makeText(this@list_restaurant, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Hiển thị danh sách nhà hàng lên RecyclerView.
     *
     * Tạo mới RestaurantAdapter với danh sách hiện tại và gán cho RecyclerView.
     */
    private fun displayRestaurants() {
        restaurantAdapter = RestaurantAdapter(this, restaurants)
        rvRestaurants.adapter = restaurantAdapter
    }
}
