package com.example.myapp.screens

/**
 * @file activity_cart.kt
 * @brief Activity giỏ hàng (phiên bản Activity độc lập).
 *
 * Phiên bản Activity của màn hình giỏ hàng, sử dụng chung CartAdapter
 * và dữ liệu cart.cartList từ class cart. Hỗ trợ chọn tất cả,
 * xem tổng tiền, và chuyển sang màn hình đặt hàng.
 */

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import java.text.NumberFormat
import java.util.Locale

/**
 * Activity màn hình giỏ hàng (phiên bản độc lập).
 *
 * Chức năng chính:
 * - Hiển thị danh sách giỏ hàng sử dụng cart.cartList
 * - Chọn/bỏ chọn tất cả món
 * - Cập nhật tổng số lượng và tổng tiền
 * - Chuyển sang màn hình đặt hàng (order)
 */
class activity_cart : AppCompatActivity() {
    /** RecyclerView hiển thị danh sách giỏ hàng */
    private lateinit var rvCart: RecyclerView
    /** TextView hiển thị tổng số lượng món đã chọn */
    private lateinit var tvTotalItems: TextView
    /** TextView hiển thị tổng tiền */
    private lateinit var tvTotalPrice: TextView
    /** CheckBox chọn tất cả */
    private lateinit var cbSelectAll: CheckBox
    /** Nút thanh toán */
    private lateinit var btnCheckout: AppCompatButton
    /** Adapter cho RecyclerView giỏ hàng */
    private lateinit var adapter: CartAdapter

    /**
     * Khởi tạo Activity giỏ hàng.
     *
     * Ánh xạ view, thiết lập nút quay lại, nút thanh toán,
     * CartAdapter với dữ liệu từ cart.cartList, CheckBox chọn tất cả,
     * và cập nhật tổng quan.
     *
     * @param savedInstanceState Trạng thái đã lưu của Activity (nếu có)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // Ánh xạ View
        rvCart = findViewById(R.id.rvCart)
        tvTotalItems = findViewById(R.id.tvTotalItems)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        cbSelectAll = findViewById(R.id.cbSelectAll)
        btnCheckout = findViewById(R.id.btnCheckout)
        val btnBack: ImageView = findViewById(R.id.btnBack)

        // Nút quay lại
        btnBack.setOnClickListener { finish() }

        // CHUYỂN SANG MÀN HÌNH ORDER
        btnCheckout.setOnClickListener {
            val intent = Intent(this, order::class.java)
            startActivity(intent)
        }

        // Thiết lập Adapter sử dụng dữ liệu từ class cart
        adapter = CartAdapter(
            cart.cartList,
            onUpdate = { updateSummary() },
            onDelete = { position ->
                cart.cartList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, cart.cartList.size)
                updateSummary()
            }
        )
        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = adapter

        // Chọn tất cả
        cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            cart.cartList.forEach { it.isSelected = isChecked }
            adapter.notifyDataSetChanged()
            updateSummary()
        }

        updateSummary()
    }

    /**
     * Cập nhật tổng quan giỏ hàng.
     *
     * Tính tổng số lượng và tổng tiền từ các món đã chọn,
     * hiển thị theo định dạng tiền tệ Việt Nam.
     * Nút thanh toán luôn được bật (phục vụ mục đích kiểm thử).
     */
    private fun updateSummary() {
        val selectedItems = cart.cartList.filter { it.isSelected }
        val totalQty = selectedItems.sumOf { it.qty }
        val totalPrice = selectedItems.sumOf { it.qty * it.price }

        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        tvTotalItems.text = totalQty.toString()
        tvTotalPrice.text = fmt.format(totalPrice) + "đ"

        // LUÔN BẬT NÚT ĐỂ TEST
        btnCheckout.isEnabled = true
        btnCheckout.alpha = 1.0f
    }
}