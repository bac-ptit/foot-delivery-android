# CLAUDE Rules — Kotlin Android Development

You are an expert Android developer. Follow these rules strictly for all code generation, review, and suggestions.

---

## 1. Tech Stack

- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Java Version**: 17
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`)
- **Networking**: Retrofit 2.9.0 + Gson 2.10.1
- **Image Loading**: Picasso 2.8 (primary), Glide 4.16.0 (secondary)
- **UI**: Traditional View system with XML layouts (no Jetpack Compose)
- **State**: SharedPreferences (no ViewModel, no LiveData)
- **Navigation**: Explicit Intent (no Navigation Component)
- **Push Notifications**: Firebase Cloud Messaging (FCM)

---

## 2. Architecture

Dự án sử dụng kiến trúc **Activity-based** đơn giản, không dùng MVVM/MVP.

```
┌─────────────────────────────────────────────┐
│              screens/                        │
│  (Activity = Screen, xử lý UI + logic)      │
├─────────────────────────────────────────────┤
│              adapters/                       │
│  (RecyclerView.Adapter, ViewHolder)          │
├─────────────────────────────────────────────┤
│              api/                            │
│  (Retrofit interface, data class, config)    │
├─────────────────────────────────────────────┤
│              res/                            │
│  (layouts, drawables, values)                │
└─────────────────────────────────────────────┘
```

### Quy tắc phụ thuộc
- `screens/` có thể import từ `adapters/` và `api/`
- `adapters/` có thể import từ `api/` (data class)
- `api/` không import từ `screens/` hay `adapters/`

---

## 3. Project Structure

```
com.example.myapp/
├── adapters/                    # RecyclerView adapters
│   ├── AddressAdapter.kt
│   ├── ChatMessageAdapter.kt
│   ├── FAQAdapter.kt
│   ├── MenuItemAdapter.kt
│   ├── MenuItemAdapterSmall.kt
│   ├── MyFirebaseMessagingService.kt
│   ├── NotificationAdapter.kt
│   └── RestaurantAdapter.kt
│
├── api/                         # Networking layer
│   ├── ApiService.kt            # Retrofit interface + ALL data classes
│   ├── FcmTokenRegistrar.kt     # FCM token sync
│   ├── NetworkConfig.kt         # Base URL (emulator vs device)
│   └── RetrofitClient.kt        # Singleton Retrofit + auth interceptor
│
└── screens/                     # Activity screens
    ├── start.kt                 # LAUNCHER
    ├── signin.kt
    ├── signup.kt
    ├── home.kt
    ├── profile.kt
    ├── cart.kt
    ├── order.kt
    ├── food_detail.kt
    ├── list_restaurant.kt
    ├── restaurant_profile.kt
    ├── notification.kt
    ├── chatbot.kt
    ├── customer_support.kt
    ├── order_history.kt
    ├── OrderTracking.kt
    ├── OrderTrackingDetail.kt
    ├── VNPayActivity.kt
    ├── PointsDetail.kt
    ├── discouts.kt
    └── ...
```

---

## 4. Naming Conventions

| Kind | Convention | Example |
|---|---|---|
| Activity classes | `snake_case` (hiện tại) | `home`, `cart`, `signin` |
| Adapter classes | `PascalCase` | `RestaurantAdapter` |
| Data classes | `PascalCase` | `LoginRequest`, `MenuItem` |
| Functions | `camelCase` | `handleLogin`, `loadRestaurants` |
| Variables | `camelCase` | `restaurantList`, `accessToken` |
| Constants | `UPPER_SNAKE_CASE` | `RESTAURANT_ID`, `PREF_TOKEN` |
| Layout files | `snake_case` | `activity_home.xml`, `item_restaurant.xml` |
| Drawable files | `snake_case` | `placeholder_loading.png` |
| IDs (XML) | `camelCase` with prefix | `tvName`, `imgFood`, `btnSubmit` |

### Prefix cho View IDs
| Prefix | View type | Ví dụ |
|--------|-----------|-------|
| `tv` | TextView | `tvRestaurantName` |
| `img` / `iv` | ImageView | `imgFood` |
| `btn` | Button | `btnPlaceOrder` |
| `et` | EditText | `etSearch` |
| `rv` | RecyclerView | `rvMenuItems` |
| `cb` | CheckBox | `cbRemember` |
| `fl` | FrameLayout | `flContainer` |

---

## 5. Activity Pattern

Mỗi Activity là một màn hình hoàn chỉnh, tự xử lý UI và logic.

```kotlin
/**
 * Màn hình danh sách nhà hàng.
 *
 * Hiển thị danh sách nhà hàng với tìm kiếm và lọc.
 * Nhấn vào nhà hàng sẽ mở màn hình chi tiết.
 *
 * @see RestaurantAdapter
 * @see restaurant_profile
 */
