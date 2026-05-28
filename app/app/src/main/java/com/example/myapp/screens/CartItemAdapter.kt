package com.example.myapp.screens

/** @file CartItemAdapter.kt
 * @brief Adapter hiển thị các mụcCartItem trong RecyclerView.
 *
 * Hiển thị thông tin từng món ăn trong giỏ hàng bao gồm hình ảnh,
 * tên, số lượng và tổng giá tiền.
 */

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter cho RecyclerView hiển thị danh sáchCartItem.
 *
 * Mỗi item hiển thị hình ảnh món ăn, tên, số lượng và tổng giá tiền
 * (đơn giá x số lượng) với định dạng tiền Việt Nam.
 *
 * @param items Danh sách cácCartItem cần hiển thị.
 */
class CartItemAdapter(private val items: List<CartItem>) : RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    /**
     * ViewHolder chứa các view con cho mỗiCartItem.
     *
     * Bao gồm hình ảnh, tên món, số lượng và giá tiền.
     */
    class CartItemViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        /** ImageView hiển thị hình ảnh món ăn */
        private val imgCartItem: ImageView = itemView.findViewById(R.id.imgCartItem)
        /** TextView hiển thị tên món ăn */
        private val tvCartItemName: TextView = itemView.findViewById(R.id.tvCartItemName)
        /** TextView hiển thị số lượng */
        private val tvCartItemQty: TextView = itemView.findViewById(R.id.tvCartItemQty)
        /** TextView hiển thị tổng giá tiền */
        private val tvCartItemPrice: TextView = itemView.findViewById(R.id.tvCartItemPrice)

        /**
         * Gán dữ liệuCartItem vào các view con.
         *
         * Hiển thị tên, số lượng, tổng giá tiền và hình ảnh (qua Picasso).
         *
         * @param cartItem Đối tượngCartItem chứa dữ liệu cần hiển thị.
         */
        fun bind(cartItem: CartItem) {
            tvCartItemName.text = cartItem.name
            tvCartItemQty.text = "SL: ${cartItem.qty}"
            
            val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
            val totalPrice = cartItem.qty * cartItem.price
            tvCartItemPrice.text = "${fmt.format(totalPrice)}đ"

            if (!cartItem.imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(cartItem.imageUrl)
                    .placeholder(R.drawable.placeholder_loading)
                    .error(R.drawable.pngwing)
                    .into(imgCartItem)
            } else {
                imgCartItem.setImageResource(R.drawable.pngwing)
            }
        }
    }

    /**
     * Tạo ViewHolder mới khi RecyclerView cần.
     *
     * @param parent ViewGroup cha.
     * @param viewType Loại view (không sử dụng).
     * @return CartItemViewHolder mới được inflate từ layout order_cart_item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_cart_item, parent, false)
        return CartItemViewHolder(itemView)
    }

    /**
     * Gán dữ liệu vào ViewHolder tại vị trí chỉ định.
     *
     * @param holder CartItemViewHolder cần gán dữ liệu.
     * @param position Vị trí của item trong danh sách.
     */
    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    /**
     * Trả về tổng sốCartItem trong danh sách.
     *
     * @return Số lượng item.
     */
    override fun getItemCount(): Int = items.size
}
