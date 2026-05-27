---
name: doc-standard
description: Tiêu chuẩn tài liệu Doxygen cho dự án Kotlin Android (foot-delivery-android). Áp dụng khi viết hoặc review comment KDoc cho các file .kt.
trigger: Khi người dùng yêu cầu viết tài liệu, review tài liệu, hoặc thêm comment cho code Kotlin.
---

# Tiêu Chuẩn Tài Liệu Doxygen — Dự Án Kotlin Android

## 1. Nguyên Tắc Chung

- Ngôn ngữ tài liệu: **tiếng Việt**.
- Sử dụng cú pháp **KDoc** (`/** ... */`) — tương thích Doxygen và Kotlin.
- Tài liệu hóa **tất cả** các thành phần public: class, interface, function, data class, property.
- Với code nội bộ (private/internal), chỉ cần comment ngắn gọn khi logic phức tạp.
- Không tài liệu hóa code hiển nhiên (getter/setter tự sinh, hàm `onCreate` chuẩn).

## 2. Cú Pháp KDoc Cơ Bản

### 2.1. Class / Data Class

```kotlin
/**
 * Đại diện cho một nhà hàng trong hệ thống.
 *
 * Chứa thông tin cơ bản của nhà hàng bao gồm tên, địa chỉ,
 * giờ hoạt động và danh sách món ăn.
 *
 * @property id Định danh duy nhất của nhà hàng.
 * @property name Tên hiển thị của nhà hàng.
 * @property image_url URL hình ảnh đại diện, có thể null nếu chưa cập nhật.
 * @property address Địa chỉ nhà hàng.
 * @property rating Điểm đánh giá trung bình (1-5), null nếu chưa có đánh giá.
 * @property open_time Giờ mở cửa định dạng "HH:mm".
 * @property close_time Giờ đóng cửa định dạng "HH:mm".
 * @property phone_number Số điện thoại liên hệ.
 * @property status Trạng thái hoạt động: "open", "closed", hoặc "temporarily_closed".
 * @property description Mô tả ngắn về nhà hàng, có thể null.
 * @property menu_items Danh sách món ăn thuộc nhà hàng.
 *
 * @since 1.0.0
 * @see MenuItem
 */
data class Restaurant(
    val id: Int,
    val name: String,
    val image_url: String?,
    val address: String?,
    val rating: Int?,
    val open_time: String?,
    val close_time: String?,
    val phone_number: String,
    val status: String,
    val description: String?,
    val menu_items: List<MenuItem>
)
```

### 2.2. Function / Method

```kotlin
/**
 * Xử lý đăng nhập người dùng.
 *
 * Gửi yêu cầu đăng nhập đến server, lưu token vào SharedPreferences
 * khi thành công, và chuyển sang màn hình chính.
 *
 * @param username Tên đăng nhập do người dùng nhập.
 * @param password Mật khẩu do người dùng nhập.
 * @return Unit — không trả về giá trị.
 *
 * @throws IllegalArgumentException nếu username hoặc password rỗng.
 *
 * @see RetrofitClient
 * @see ApiService.login
 */
private fun handleLogin(username: String, password: String) {
    // ...
}
```

### 2.3. Adapter (RecyclerView)

```kotlin
/**
 * Adapter hiển thị danh sách nhà hàng trong RecyclerView.
 *
 * Mỗi item bao gồm hình ảnh, tên, đánh giá và giờ mở cửa.
 * Nhấn vào item sẽ mở màn hình chi tiết nhà hàng.
 *
 * @param context Context hiện tại, dùng để inflate layout và tạo Intent.
 * @param restaurants Danh sách nhà hàng cần hiển thị.
 *
 * @see Restaurant
 * @see restaurant_profile
 */
class RestaurantAdapter(
    private val context: Context,
    private val restaurants: List<Restaurant>
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>()
```

### 2.4. Retrofit API Interface

```kotlin
/**
 * Định nghĩa các endpoint API cho ứng dụng.
 *
 * Tất cả request đều sử dụng Retrofit với Call<T> và enqueue().
 * Token xác thực được tự động đính kèm qua OkHttp Interceptor.
 *
 * @see RetrofitClient
 */
interface ApiService {

    /**
     * Đăng nhập và lấy access token.
     *
     * @param request Thông tin đăng nhập (username, password).
     * @return [LoginResponse] chứa access_token và token_type.
     */
    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    /**
     * Lấy danh sách tất cả nhà hàng.
     *
     * @return Danh sách [Restaurant] từ server.
     */
    @GET("restaurants/")
    fun getRestaurants(): Call<List<Restaurant>>
}
```

