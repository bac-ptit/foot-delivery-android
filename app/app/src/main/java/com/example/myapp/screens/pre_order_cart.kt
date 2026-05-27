package com.example.myapp.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R

/**
 * Giỏ hàng đặt trước (pre-order cart).
 *
 * Hiển thị danh sách món đã chọn để đặt trước.
 * Chuyển đổi PreOrderItem → CartItem và gửi đến [order] qua Intent.
 *
 * @see PreOrderItem
 * @see pre_order
 * @see order
 */
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

// 1. Khai báo dữ liệu món ăn đặt trước
data class PreOrderItem(
    val id: Int,
    val name: String,
    val price: Int,
    var qty: Int,
    val imageUrl: String? = null,
    val deliveryTime: String,
    var isSelected: Boolean = false
)

class pre_order_cart : AppCompatActivity() {
    private lateinit var rvPreOrderCart: RecyclerView
    private lateinit var tvTotalItems: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var cbSelectAll: CheckBox
    private lateinit var btnCheckout: AppCompatButton
    private lateinit var adapter: PreOrderCartAdapter

    companion object {
        val preOrderList = mutableListOf<PreOrderItem>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_order_cart)

        // Ánh xạ View
        rvPreOrderCart = findViewById(R.id.rvPreOrderCart)
        tvTotalItems = findViewById(R.id.tvTotalItems)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        cbSelectAll = findViewById(R.id.cbSelectAll)
        btnCheckout = findViewById(R.id.btnCheckout)
        val btnBack: ImageView = findViewById(R.id.btnBack)
        val btnNormalCart: ImageView = findViewById(R.id.btnNormalCart)

        // Nút quay lại
        btnBack.setOnClickListener { finish() }

        // Quay lại giỏ hàng thường
        btnNormalCart.setOnClickListener {
            val intent = Intent(this, cart::class.java)
            startActivity(intent)
            finish()
        }

        // --- SỰ KIỆN CLICK THANH TOÁN ĐẶT TRƯỚC ---
        btnCheckout.setOnClickListener {
            val selectedItems = preOrderList.filter { it.isSelected }
            if (selectedItems.isEmpty()) return@setOnClickListener

            // Chuyển đổi PreOrderItem sang CartItem để tương thích với màn hình order
            val cartItems = ArrayList<CartItem>(selectedItems.map {
                CartItem(
                    id = it.id,
                    name = it.name,
                    price = it.price,
                    qty = it.qty,
                    imageUrl = it.imageUrl,
                    isSelected = true
                )
            })

            // Chuyển sang màn hình order và gửi danh sách món ăn
            val intent = Intent(this, order::class.java)
            intent.putExtra("selected_items", cartItems)
            intent.putExtra("is_pre_order", true)
            startActivity(intent)
        }

        // Thiết lập RecyclerView
        adapter = PreOrderCartAdapter(
            preOrderList,
            onUpdate = { updateSummary() },
            onDelete = { position ->
                preOrderList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, preOrderList.size)
                updateSummary()
            }
        )
        rvPreOrderCart.layoutManager = LinearLayoutManager(this)
        rvPreOrderCart.adapter = adapter

        // Chọn tất cả
        cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            preOrderList.forEach { it.isSelected = isChecked }
            adapter.notifyDataSetChanged()
            updateSummary()
        }

        updateSummary()
    }

    private fun updateSummary() {
        val selectedItems = preOrderList.filter { it.isSelected }
        val totalQty = selectedItems.sumOf { it.qty }
        val totalPrice = selectedItems.sumOf { it.qty * it.price }

        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        tvTotalItems.text = totalQty.toString()
        tvTotalPrice.text = fmt.format(totalPrice) + "đ"

        // Chỉ cho phép bấm nếu có chọn món
        btnCheckout.isEnabled = selectedItems.isNotEmpty()
        btnCheckout.alpha = if (selectedItems.isNotEmpty()) 1.0f else 0.5f
    }
}

// Adapter cho RecyclerView đặt trước
class PreOrderCartAdapter(
    private val items: MutableList<PreOrderItem>,
    private val onUpdate: () -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<PreOrderCartAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgFood: ImageView = view.findViewById(R.id.imgFood)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvQty: TextView = view.findViewById(R.id.tvQty)
        val tvDeliveryTime: TextView = view.findViewById(R.id.tvDeliveryTime)
        val btnPlus: TextView = view.findViewById(R.id.btnPlus)
        val btnMinus: TextView = view.findViewById(R.id.btnMinus)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pre_order_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))

        holder.tvName.text = item.name
        holder.tvPrice.text = fmt.format(item.price) + "đ"
        holder.tvQty.text = item.qty.toString()
        holder.tvDeliveryTime.text = "Giao lúc: " + item.deliveryTime
        holder.cbSelect.isChecked = item.isSelected

        if (!item.imageUrl.isNullOrEmpty()) {
            Picasso.get().load(item.imageUrl)
                .placeholder(R.drawable.placeholder_loading)
                .error(R.drawable.pngwing)
                .into(holder.imgFood)
        } else {
            holder.imgFood.setImageResource(R.drawable.pngwing)
        }

        holder.btnPlus.setOnClickListener {
            item.qty++
            notifyItemChanged(holder.adapterPosition)
            onUpdate()
        }

        holder.btnMinus.setOnClickListener {
            if (item.qty > 1) {
                item.qty--
                notifyItemChanged(holder.adapterPosition)
                onUpdate()
            }
        }

        holder.cbSelect.setOnClickListener {
            item.isSelected = holder.cbSelect.isChecked
            onUpdate()
        }

        holder.btnDelete.setOnClickListener {
            onDelete(holder.adapterPosition)
        }
    }

    override fun getItemCount() = items.size
}