class list_restaurant : AppCompatActivity() {

    private lateinit var rvRestaurants: RecyclerView
    private lateinit var adapter: RestaurantAdapter
    private var restaurantList: List<Restaurant> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_restaurant)

        initViews()
        setupRecyclerView()
        loadRestaurants()
    }

    private fun initViews() {
        rvRestaurants = findViewById(R.id.rvRestaurants)
    }

    private fun setupRecyclerView() {
        adapter = RestaurantAdapter(this, restaurantList)
        rvRestaurants.layoutManager = LinearLayoutManager(this)
        rvRestaurants.adapter = adapter
    }

    private fun loadRestaurants() {
        val api = RetrofitClient.instance
        api.getRestaurants().enqueue(object : Callback<List<Restaurant>> {
            override fun onResponse(
                call: Call<List<Restaurant>>,
                response: Response<List<Restaurant>>
            ) {
                if (response.isSuccessful) {
                    restaurantList = response.body() ?: emptyList()
                    adapter = RestaurantAdapter(this@list_restaurant, restaurantList)
                    rvRestaurants.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<Restaurant>>, t: Throwable) {
                Toast.makeText(
                    this@list_restaurant,
                    "Lỗi kết nối: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
```

### Quy tắc Activity
- ✅ Dùng `findViewById` (không dùng ViewBinding trừ khi được yêu cầu)
- ✅ Gọi API bằng Retrofit `enqueue()` (không dùng coroutine)
- ✅ Lưu auth token vào SharedPreferences
- ✅ Chuyển màn hình bằng `Intent` + `putExtra()`
- ❌ Không dùng ViewModel, LiveData, Flow
- ❌ Không dùng Jetpack Compose
- ❌ Không dùng DataBinding

---

## 6. Retrofit API Pattern

Tất cả data class và API endpoints định nghĩa trong `ApiService.kt`.

```kotlin
// ── Data Classes ──────────────────────────────────────────────

/**
 * Yêu cầu đăng nhập.
 *
 * @property username Tên đăng nhập.
 * @property password Mật khẩu.
 */
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * Phản hồi đăng nhập từ server.
 *
 * @property access_token JWT token xác thực.
 * @property token_type Loại token (luôn là "bearer").
 */
data class LoginResponse(
    val access_token: String,
    val token_type: String
)

// ── API Interface ─────────────────────────────────────────────

/**
 * Các endpoint API cho ứng dụng.
 *
 * Token xác thực được tự động đính kèm qua OkHttp Interceptor
 * trong [RetrofitClient].
 */
interface ApiService {

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("restaurants/")
    fun getRestaurants(): Call<List<Restaurant>>

    @GET("restaurants/{id}")
    fun getRestaurant(@Path("id") id: Int): Call<Restaurant>

    @FormUrlEncoded
    @POST("orders/")
    fun createOrder(
        @Field("restaurant_id") restaurantId: Int,
        @Field("items") items: String
    ): Call<OrderResponse>
}
```

### Quy tắc API
- ✅ Data class đặt chung trong `ApiService.kt`
- ✅ Field names dùng `snake_case` (match backend JSON)
- ✅ Nullable fields dùng `?` (e.g., `val image_url: String?`)
- ✅ Giá trị mặc định nếu cần (e.g., `val reviews: List<ReviewDetail>? = null`)
- ✅ Luôn dùng `enqueue()` với `Callback<T>` (không dùng `execute()`)
- ❌ Không tách data class ra file riêng
- ❌ Không dùng coroutines hay `suspend fun`

---

## 7. RetrofitClient Pattern

Singleton với OkHttp Interceptor tự động đính kèm JWT token.

```kotlin
/**
 * Singleton Retrofit client với xác thực tự động.
 *
 * Đọc JWT token từ SharedPreferences và đính kèm
 * vào mọi request qua Authorization header.
 */
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val context = MyApplication.instance.applicationContext
            val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val token = prefs.getString("access_token", null)

            val request = chain.request().newBuilder().apply {
                token?.let {
                    addHeader("Authorization", "Bearer $it")
                }
            }.build()

            chain.proceed(request)
        }
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

---

## 8. SharedPreferences Pattern

Lưu trữ auth state và user info đơn giản.

```kotlin
// ── Lưu token sau đăng nhập ──────────────────────────────────
val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
prefs.edit()
    .putString("access_token", response.access_token)
    .putString("user_id", user.id.toString())
    .putString("user_name", user.name)
    .apply()

// ── Đọc token trong Interceptor ──────────────────────────────
val token = prefs.getString("access_token", null)

// ── Xóa khi đăng xuất ────────────────────────────────────────
prefs.edit().clear().apply()
```

### Keys chuẩn
| Key | Kiểu | Giá trị |
|-----|------|---------|
| `access_token` | String | JWT token |
| `user_id` | String | ID người dùng |
| `user_name` | String | Tên hiển thị |
| `fcm_token` | String | Firebase Cloud Messaging token |

---

## 9. RecyclerView Adapter Pattern

```kotlin
/**
 * Adapter hiển thị danh sách nhà hàng.
 *
 * @param context Context hiện tại.
 * @param restaurants Danh sách nhà hàng.
 * @see Restaurant
 * @see restaurant_profile
 */
class RestaurantAdapter(
    private val context: Context,
    private val restaurants: List<Restaurant>
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFood: ImageView = itemView.findViewById(R.id.imgFood)
        val tvRestaurantName: TextView = itemView.findViewById(R.id.tvRestaurantName)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val tvOpeningHours: TextView = itemView.findViewById(R.id.tvOpeningHours)
        val foodTitleCard: View = itemView.findViewById(R.id.foodTitleCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]

        // Load image
        if (!restaurant.image_url.isNullOrEmpty()) {
            Picasso.get()
                .load(restaurant.image_url)
                .placeholder(R.drawable.placeholder_loading)
                .error(R.drawable.pngwing)
                .into(holder.imgFood)
        }

        holder.tvRestaurantName.text = restaurant.name
        holder.tvRating.text = restaurant.rating?.toString() ?: "N/A"

        // Click → navigate
        holder.foodTitleCard.setOnClickListener {
            val intent = Intent(context, restaurant_profile::class.java)
            intent.putExtra("RESTAURANT_ID", restaurant.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = restaurants.size
}
```

### Quy tắc Adapter
- ✅ ViewHolder class đặt bên trong Adapter
- ✅ Dùng `Picasso.get().load()` cho hình ảnh từ URL
- ✅ Dùng `.placeholder()` và `.error()` cho fallback
- ✅ Set click listener trong `onBindViewHolder`
- ✅ Truyền data qua `Intent.putExtra()`
- ❌ Không dùng DiffUtil (danh sách nhỏ, reload toàn bộ)

---

## 10. Error Handling

```kotlin
// ── Retrofit callback ─────────────────────────────────────────
api.getRestaurants().enqueue(object : Callback<List<Restaurant>> {
    override fun onResponse(
        call: Call<List<Restaurant>>,
        response: Response<List<Restaurant>>
    ) {
        if (response.isSuccessful) {
            // Xử lý thành công
            val data = response.body() ?: emptyList()
            updateUI(data)
        } else {
            // Xử lý lỗi HTTP
            val errorMsg = when (response.code()) {
                401 -> "Phiên đăng nhập hết hạn"
                403 -> "Không có quyền truy cập"
                404 -> "Không tìm thấy dữ liệu"
                500 -> "Lỗi server"
                else -> "Lỗi: ${response.code()}"
            }
            Toast.makeText(this@Activity, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onFailure(call: Call<List<Restaurant>>, t: Throwable) {
        // Lỗi mạng
        Toast.makeText(
            this@Activity,
            "Lỗi kết nối: ${t.message}",
            Toast.LENGTH_SHORT
        ).show()
    }
})
```

### Quy tắc Error Handling
- ✅ Luôn kiểm tra `response.isSuccessful` trước khi dùng `response.body()`
- ✅ Hiển thị lỗi cho người dùng bằng `Toast` hoặc `AlertDialog`
- ✅ Xử lý cả `onResponse` (HTTP error) và `onFailure` (network error)
- ✅ Log lỗi cho debug: `Log.e("TAG", "Error: ${t.message}")`
- ❌ Không dùng `try-catch` bao quanh Retrofit callback
- ❌ Không nuốt lỗi (luôn thông báo cho người dùng)

---

## 11. XML Layout Rules

### Naming
- Activity layout: `activity_{name}.xml` → `activity_home.xml`
- Item layout: `item_{type}.xml` → `item_restaurant.xml`
- Dialog layout: `dialog_{name}.xml` → `dialog_confirm.xml`
- Fragment layout: `fragment_{name}.xml` → `fragment_profile.xml`

### ID Naming
```xml
<!-- Tốt -->
<TextView android:id="@+id/tvRestaurantName" />
<ImageView android:id="@+id/imgFood" />
<Button android:id="@+id/btnPlaceOrder" />

<!-- Tệ -->
<TextView android:id="@+id/textView" />
<ImageView android:id="@+id/image" />
<Button android:id="@+id/button1" />
```

### Chiều dài cố định
```xml
<!-- Tốt: dùng wrap_content hoặc match_parent -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />

<!-- Tệ: dùng kích thước cứng除非 cần thiết -->
<TextView
    android:layout_width="123dp"
    android:layout_height="45dp" />
```

---

## 12. Security

- ✅ Lưu token trong SharedPreferences (mode private)
- ✅ Gửi token qua `Authorization: Bearer {token}` header
- ✅ Dùng HTTPS trong production
- ✅ Không log token hoặc thông tin nhạy cảm
- ❌ Không hardcode API key trong source code
- ❌ Không lưu password trong SharedPreferences
- ❌ Không truyền token qua Intent extras

---

## 13. Manifest & Deep Links

```xml
<!-- Đăng ký Activity -->
<activity
    android:name=".screens.restaurant_profile"
    android:exported="true">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="https"
            android:host="yourdomain.com"
            android:pathPrefix="/restaurant" />
    </intent-filter>
</activity>
```

---

## 14. What NOT to Do

- ❌ Không dùng Jetpack Compose — dự án dùng XML layouts
- ❌ Không dùng ViewModel/LiveData/Flow — dùng SharedPreferences
- ❌ Không dùng Coroutines — dùng Retrofit `enqueue()`
- ❌ Không dùng Dagger/Hilt — không có DI
- ❌ Không dùng Navigation Component — dùng explicit Intent
- ❌ Không dùng ViewBinding/DataBinding — dùng `findViewById`
- ❌ Không dùng Room database — data từ API
- ❌ Không dùng `execute()` (blocking) — luôn dùng `enqueue()` (async)
- ❌ Không dùng `Log.d` trong production — dùng `BuildConfig.DEBUG` check
- ❌ Không tạo file riêng cho data class — đặt trong `ApiService.kt`

---

## 15. Git & Commit Style

- Dùng **Conventional Commits**: `feat:`, `fix:`, `refactor:`, `chore:`, `docs:`
- Một thay đổi logic mỗi commit
- Branch naming: `feat/restaurant-search`, `fix/cart-crash`
- Commit message tiếng Việt hoặc tiếng Anh đều được

---

## 16. Code Style (kotlinc)

- Tuân thủ Kotlin coding conventions
- Max line length: **120 characters**
- Dùng `val` thay vì `var` khi có thể
- Dùng `?.` và `?:` cho null safety
- Dùng `apply`, `let`, `with` khi phù hợp
- Không dùng `!!` (non-null assertion) trừ khi chắc chắn

```kotlin
// Tốt
val name = restaurant.name ?: "Không tên"
restaurant.image_url?.let { loadImage(it) }

// Tệ
val name = restaurant.name!!
if (restaurant.image_url != null) {
    loadImage(restaurant.image_url!!)
}
```
