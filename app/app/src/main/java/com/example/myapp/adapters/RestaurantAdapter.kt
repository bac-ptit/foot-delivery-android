package com.example.myapp.adapters

/**
 * @file RestaurantAdapter.kt
 * @brief Adapter hiển thị danh sách nhà hàng trong RecyclerView.
 */

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.screens.api.Restaurant
import com.example.myapp.screens.restaurant_profile
import com.squareup.picasso.Picasso

/**
 * Adapter hiển thị danh sách nhà hàng trong RecyclerView.
 * Mỗi item bao gồm hình ảnh, tên, đánh giá và giờ mở cửa.
 * Nhấn vào card sẽ chuyển đến trang hồ sơ nhà hàng.
 *
 * @property context Context hiện tại, dùng để inflate layout và startActivity.
 * @property restaurants Danh sách các nhà hàng cần hiển thị.
 */
class RestaurantAdapter(
    private val context: Context,
    private val restaurants: List<Restaurant>
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    /**
     * ViewHolder chứa các view hiển thị thông tin của một nhà hàng.
     *
     * @property imgFood ImageView hiển thị hình ảnh nhà hàng.
     * @property tvRestaurantName TextView hiển thị tên nhà hàng.
     * @property tvRating TextView hiển thị đánh giá của nhà hàng.
     * @property tvOpeningHours TextView hiển thị giờ mở cửa.
     * @property foodTitleCard View gốc của card, dùng để gán sự kiện nhấn.
     */
    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFood: ImageView = itemView.findViewById(R.id.imgFood)
        val tvRestaurantName: TextView = itemView.findViewById(R.id.tvRestaurantName)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val tvOpeningHours: TextView = itemView.findViewById(R.id.tvOpeningHours)
        val foodTitleCard: View = itemView.findViewById(R.id.foodTitleCard)
    }

    /**
     * Tạo ViewHolder mới bằng cách inflate layout item_restaurant.
     *
     * @param parent ViewGroup cha chứa RecyclerView.
     * @param viewType Loại view (không sử dụng trong trường hợp này).
     * @return Đối tượng RestaurantViewHolder mới được tạo.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    /**
     * Gán dữ liệu nhà hàng vào ViewHolder tại vị trí cho trước.
     * Tải hình ảnh bằng Picasso, hiển thị tên, đánh giá, giờ mở cửa
     * và thiết lập sự kiện nhấn để mở trang hồ sơ nhà hàng.
     *
     * @param holder ViewHolder cần gán dữ liệu.
     * @param position Vị trí của item trong danh sách.
     */
    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]

        // Load restaurant image
        if (!restaurant.image_url.isNullOrEmpty()) {
            Picasso.get()
                .load(restaurant.image_url)
                .placeholder(R.drawable.placeholder_loading)
                .error(R.drawable.pngwing)
                .into(holder.imgFood)
        }

        // Set restaurant name
        holder.tvRestaurantName.text = restaurant.name

        // Set restaurant rating
        holder.tvRating.text = restaurant.rating?.toString() ?: "N/A"

        // Set opening hours
        val openTime = restaurant.open_time ?: "N/A"
        val closeTime = restaurant.close_time ?: "N/A"
        holder.tvOpeningHours.text = "Giờ mở cửa: $openTime - $closeTime"

        // Set click listener for foodTitleCard
        holder.foodTitleCard.setOnClickListener {
            val intent = Intent(context, restaurant_profile::class.java)
            intent.putExtra("RESTAURANT_ID", restaurant.id)
            context.startActivity(intent)
        }
    }

    /**
     * Trả về tổng số nhà hàng trong danh sách.
     *
     * @return Số lượng nhà hàng.
     */
    override fun getItemCount(): Int = restaurants.size
}
