package com.example.myapp.adapters

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
 * Adapter hiển thị danh sách nhà hàng.
 *
 * Mỗi item bao gồm: hình ảnh, tên nhà hàng, đánh giá, giờ mở cửa.
 * Nhấn vào item → mở [restaurant_profile].
 *
 * @param context Context hiện tại.
 * @param restaurants Danh sách nhà hàng cần hiển thị.
 * @see Restaurant
 * @see restaurant_profile
 */
class RestaurantAdapter(
    private val context: Context,
    private val restaurants: List<Restaurant>
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFood: ImageView = itemView.findViewById(R.id.imgFood)
        val tvRestaurantName: TextView = itemView.findViewById(R.id.tvRestaurantName)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val tvOpeningHours: TextView = itemView.findViewById(R.id.tvOpeningHours)
        val foodTitleCard: View = itemView.findViewById(R.id.foodTitleCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

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

    override fun getItemCount(): Int = restaurants.size
}
