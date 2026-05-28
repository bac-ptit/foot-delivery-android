package com.example.myapp.screens

/**
 * @file order.kt
 * @brief Màn hình đặt hàng (Checkout) của ứng dụng giao đồ ăn.
 *
 * Hiển thị thông tin giỏ hàng, địa chỉ giao hàng, voucher giảm giá,
 * phương thức thanh toán. Cho phép người dùng xác nhận đặt hàng.
 * Hỗ trợ đặt lại đơn hàng (reorder) từ lịch sử.
 */

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.View
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.example.myapp.screens.api.Address
import com.example.myapp.screens.api.MenuItem
import com.example.myapp.screens.api.OrderCreateRequest
import com.example.myapp.screens.api.OrderItemRequest
import com.example.myapp.screens.api.OrderResponse
import com.example.myapp.screens.api.RetrofitClient
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

/**
 * Activity màn hình đặt hàng (Checkout).
 *
 * Chức năng chính:
 * - Hiển thị danh sách món ăn từ giỏ hàng hoặc từ đơn hàng cũ (reorder)
 * - Hiển thị và chọn địa chỉ giao hàng
 * - Chọn và áp dụng voucher/mã giảm giá
 * - Chọn phương thức thanh toán (COD/Online)
 * - Tạo đơn hàng mới qua API
 * - Xử lý thanh toán online hoặc COD
 */
class order : AppCompatActivity() {
    /** Địa chỉ giao hàng hiện tại của người dùng */
    private var currentAddress: Address? = null
    /** ID nhà hàng của đơn hàng */
    private var restaurantId: Int = 1
    /** Danh sách các món ăn đã chọn để đặt hàng */
    private val selectedItems = mutableListOf<CartItem>()
    /** ID đơn hàng gốc khi thực hiện đặt lại (reorder), null nếu đặt mới */
    private var reorderSourceOrderId: Int? = null
    /** Tổng tiền cơ bản (trước khi áp dụng giảm giá) */
    private var baseTotalPrice: Int = 0
    /** Mã giảm giá đã chọn */
    private var selectedPromotionCode: String? = null
    /** Loại giảm giá: "percent" (phần trăm) hoặc "fixed" (cố định) */
    private var selectedDiscountType: String? = null
    /** Giá trị giảm giá */
    private var selectedDiscountValue: Int = 0
    /** Phương thức thanh toán: "cod" hoặc "online" */
    private var selectedPaymentMethod: String = "cod"
    /** Nhãn hiển thị của phương thức thanh toán */
    private var selectedPaymentMethodLabel: String = "Thanh toán khi nhận hàng"

