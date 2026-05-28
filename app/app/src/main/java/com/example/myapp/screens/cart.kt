package com.example.myapp.screens

/**
 * @file cart.kt
 * @brief Màn hình giỏ hàng của ứng dụng giao đồ ăn.
 *
 * Hiển thị danh sách các món ăn đã thêm vào giỏ hàng, cho phép tăng/giảm
 * số lượng, chọn/bỏ chọn từng món hoặc chọn tất cả, xóa món, và
 * chuyển sang màn hình đặt hàng. Bao gồm data class CartItem và CartAdapter.
 */

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.Locale

import java.io.Serializable

/**
 * Data class đại diện cho một món ăn trong giỏ hàng.
 *
 * @property id ID duy nhất của món ăn
 * @property name Tên món ăn
 * @property price Đơn giá của món ăn (đơn vị: VNĐ)
 * @property qty Số lượng đặt mua
 * @property imageUrl URL hình ảnh món ăn (có thể null)
 * @property isSelected Trạng thái chọn để thanh toán
 */
data class CartItem(
    val id: Int,
    val name: String,
    val price: Int,
    var qty: Int,
    val imageUrl: String? = null,
    var isSelected: Boolean = false
) : Serializable

/**
 * Activity màn hình giỏ hàng.
 *
 * Chức năng chính:
 * - Hiển thị danh sách món ăn trong giỏ hàng dạng RecyclerView
 * - Chọn/bỏ chọn từng món hoặc chọn tất cả
 * - Tăng/giảm số lượng, xóa món khỏi giỏ
 * - Tính tổng tiền và tổng số lượng các món đã chọn
 * - Chuyển sang màn hình đặt hàng khi nhấn Thanh toán
 */
class cart : AppCompatActivity() {
    /** RecyclerView hiển thị danh sách giỏ hàng */
    private lateinit var rvCart: RecyclerView
    /** TextView hiển thị tổng số lượng món đã chọn */
    private lateinit var tvTotalItems: TextView
    /** TextView hiển thị tổng tiền các món đã chọn */
    private lateinit var tvTotalPrice: TextView
    /** CheckBox chọn/bỏ chọn tất cả món */
    private lateinit var cbSelectAll: CheckBox
    /** Nút thanh toán/chuyển sang đặt hàng */
    private lateinit var btnCheckout: AppCompatButton
    /** Adapter cho RecyclerView giỏ hàng */
    private lateinit var adapter: CartAdapter

    companion object {
        /** Danh sách toàn cục các món trong giỏ hàng (static) */
        val cartList = mutableListOf<CartItem>()
    }

    /**
     * Khởi tạo màn hình giỏ hàng.
     *
     * Thiết lập layout, ánh xạ view, gán sự kiện cho nút quay lại,
     * nút thanh toán, CheckBox chọn tất cả, và thiết lập RecyclerView
     * với CartAdapter. Cập nhật tổng tiền khi có thay đổi.
     *
     * @param savedInstanceState Trạng thái đã lưu của Activity (nếu có)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        rvCart = findViewById(R.id.rvCart)
        tvTotalItems = findViewById(R.id.tvTotalItems)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        cbSelectAll = findViewById(R.id.cbSelectAll)
        btnCheckout = findViewById(R.id.btnCheckout)
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // CLICK THANH TOÁN SANG ORDER
        btnCheckout.setOnClickListener {
            if (cartList.none { it.isSelected }) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 món", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, order::class.java))
        }

        adapter = CartAdapter(cartList, { updateSummary() }, { position ->
            cartList.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, cartList.size)
            updateSummary()
        })
        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = adapter

        cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            cartList.forEach { it.isSelected = isChecked }
            adapter.notifyDataSetChanged()
            updateSummary()
        }
        updateSummary()
    }

    /**
     * Cập nhật tổng quan giỏ hàng (tổng số lượng, tổng tiền).
     *
     * Lọc các món đã chọn, tính tổng số lượng và tổng tiền,
     * hiển thị theo định dạng tiền tệ Việt Nam, và bật/tắt nút thanh toán.
     */
    private fun updateSummary() {
        val selectedItems = cartList.filter { it.isSelected }
        val totalQty = selectedItems.sumOf { it.qty }
        val totalPrice = selectedItems.sumOf { it.qty * it.price }
        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        tvTotalItems.text = totalQty.toString()
        tvTotalPrice.text = fmt.format(totalPrice) + "đ"
        btnCheckout.isEnabled = selectedItems.isNotEmpty()
        btnCheckout.alpha = if (selectedItems.isNotEmpty()) 1f else 0.5f
    }
}

/**
 * Adapter cho RecyclerView hiển thị danh sách món ăn trong giỏ hàng.
 *
 * @property items Danh sách mutable các CartItem
 * @property onUpdate Callback được gọi khi số lượng hoặc trạng thái chọn thay đổi
 * @property onDelete Callback được gọi khi xóa một món, nhận vào vị trí (position)
 */
class CartAdapter(
    private val items: MutableList<CartItem>,
    private val onUpdate: () -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {
    /**
     * ViewHolder chứa các view cho mỗi item trong giỏ hàng.
     *
     * @param view View gốc của item
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgFood: ImageView = view.findViewById(R.id.imgFood)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvQty: TextView = view.findViewById(R.id.tvQty)
        val btnPlus: TextView = view.findViewById(R.id.btnPlus)
        val btnMinus: TextView = view.findViewById(R.id.btnMinus)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    /**
     * Tạo ViewHolder mới khi RecyclerView cần.
     *
     * @param parent ViewGroup cha
     * @param viewType Loại view (không sử dụng)
     * @return ViewHolder mới được tạo từ layout item_cart
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    /**
     * Gán dữ liệu cho ViewHolder tại vị trí cụ thể.
     *
     * Hiển thị tên, giá, số lượng, hình ảnh, trạng thái chọn.
     * Gán sự kiện click cho nút tăng/giảm số lượng, checkbox chọn, và nút xóa.
     *
     * @param holder ViewHolder cần gán dữ liệu
     * @param position Vị trí của item trong danh sách
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        holder.tvName.text = item.name
        holder.tvPrice.text = fmt.format(item.price) + "đ"
        holder.tvQty.text = item.qty.toString()
        holder.cbSelect.isChecked = item.isSelected
        if (!item.imageUrl.isNullOrEmpty()) Picasso.get().load(item.imageUrl).into(holder.imgFood)
        holder.btnPlus.setOnClickListener { item.qty++; notifyItemChanged(holder.adapterPosition); onUpdate() }
        holder.btnMinus.setOnClickListener { if (item.qty > 1) { item.qty--; notifyItemChanged(holder.adapterPosition); onUpdate() } }
        holder.cbSelect.setOnClickListener { item.isSelected = holder.cbSelect.isChecked; onUpdate() }
        holder.btnDelete.setOnClickListener { onDelete(holder.adapterPosition) }
    }
    /**
     * Trả về tổng số lượng item trong giỏ hàng.
     *
     * @return Số lượng CartItem trong danh sách
     */
    override fun getItemCount() = items.size
}