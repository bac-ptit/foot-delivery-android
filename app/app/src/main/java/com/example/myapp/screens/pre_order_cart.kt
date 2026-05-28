package com.example.myapp.screens

/** @file pre_order_cart.kt
 * @brief Màn hình giỏ hàng đặt trước.
 *
 * Hiển thị danh sách các món ăn đã được đặt trước, cho phép người dùng
 * chọn/bỏ chọn, điều chỉnh số lượng, xóa món và tiến hành thanh toán.
 */

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
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

/**
 * Dữ liệu đại diện cho một món ăn trong giỏ hàng đặt trước.
 *
 * @property id ID duy nhất của món ăn.
 * @property name Tên món ăn.
 * @property price Đơn giá của món ăn (đơn vị: đồng).
 * @property qty Số lượng đặt hàng, có thể thay đổi.
 * @property imageUrl URL hình ảnh món ăn, có thể null.
 * @property deliveryTime Thời gian giao hàng dự kiến (định dạng: "HH:mm, d/M/yyyy").
 * @property isSelected Trạng thái được chọn trong giỏ hàng, mặc định là false.
 */
data class PreOrderItem(
    val id: Int,
    val name: String,
    val price: Int,
    var qty: Int,
    val imageUrl: String? = null,
    val deliveryTime: String,
    var isSelected: Boolean = false
)

/**
 * Màn hình giỏ hàng đặt trước.
 *
 * Hiển thị danh sách các món ăn đã đặt trước trong RecyclerView.
 * Người dùng có thể chọn/bỏ chọn từng món hoặc tất cả, điều chỉnh số lượng,
 * xóa món, và tiến hành thanh toán. Chuyển đổi PreOrderItem sang CartItem
 * để tương thích với màn hình order.
 */
class pre_order_cart : AppCompatActivity() {
    /** RecyclerView hiển thị danh sách món ăn đặt trước */
    private lateinit var rvPreOrderCart: RecyclerView
    /** TextView hiển thị tổng số lượng món được chọn */
    private lateinit var tvTotalItems: TextView
    /** TextView hiển thị tổng tiền của các món được chọn */
    private lateinit var tvTotalPrice: TextView
    /** CheckBox chọn/bỏ chọn tất cả món */
    private lateinit var cbSelectAll: CheckBox
    /** Nút tiến hành thanh toán */
    private lateinit var btnCheckout: AppCompatButton
    /** Adapter quản lý hiển thị danh sách đặt trước */
    private lateinit var adapter: PreOrderCartAdapter

    companion object {
        /** Danh sách toàn cục các món ăn đặt trước, được chia sẻ giữa các Activity */
        val preOrderList = mutableListOf<PreOrderItem>()
    }

    /**
     * Khởi tạo màn hình giỏ hàng đặt trước.
     *
     * Thiết lập RecyclerView với adapter, đăng ký sự kiện cho nút quay lại,
     * nút chuyển sang giỏ hàng thường, nút thanh toán, và checkbox chọn tất cả.
     *
     * @param savedInstanceState Trạng thái đã lưu trước đó (nếu có).
     */
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

    /**
     * Cập nhật thông tin tóm tắt giỏ hàng.
     *
     * Tính tổng số lượng và tổng tiền của các món được chọn,
     * cập nhật giao diện và kích hoạt/vô hiệu hóa nút thanh toán.
     */
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

/**
 * Adapter cho RecyclerView hiển thị danh sách món ăn đặt trước.
 *
 * Quản lý hiển thị từng item với hình ảnh, tên, giá, số lượng,
 * thời gian giao hàng, checkbox chọn và nút xóa.
 *
 * @param items Danh sách các món ăn đặt trước.
 * @param onUpdate Callback được gọi khi có thay đổi số lượng hoặc trạng thái chọn.
 * @param onDelete Callback được gọi khi xóa một món, truyền vào vị trí cần xóa.
 */
class PreOrderCartAdapter(
    private val items: MutableList<PreOrderItem>,
    private val onUpdate: () -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<PreOrderCartAdapter.ViewHolder>() {

    /**
     * ViewHolder chứa các view con cho mỗi item trong danh sách đặt trước.
     *
     * @param view View gốc của item.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /** ImageView hiển thị hình ảnh món ăn */
        val imgFood: ImageView = view.findViewById(R.id.imgFood)
        /** TextView hiển thị tên món ăn */
        val tvName: TextView = view.findViewById(R.id.tvName)
        /** TextView hiển thị đơn giá */
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        /** TextView hiển thị số lượng */
        val tvQty: TextView = view.findViewById(R.id.tvQty)
        /** TextView hiển thị thời gian giao hàng dự kiến */
        val tvDeliveryTime: TextView = view.findViewById(R.id.tvDeliveryTime)
        /** Nút tăng số lượng */
        val btnPlus: TextView = view.findViewById(R.id.btnPlus)
        /** Nút giảm số lượng */
        val btnMinus: TextView = view.findViewById(R.id.btnMinus)
        /** CheckBox chọn/bỏ chọn món ăn */
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
        /** Nút xóa món ăn khỏi danh sách */
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    /**
     * Tạo ViewHolder mới khi RecyclerView cần.
     *
     * @param parent ViewGroup cha.
     * @param viewType Loại view (không sử dụng).
     * @return ViewHolder mới được inflate từ layout item_pre_order_cart.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pre_order_cart, parent, false)
        return ViewHolder(view)
    }

    /**
     * Gán dữ liệu món ăn vào ViewHolder tại vị trí chỉ định.
     *
     * Thiết lập hình ảnh, tên, giá, số lượng, thời gian giao hàng,
     * trạng thái checkbox và các sự kiện nhấn (tăng/giảm SL, chọn, xóa).
     *
     * @param holder ViewHolder cần gán dữ liệu.
     * @param position Vị trí của item trong danh sách.
     */
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

    /**
     * Trả về tổng số lượng item trong danh sách.
     *
     * @return Số lượng món ăn đặt trước.
     */
    override fun getItemCount() = items.size
}