    /** Launcher nhận kết quả chọn voucher từ màn hình giảm giá */
    private val voucherPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK || result.data == null) return@registerForActivityResult

        selectedPromotionCode = result.data?.getStringExtra("promotion_code")
        selectedDiscountType = result.data?.getStringExtra("discount_type")
        selectedDiscountValue = result.data?.getIntExtra("discount_value", 0) ?: 0
        bindVoucherAndTotal()
    }

    /** Launcher nhận kết quả chọn phương thức thanh toán */
    private val paymentMethodPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK || result.data == null) return@registerForActivityResult

        selectedPaymentMethod = result.data?.getStringExtra("payment_method") ?: selectedPaymentMethod
        selectedPaymentMethodLabel = result.data?.getStringExtra("payment_method_label") ?: selectedPaymentMethodLabel
        bindPaymentMethod()
    }

    /**
     * Khởi tạo màn hình đặt hàng.
     *
     * Xử lý 2 trường hợp: đặt lại đơn hàng (reorder) hoặc đặt hàng mới từ giỏ.
     * Thiết lập sự kiện click cho voucher, phương thức thanh toán, địa chỉ,
     * nút đặt hàng và các nút điều hướng.
     *
     * @param savedInstanceState Trạng thái đã lưu của Activity (nếu có)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order)

        reorderSourceOrderId = intent.getIntExtra("reorder_order_id", -1).takeIf { it > 0 }

        if (reorderSourceOrderId != null) {
            bindCartPreview()
            bindPaymentMethod()
            loadReorderSourceOrder(reorderSourceOrderId!!)
        } else {
            selectedItems.clear()
            
            // THÊM: Kiểm tra xem có nhận danh sách món ăn từ Intent (ví dụ từ Đặt trước) không
            val passedItems = intent.getSerializableExtra("selected_items") as? ArrayList<CartItem>
            if (passedItems != null) {
                selectedItems.addAll(passedItems)
            } else {
                // Nếu không có thì mới lấy từ giỏ hàng thường
                selectedItems.addAll(cart.cartList.filter { it.isSelected })
                if (selectedItems.isEmpty()) {
                    selectedItems.addAll(cart.cartList)
                }
            }

            bindCartPreview()
            bindPaymentMethod()
            resolveRestaurantId()
        }
        loadUserAddress()

        val cardVoucher = findViewById<View>(R.id.cardVoucher)
        val cardPaymentMethod = findViewById<View>(R.id.cardPaymentMethod)
        val lblAddress = findViewById<TextView>(R.id.lblAddress)
        val valAddress = findViewById<TextView>(R.id.valAddress)
        val btnCancel = findViewById<View>(R.id.btnCancel)
        val btnOrder = findViewById<View>(R.id.btnOrder)
        val btnBack = findViewById<ImageView>(R.id.icBack)
        val icHome = findViewById<ImageView>(R.id.icHome)
        val icProfile = findViewById<ImageView>(R.id.icProfile)
        val icCart = findViewById<ImageView>(R.id.icCart)

        cardVoucher.setOnClickListener {
            voucherPickerLauncher.launch(Intent(this, discouts::class.java))
        }

        cardPaymentMethod.setOnClickListener {
            val paymentIntent = Intent(this, payment_methods::class.java)
            val discountAmount = calculateDiscountAmount(baseTotalPrice)
            val payableTotal = (baseTotalPrice - discountAmount).coerceAtLeast(0)
            paymentIntent.putExtra("order_total", payableTotal)
            paymentIntent.putExtra("select_mode", true)
            paymentIntent.putExtra("selected_payment_method", selectedPaymentMethod)
            paymentMethodPickerLauncher.launch(paymentIntent)
        }

        lblAddress.setOnClickListener {
            startActivity(Intent(this, savedeliveryaddress::class.java))
        }

        valAddress.setOnClickListener {
            startActivity(Intent(this, savedeliveryaddress::class.java))
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnOrder.setOnClickListener {
            createOrderFromCart()
        }

        btnBack.setOnClickListener { finish() }
        icHome.setOnClickListener { startActivity(Intent(this, home::class.java)) }
        icProfile.setOnClickListener { startActivity(Intent(this, profile::class.java)) }
        icCart.setOnClickListener { startActivity(Intent(this, cart::class.java)) }

    }

    /**
     * Hiển thị danh sách món ăn trong phần xem trước giỏ hàng.
     *
     * Tạo view động cho từng món ăn với hình ảnh, tên, số lượng,
     * đơn giá và thành tiền. Tính tổng tiền cơ bản và cập nhật voucher.
     */
    private fun bindCartPreview() {
        val tvTotal = findViewById<TextView>(R.id.valTotal)
        val tvRestaurantName = findViewById<TextView>(R.id.tvRestaurantName)
        val tvAddress = findViewById<TextView>(R.id.valAddress)
        val cartItemsContainer = findViewById<android.widget.LinearLayout>(R.id.cartItemsContainer)

        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        baseTotalPrice = selectedItems.sumOf { it.qty * it.price }

        cartItemsContainer.removeAllViews()

        if (selectedItems.isEmpty()) {
            val emptyView = android.widget.TextView(this).apply {
                text = "Giỏ hàng trống"
                textSize = 16f
                setTextColor(android.graphics.Color.GRAY)
                gravity = android.view.Gravity.CENTER
                setPadding(16, 32, 16, 32)
            }
            cartItemsContainer.addView(emptyView)
            tvTotal.text = "0đ"
            tvRestaurantName.text = "Giỏ hàng"
            tvAddress.text = "Chưa có địa chỉ"
            bindVoucherAndTotal()
            return
        }

        // Display ALL items
        for (item in selectedItems) {
            val itemView = android.view.LayoutInflater.from(this)
                .inflate(R.layout.order_cart_item, null, false)
            
            val imgCartItem = itemView.findViewById<ImageView>(R.id.imgCartItem)
            val tvCartItemName = itemView.findViewById<TextView>(R.id.tvCartItemName)
            val tvCartItemQty = itemView.findViewById<TextView>(R.id.tvCartItemQty)
            val tvCartItemUnitPrice = itemView.findViewById<TextView>(R.id.tvCartItemUnitPrice)
            val tvCartItemPrice = itemView.findViewById<TextView>(R.id.tvCartItemPrice)

            tvCartItemName.text = item.name
            tvCartItemQty.text = "Số lượng: ${item.qty}"
            tvCartItemUnitPrice.text = "Đơn giá: ${fmt.format(item.price)}đ"
            tvCartItemPrice.text = "Thành tiền: ${fmt.format(item.qty * item.price)}đ"

            if (!item.imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(item.imageUrl)
                    .placeholder(R.drawable.placeholder_loading)
                    .error(R.drawable.pngwing)
                    .into(imgCartItem)
            } else {
                imgCartItem.setImageResource(R.drawable.pngwing)
            }

            itemView.layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 12
            }

            cartItemsContainer.addView(itemView)
        }

        tvRestaurantName.text = "Quán hàng"
        tvAddress.text = "Đang tải..."
        tvTotal.text = "${fmt.format(baseTotalPrice)}đ"
        bindVoucherAndTotal()
    }

    /**
     * Cập nhật hiển thị voucher và tổng tiền thanh toán.
     *
     * Tính toán số tiền giảm giá, hiển thị mã voucher và tổng tiền
     * sau khi trừ giảm giá (không âm).
     */
    private fun bindVoucherAndTotal() {
        val voucherValueText = findViewById<TextView>(R.id.tvVoucherValue)
        val tvTotal = findViewById<TextView>(R.id.valTotal)
        val discountAmount = calculateDiscountAmount(baseTotalPrice)
        val payableTotal = (baseTotalPrice - discountAmount).coerceAtLeast(0)
        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))

        voucherValueText.text = if (!selectedPromotionCode.isNullOrBlank() && selectedDiscountValue > 0) {
            "$selectedPromotionCode (-${fmt.format(discountAmount)}đ)"
        } else {
            "Chọn >"
        }

        tvTotal.text = "${fmt.format(payableTotal)}đ"
    }

    /**
     * Cập nhật hiển thị phương thức thanh toán đã chọn.
     */
    private fun bindPaymentMethod() {
        findViewById<TextView>(R.id.tvPaymentMethodValue).text = selectedPaymentMethodLabel
    }

    /**
     * Tính số tiền giảm giá dựa trên loại và giá trị giảm giá.
     *
     * @param total Tổng tiền gốc trước khi giảm
     * @return Số tiền được giảm (VNĐ). Trả về 0 nếu không có giảm giá.
     */
    private fun calculateDiscountAmount(total: Int): Int {
        if (selectedDiscountValue <= 0) return 0
        return if ((selectedDiscountType ?: "").equals("percent", ignoreCase = true)) {
            ((total * selectedDiscountValue) / 100.0).toInt()
        } else {
            selectedDiscountValue
        }
    }

    /**
     * Tải địa chỉ giao hàng của người dùng từ API.
     *
     * Lấy userId từ SharedPreferences, gọi API getUserAddresses.
     * Hiển thị địa chỉ đầu tiên hoặc thông báo nếu chưa có địa chỉ.
     */
    private fun loadUserAddress() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        if (userId <= 0) {
            findViewById<TextView>(R.id.valAddress).text = "Chưa đăng nhập"
            return
        }

        RetrofitClient.apiService.getUserAddresses(userId).enqueue(object : Callback<List<Address>> {
            override fun onResponse(call: Call<List<Address>>, response: Response<List<Address>>) {
                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    findViewById<TextView>(R.id.valAddress).text = "Chưa có địa chỉ"
                    return
                }
                currentAddress = response.body()!!.first()
                findViewById<TextView>(R.id.valAddress).text = currentAddress!!.detail
            }

            override fun onFailure(call: Call<List<Address>>, t: Throwable) {
                findViewById<TextView>(R.id.valAddress).text = "Không tải được địa chỉ"
            }
        })
    }

    /**
     * Xác định ID nhà hàng từ danh sách món ăn đã chọn.
     *
     * Gọi API getAllMenuItems, tìm món ăn đầu tiên khớp với selectedItems
     * để lấy restaurantId và tên nhà hàng.
     */
    private fun resolveRestaurantId() {
        if (reorderSourceOrderId != null) return
        val menuIds = selectedItems.map { it.id }.toSet()
        if (menuIds.isEmpty()) return

        RetrofitClient.apiService.getAllMenuItems().enqueue(object : Callback<List<MenuItem>> {
            override fun onResponse(call: Call<List<MenuItem>>, response: Response<List<MenuItem>>) {
                if (!response.isSuccessful || response.body().isNullOrEmpty()) return
                val firstMatched = response.body()!!.firstOrNull { it.id in menuIds }
                if (firstMatched != null) {
                    restaurantId = firstMatched.restaurantid
                    findViewById<TextView>(R.id.tvRestaurantName).text = firstMatched.restaurant_name ?: "Nhà hàng #${firstMatched.restaurantid}"
                }
            }

            override fun onFailure(call: Call<List<MenuItem>>, t: Throwable) {
            }
        })
    }

    /**
     * Tải thông tin đơn hàng gốc để đặt lại (reorder).
     *
     * Gọi API getOrderDetail, chuyển đổi OrderItem thành CartItem,
     * cập nhật restaurantId và hiển thị lại giỏ hàng.
     *
     * @param orderId ID của đơn hàng cần đặt lại
     */
    private fun loadReorderSourceOrder(orderId: Int) {
        RetrofitClient.apiService.getOrderDetail(orderId).enqueue(object : Callback<com.example.myapp.screens.api.OrderDetailResponse> {
            override fun onResponse(call: Call<com.example.myapp.screens.api.OrderDetailResponse>, response: Response<com.example.myapp.screens.api.OrderDetailResponse>) {
                if (!response.isSuccessful || response.body() == null) {
                    Toast.makeText(this@order, "Không tải được đơn để đặt lại", Toast.LENGTH_SHORT).show()
                    return
                }

                val sourceOrder = response.body()!!
                selectedItems.clear()
                selectedItems.addAll(
                    sourceOrder.order_items.map { item ->
                        CartItem(
                            id = item.menuitemid,
                            name = item.menuitem_name ?: "Món #${item.menuitemid}",
                            price = item.price,
                            qty = item.quantity,
                            imageUrl = item.image_url
                        )
                    }
                )
                restaurantId = sourceOrder.restaurantid
                findViewById<TextView>(R.id.tvRestaurantName).text = sourceOrder.restaurant_name ?: "Nhà hàng #${sourceOrder.restaurantid}"
                bindCartPreview()
            }

            override fun onFailure(call: Call<com.example.myapp.screens.api.OrderDetailResponse>, t: Throwable) {
                Toast.makeText(this@order, "Lỗi mạng khi tải đơn đặt lại", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Tạo đơn hàng mới từ giỏ hàng hiện tại.
     *
     * Xác thực: kiểm tra đăng nhập, địa chỉ giao hàng, giỏ hàng không trống.
     * Tạo OrderCreateRequest và gọi API createOrder.
     * Sau khi thành công: xóa món đã đặt khỏi giỏ, chuyển sang thanh toán
     * online hoặc trang thanh toán thành công (COD).
     */
    private fun createOrderFromCart() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId <= 0) {
            Toast.makeText(this, "Bạn cần đăng nhập lại", Toast.LENGTH_SHORT).show()
            return
        }

        val address = currentAddress
        if (address == null) {
            Toast.makeText(this, "Vui lòng thêm địa chỉ giao hàng", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, savedeliveryaddress::class.java))
            return
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show()
            return
        }

        val totalPrice = selectedItems.sumOf { it.qty * it.price }
        val orderItems = selectedItems.map {
            OrderItemRequest(quantity = it.qty, price = it.price, menuitemid = it.id)
        }

        val request = OrderCreateRequest(
            status = "pending",
            totalprice = totalPrice,
            restaurantid = restaurantId,
            addressid = address.id,
            userid = userId,
            order_items = orderItems
        )

        RetrofitClient.apiService.createOrder(request).enqueue(object : Callback<OrderResponse> {
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                if (!response.isSuccessful || response.body() == null) {
                    Toast.makeText(this@order, "Không thể tạo đơn hàng", Toast.LENGTH_SHORT).show()
                    return
                }

                val createdOrder = response.body()!!
                cart.cartList.removeAll(selectedItems.toSet())
                selectedItems.clear()
                val discountAmount = calculateDiscountAmount(totalPrice)
                val payableTotal = (totalPrice - discountAmount).coerceAtLeast(0)

                if (selectedPaymentMethod.equals("online", ignoreCase = true)) {
                    val intent = Intent(this@order, payment_methods::class.java)
                    intent.putExtra("order_id", createdOrder.id)
                    intent.putExtra("order_total", payableTotal)
                    intent.putExtra("select_mode", false)
                    intent.putExtra("auto_pay_now", true)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@order, payment_successful::class.java)
                    intent.putExtra("order_id", createdOrder.id)
                    startActivity(intent)
                }
            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                Toast.makeText(this@order, "Lỗi mạng khi tạo đơn", Toast.LENGTH_SHORT).show()
            }
        })

    }
}