package com.example.myapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.screens.api.MenuItem
import com.example.myapp.screens.food_detail
import com.squareup.picasso.Picasso

class MenuItemAdapter(
    private val context: Context,
    private val menuItems: List<MenuItem>
) : RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder>() {

    class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFood: ImageView = itemView.findViewById(R.id.imgFood)
        val tvFoodName: TextView = itemView.findViewById(R.id.tvFoodName)
        val tvFoodPrice: TextView = itemView.findViewById(R.id.tvFoodPrice)
        val tvRestaurantName: TextView = itemView.findViewById(R.id.tvRestaurantName)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val foodTitleCard: View = itemView.findViewById(R.id.foodTitleCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false)
        return MenuItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val menuItem = menuItems[position]

        // Load menu item image
        if (!menuItem.image_url.isNullOrEmpty()) {
            Picasso.get()
                .load(menuItem.image_url)
                .placeholder(R.drawable.placeholder_loading)
                .error(R.drawable.pngwing)
                .into(holder.imgFood)
        }

        // Set menu item name
        holder.tvFoodName.text = menuItem.name

        // Set menu item price
        holder.tvFoodPrice.text = "${menuItem.price} VNĐ"

        // Set restaurant name
        holder.tvRestaurantName.text = menuItem.restaurant_name ?: ""

        // Set rating
        val avgRating = menuItem.avg_rating ?: 0f
        holder.ratingBar.rating = avgRating
        holder.tvRating.text = String.format("(%.1f)", avgRating)

        // Set description
        holder.tvDescription.text = menuItem.description ?: ""

        // Set click listener on foodTitleCard to navigate to food_detail
        holder.foodTitleCard.setOnClickListener {
            val intent = Intent(context, food_detail::class.java).apply {
                putExtra("food_id", menuItem.id) // Pass food_id
                putExtra("food_name", menuItem.name)
                putExtra("food_price", menuItem.price)
                putExtra("food_description", menuItem.description ?: "")
                putExtra("food_image_url", menuItem.image_url ?: "")
                putExtra("restaurant_name", menuItem.restaurant_name ?: "")
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = menuItems.size
}
