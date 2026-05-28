package com.example.myapp.screens

/**
 * @file restaurant_profile.kt
 * @brief Màn hình hồ sơ nhà hàng.
 *
 * Hiển thị thông tin chi tiết của một nhà hàng bao gồm hình ảnh banner,
 * tên, đánh giá, địa chỉ, giờ mở cửa, số điện thoại và mô tả.
 * Hiển thị danh sách các món ăn có sẵn dạng RecyclerView cuộn ngang.
 * Hỗ trợ chia sẻ thông tin nhà hàng.
 */

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.adapters.MenuItemAdapterSmall
import com.example.myapp.screens.api.RetrofitClient
import com.example.myapp.screens.api.Restaurant
import com.example.myapp.screens.home
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity hiển thị hồ sơ chi tiết của nhà hàng.
 *
 * Nhận `RESTAURANT_ID` từ Intent, tải thông tin nhà hàng từ API
 * và hiển thị banner, tên, đánh giá, địa chỉ, giờ mở cửa, số điện thoại,
 * mô tả và danh sách món ăn khả dụng.
 *
 * @property restaurant Đối tượng nhà hàng đã tải từ API
 * @property rvMenuItems RecyclerView hiển thị danh sách món ăn
 * @property menuItemAdapter Adapter cho danh sách món ăn
 */
class restaurant_profile: AppCompatActivity()  {
    private var restaurant: Restaurant? = null
    private lateinit var rvMenuItems: RecyclerView
    private lateinit var menuItemAdapter: MenuItemAdapterSmall

    /**
     * Khởi tạo Activity, thiết lập giao diện và các sự kiện click.
     *
     * Thiết lập các nút điều hướng (quay lại, profile, home, giỏ hàng, chia sẻ),
     * RecyclerView cho danh sách món ăn và tải chi tiết nhà hàng từ API.
     * Hiển thị Toast và đóng Activity nếu không tìm thấy ID nhà hàng.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurant_profile)

        // click btnBack
        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        // click icProfile
        val icProfile: ImageView = findViewById(R.id.icProfile)
        icProfile.setOnClickListener {
            val intent = Intent(this, profile::class.java)
            startActivity(intent)
        }

        // click icHome
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

        val btn_share: ImageView = findViewById(R.id.btn_share)
        btn_share.setOnClickListener {
            restaurant?.let { rest ->
                val intent = Intent(this, share_restaurant::class.java).apply {
                    putExtra("restaurant_name", rest.name)
                    putExtra("restaurant_address", rest.address)
                    putExtra("restaurant_rating", rest.rating ?: 0)
                    putExtra("restaurant_image_url", rest.image_url)
                    putExtra("restaurant_open_time", rest.open_time)
                    putExtra("restaurant_close_time", rest.close_time)
                }
                startActivity(intent)
            }
        }

        // Setup RecyclerView
        rvMenuItems = findViewById(R.id.rvMenuItems)
        rvMenuItems.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Get restaurant ID from intent
        val restaurantId = intent.getIntExtra("RESTAURANT_ID", -1)
        if (restaurantId != -1) {
            fetchRestaurantDetails(restaurantId)
        } else {
            Toast.makeText(this, "Không tìm thấy nhà hàng", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * Tải chi tiết nhà hàng từ API theo ID.
     *
     * Gọi API getRestaurant và hiển thị thông tin lên giao diện.
     * Hiển thị Toast nếu tải thất bại.
     *
     * @param restaurantId ID của nhà hàng cần tải
     */
    private fun fetchRestaurantDetails(restaurantId: Int) {
        RetrofitClient.apiService.getRestaurant(restaurantId).enqueue(object : Callback<Restaurant> {
            override fun onResponse(call: Call<Restaurant>, response: Response<Restaurant>) {
                if (response.isSuccessful) {
                    restaurant = response.body()
                    displayRestaurantDetails()
                } else {
                    Toast.makeText(this@restaurant_profile, "Lỗi khi tải thông tin nhà hàng", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Restaurant>, t: Throwable) {
                Toast.makeText(this@restaurant_profile, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Hiển thị thông tin chi tiết nhà hàng lên giao diện.
     *
     * Hiển thị banner image (qua Picasso), tên, đánh giá, địa chỉ,
     * giờ mở cửa, số điện thoại, mô tả và danh sách món ăn khả dụng.
     */
    private fun displayRestaurantDetails() {
        restaurant?.let { restaurant ->
            // Display restaurant banner image
            val imgRestaurantBanner: ImageView = findViewById(R.id.imgRestaurantBanner)
            if (!restaurant.image_url.isNullOrEmpty()) {
                Picasso.get()
                    .load(restaurant.image_url)
                    .placeholder(R.drawable.placeholder_loading)
                    .error(R.drawable.pngwing)
                    .into(imgRestaurantBanner)
            }

            // Display restaurant name
            val tvRestaurantName: TextView = findViewById(R.id.tvRestaurantName)
            tvRestaurantName.text = restaurant.name

            // Display restaurant rating
            val tvRating: TextView = findViewById(R.id.tvRating)
            tvRating.text = restaurant.rating?.toString() ?: "N/A"

            // Display restaurant address
            val tvAddress: TextView = findViewById(R.id.tvAddress)
            tvAddress.text = restaurant.address ?: "Chưa có địa chỉ"

            // Display opening hours
            val tvOpeningHours: TextView = findViewById(R.id.tvOpeningHours)
            val openTime = restaurant.open_time ?: "N/A"
            val closeTime = restaurant.close_time ?: "N/A"
            tvOpeningHours.text = "Giờ mở cửa: $openTime - $closeTime"

            // Display phone number
            val tvPhoneNumber: TextView = findViewById(R.id.tvPhoneNumber)
            tvPhoneNumber.text = "Số điện thoại: ${restaurant.phone_number}"

            // Display description
            val tvDescription: TextView = findViewById(R.id.tvDescription)
            tvDescription.text = "Mô tả: ${restaurant.description ?: "Chưa có mô tả"}"

            // Display menu items
            displayMenuItems(restaurant.menu_items)
        }
    }

    /**
     * Hiển thị danh sách món ăn khả dụng của nhà hàng.
     *
     * Lọc chỉ các món ăn có sẵn (is_available = true) và gán adapter
     * cho RecyclerView.
     *
     * @param menuItems Danh sách tất cả món ăn của nhà hàng
     */
    private fun displayMenuItems(menuItems: List<com.example.myapp.screens.api.MenuItem>) {
        val availableMenuItems = menuItems.filter { it.is_available }
        menuItemAdapter = MenuItemAdapterSmall(this, availableMenuItems)
        rvMenuItems.adapter = menuItemAdapter
    }
}
