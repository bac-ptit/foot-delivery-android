package com.example.myapp.screens

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

class CartItemAdapter(private val items: List<CartItem>) : RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    class CartItemViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val imgCartItem: ImageView = itemView.findViewById(R.id.imgCartItem)
        private val tvCartItemName: TextView = itemView.findViewById(R.id.tvCartItemName)
        private val tvCartItemQty: TextView = itemView.findViewById(R.id.tvCartItemQty)
        private val tvCartItemPrice: TextView = itemView.findViewById(R.id.tvCartItemPrice)

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_cart_item, parent, false)
        return CartItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
