package com.example.myapp.screens

/** @file pre_order.kt
 * @brief Màn hình đặt hàng trước cho món ăn.
 *
 * Cho phép người dùng chọn ngày giờ giao hàng trước, điều chỉnh số lượng,
 * và thêm món ăn vào giỏ hàng đặt trước.
 */

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.myapp.R
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.*

/**
 * Màn hình đặt hàng trước cho một món ăn cụ thể.
 *
 * Cho phép người dùng:
 * - Xem thông tin món ăn (tên, giá, hình ảnh).
 * - Chọn ngày và giờ giao hàng (tối thiểu 30 phút sau thời điểm hiện tại).
 * - Điều chỉnh số lượng đặt hàng.
 * - Thêm món ăn vào giỏ hàng đặt trước.
 */
class pre_order : AppCompatActivity() {
    /** Số lượng món ăn cần đặt, mặc định là 1 */
    private var qty = 1
    /** Đơn giá của món ăn */
    private var price = 0
    /** ID của món ăn */
    private var foodId = -1
    /** Tên của món ăn */
    private var foodName = ""
    /** URL hình ảnh của món ăn */
    private var foodImageUrl = ""

    /** Lịch chứa ngày giờ giao hàng đã chọn */
    private var selectedCalendar: Calendar = Calendar.getInstance()
    /** Trạng thái đã chọn ngày giao hàng */
    private var isDateSelected = false
    /** Trạng thái đã chọn giờ giao hàng */
    private var isTimeSelected = false

    /**
     * Khởi tạo màn hình đặt hàng trước.
     *
     * Nhận thông tin món ăn từ Intent, thiết lập giao diện,
     * và đăng ký các sự kiện chọn ngày, giờ, số lượng và thêm vào giỏ.
     *
     * @param savedInstanceState Trạng thái đã lưu trước đó (nếu có).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_order)

        foodId = intent.getIntExtra("food_id", -1)
        foodName = intent.getStringExtra("food_name") ?: ""
        price = intent.getIntExtra("food_price", 0)
        foodImageUrl = intent.getStringExtra("food_image_url") ?: ""

        val tvFoodName: TextView = findViewById(R.id.tvFoodName)
        val tvUnitPrice: TextView = findViewById(R.id.tvUnitPrice)
        val tvQty: TextView = findViewById(R.id.tvQty)
        val tvTotalValue: TextView = findViewById(R.id.tvTotalValue)
        val imgFood: ImageView = findViewById(R.id.imgFood)

        val btnSelectDate: TextView = findViewById(R.id.btnSelectDate)
        val btnSelectTime: TextView = findViewById(R.id.btnSelectTime)
        val tvSelectedDateTime: TextView = findViewById(R.id.tvSelectedDateTime)

        val btnDecrease: TextView = findViewById(R.id.btnDecrease)
        val btnIncrease: TextView = findViewById(R.id.btnIncrease)
        val btnAddToCart: AppCompatButton = findViewById(R.id.btnPreOrderAddToCart)
        val btnBack: ImageView = findViewById(R.id.btnBack)

        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))

        tvFoodName.text = foodName
        tvUnitPrice.text = fmt.format(price) + "đ"

        if (foodImageUrl.isNotEmpty()) {
            Picasso.get().load(foodImageUrl).placeholder(R.drawable.placeholder_loading).error(R.drawable.pngwing).into(imgFood)
        }

        fun updateUI() {
            tvQty.text = qty.toString()
            tvTotalValue.text = fmt.format(qty * price) + "đ"

            if (isDateSelected && isTimeSelected) {
                val day = selectedCalendar.get(Calendar.DAY_OF_MONTH)
                val month = selectedCalendar.get(Calendar.MONTH) + 1
                val year = selectedCalendar.get(Calendar.YEAR)
                val hour = selectedCalendar.get(Calendar.HOUR_OF_DAY)
                val minute = selectedCalendar.get(Calendar.MINUTE)

                val timeStr = String.format("%02d:%02d", hour, minute)
                val dateStr = "$day/$month/$year"

                tvSelectedDateTime.text = "Giao lúc: $timeStr, $dateStr"
                tvSelectedDateTime.setTextColor(resources.getColor(android.R.color.black))
            } else {
                tvSelectedDateTime.text = "Chưa chọn thời điểm giao"
                tvSelectedDateTime.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
        }

        btnDecrease.setOnClickListener { if (qty > 1) { qty--; updateUI() } }
        btnIncrease.setOnClickListener { qty++; updateUI() }

        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                isDateSelected = true
                isTimeSelected = false // Reset time when date changes to force re-validation
                updateUI()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePickerDialog.datePicker.minDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        btnSelectTime.setOnClickListener {
            if (!isDateSelected) {
                Toast.makeText(this, "Vui lòng chọn ngày trước", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val tempCalendar = selectedCalendar.clone() as Calendar
                tempCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                tempCalendar.set(Calendar.MINUTE, minute)

                val now = Calendar.getInstance()
                val minTime = Calendar.getInstance()
                minTime.add(Calendar.MINUTE, 30)

                if (tempCalendar.before(minTime)) {
                    Toast.makeText(this, "Thời gian giao phải ít nhất sau 30 phút kể từ bây giờ", Toast.LENGTH_LONG).show()
                } else {
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedCalendar.set(Calendar.MINUTE, minute)
                    isTimeSelected = true
                    updateUI()
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        btnAddToCart.setOnClickListener {
            if (!isDateSelected || !isTimeSelected) {
                Toast.makeText(this, "Chưa chọn thời điểm giao", Toast.LENGTH_SHORT).show()
            } else {
                addToPreOrderCart()
            }
        }

        btnBack.setOnClickListener { finish() }
        updateUI()
    }

    /**
     * Thêm món ăn vào giỏ hàng đặt trước.
     *
     * Tạo đối tượng PreOrderItem với thông tin món ăn, số lượng và thời gian giao,
     * sau đó chuyển sang màn hình giỏ hàng đặt trước.
     */
    private fun addToPreOrderCart() {
        val day = selectedCalendar.get(Calendar.DAY_OF_MONTH)
        val month = selectedCalendar.get(Calendar.MONTH) + 1
        val year = selectedCalendar.get(Calendar.YEAR)
        val hour = selectedCalendar.get(Calendar.HOUR_OF_DAY)
        val minute = selectedCalendar.get(Calendar.MINUTE)

        val timeStr = String.format("%02d:%02d", hour, minute)
        val dateStr = "$day/$month/$year"

        pre_order_cart.preOrderList.add(
            PreOrderItem(
                id = foodId,
                name = foodName,
                price = price,
                qty = qty,
                imageUrl = foodImageUrl,
                deliveryTime = "$timeStr, $dateStr"
            )
        )
        Toast.makeText(this, "Đã thêm vào giỏ hàng đặt trước", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, pre_order_cart::class.java)
        startActivity(intent)
        finish()
    }
}
