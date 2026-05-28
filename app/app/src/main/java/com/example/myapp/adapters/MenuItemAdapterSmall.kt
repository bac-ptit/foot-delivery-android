package com.example.myapp.adapters

/**
 * @file MenuItemAdapterSmall.kt
 * @brief Adapter hiển thị món ăn dưới dạng compact (thu nhỏ), phù hợp cho danh sách ngang.
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
import com.example.myapp.screens.api.MenuItem
import com.example.myapp.screens.food_detail
import com.squareup.picasso.Picasso

/**
 * Adapter hiển thị danh sách món ăn dưới dạng compact (thu nhỏ).
 * Phù hợp cho hiển thị danh sách ngang hoặc không gian hạn chế.
 * Nhấn vào card sẽ chuyển đến trang chi tiết món ăn.
 *
 * @property context Context hiện tại, dùng để inflate layout và startActivity.
 * @property menuItems Danh sách các món ăn cần hiển thị.
 */
class MenuItemAdapterSmall(
    private val context: Context,
    private val menuItems: List<MenuItem>
) : RecyclerView.Adapter<MenuItemAdapterSmall.MenuItemViewHolder>() {

    /**
     * ViewHolder chứa các view hiển thị thông tin tóm tắt của một món ăn.
     *
     * @property imgFood ImageView hiển thị hình ảnh món ăn.
     * @property tvFoodName TextView hiển thị tên món ăn.
     * @property tvFoodPrice TextView hiển thị giá món ăn.
     * @property foodTitleCard View gốc của card, dùng để gán sự kiện nhấn.
     */
    class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFood: ImageView = itemView.findViewById(R.id.imgFood)
        val tvFoodName: TextView = itemView.findViewById(R.id.tvFoodName)
        val tvFoodPrice: TextView = itemView.findViewById(R.id.tvFoodPrice)
        val foodTitleCard: View = itemView.findViewById(R.id.foodTitleCard)
    }

    /**
     * Tạo ViewHolder mới bằng cách inflate layout item_menu_small.
     *
     * @param parent ViewGroup cha chứa RecyclerView.
     * @param viewType Loại view (không sử dụng trong trường hợp này).
     * @return Đối tượng MenuItemViewHolder mới được tạo.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_menu_small, parent, false)
        return MenuItemViewHolder(view)
    }

    /**
     * Gán dữ liệu món ăn vào ViewHolder tại vị trí cho trước.
     * Tải hình ảnh bằng Picasso, hiển thị tên, giá
     * và thiết lập sự kiện nhấn để mở trang chi tiết.
     *
     * @param holder ViewHolder cần gán dữ liệu.
     * @param position Vị trí của item trong danh sách.
     */
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

    /**
     * Trả về tổng số món ăn trong danh sách.
     *
     * @return Số lượng món ăn.
     */
    override fun getItemCount(): Int = menuItems.size
}