## 3. Quy Tắc Định Dạng

### 3.1. Mô Tả (Description)

- **Dòng đầu tiên**: Mô tả ngắn gọn, kết thúc bằng dấu chấm. Tối đa 1 dòng.
- **Phần mở rộng**: Sau dòng trống, mô tả chi tiết hơn nếu cần. Có thể dùng nhiều đoạn.
- Dùng markdown trong mô tả: `**đậm**`, `*nghiêng*`, `` `code` ``, `[liên kết]`.

### 3.2. Các Tag Doxygen Hỗ Trợ

| Tag | Ý nghĩa | Khi nào dùng |
|-----|---------|---------------|
| `@param` | Tham số hàm | Mọi hàm có tham số |
| `@property` | Thuộc tính class | Data class, class có constructor params |
| `@return` | Giá trị trả về | Hàm trả về giá trị khác Unit |
| `@throws` / `@exception` | Ngoại lệ có thể ném | Khi hàm throw exception cụ thể |
| `@see` | Tham chiếu liên quan | Liên kết đến class/function liên quan |
| `@since` | Phiên bản xuất hiện | Khi cần đánh dấu version |
| `@deprecated` | Đánh dấu cũ | Khi code sắp bị thay thế |
| `@sample` | Ví dụ sử dụng | Khi cần minh họa cách dùng |

### 3.3. Ví Dụ Nhanh Cho Từng Loại File

**Activity:**
```kotlin
/**
 * Màn hình giỏ hàng.
 *
 * Hiển thị danh sách món ăn đã chọn, cho phép chỉnh sửa số lượng,
 * xóa món và tiến hành đặt hàng.
 *
 * @see cart
 * @see CartItemAdapter
 */
class cart : AppCompatActivity()
```

**Hàm xử lý sự kiện:**
```kotlin
/**
 * Xử lý khi người dùng nhấn nút "Đặt hàng".
 *
 * Kiểm tra giỏ hàng không rỗng, gọi API tạo đơn hàng,
 * và chuyển sang màn hình theo dõi đơn.
 */
private fun onPlaceOrderClicked() { ... }
```

**Extension function:**
```kotlin
/**
 * Hiển thị thông báo ngắn (Toast) cho người dùng.
 *
 * @param message Nội dung thông báo.
 */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
```

## 4. Quy Tắc Riêng Cho Dự Án Này

### 4.1. Data Class Trong ApiService.kt

- Vì các data class được định nghĩa chung trong `ApiService.kt`, mỗi class **phải** có KDoc đầy đủ với `@property` cho tất cả các trường.
- Ghi rõ ràng kiểu nullable (`String?`) và giá trị mặc định nếu có.

### 4.2. Activity (screens/)

- Mô tả vai trò của màn hình và luồng điều hướng liên quan.
- Dùng `@see` để liên kết sang các màn hình chuyển đến/từ.

### 4.3. Adapter (adapters/)

- Mô tả layout item được inflate và hành vi khi nhấn vào item.
- Liệt kê các View chính trong ViewHolder.

### 4.4. Tên Hàm Đã Có Comment Tiếng Việt

- Giữ nguyên comment tiếng Việt hiện có, chỉ thêm KDoc nếu chưa có.
- Không dịch comment cũ sang tiếng Anh.

## 5. Cấu Hình Doxygen Cho Dự Án

Nếu cần tạo file `Doxyfile`, sử dụng các thiết lập sau:

```doxyfile
PROJECT_NAME           = "Foot Delivery Android"
PROJECT_BRIEF          = "Ứng dụng đặt đồ ăn giao hàng"
OUTPUT_LANGUAGE        = Vietnamese
INPUT                  = app/app/src/main/java/com/example/myapp
FILE_PATTERNS          = *.kt *.java *.xml
EXTENSION_MAPPING      += kt=Java
OPTIMIZE_OUTPUT_JAVA   = YES
RECURSIVE              = YES
EXTRACT_ALL            = YES
GENERATE_HTML          = YES
GENERATE_LATEX         = NO
```

## 6. Checklist Khi Viết Tài Liệu

- [ ] Class/data class có KDoc mô tả + `@property` cho mọi trường?
- [ ] Function có `@param`, `@return` (nếu khác Unit), `@throws` (nếu có)?
- [ ] Dòng mô tả đầu tiên ngắn gọn, kết thúc bằng dấu chấm?
- [ ] Dùng `@see` để liên kết các thành phần liên quan?
- [ ] Tài liệu viết bằng tiếng Việt, dễ hiểu cho sinh viên?
- [ ] Không tài liệu hóa code hiển nhiên?
