# BÁO CÁO KỸ THUẬT — FOOT DELIVERY ANDROID

> **Dự án**: Ứng dụng đặt đồ ăn giao hàng (Foot Delivery Android)
> **Package**: `com.example.myapp`
> **Công nghệ**: Kotlin, Retrofit, Firebase, FastAPI (backend)
> **Ngày**: 2026-05-27

---

# PHẦN 1: TÀI LIỆU KỸ THUẬT

## 1. Danh sách chức năng được phân công

| STT | Chức năng | Mô tả | Trạng thái |
|-----|-----------|-------|------------|
| 1 | Đặt món từ nhà hàng địa phương | Duyệt nhà hàng, xem menu, chọn món, thêm vào giỏ hàng, đặt hàng với thanh toán COD/VNPay | Hoàn thành |
| 2 | Theo dõi đơn hàng | Xem trạng thái đơn hàng theo thời gian thực (chờ xác nhận → đang giao → hoàn thành), lịch sử đơn hàng | Hoàn thành |
| 3 | Tích điểm khách hàng | Tính điểm tích lũy dựa trên tổng chi tiêu, hiển thị trên profile và trang theo dõi đơn | Hoàn thành |
| 4 | Đánh giá và phản hồi | Đánh giá đơn hàng已完成 (1-5 sao + bình luận), hiển thị đánh giá trên trang chi tiết món ăn | Hoàn thành |

---

## 2. Kiến trúc chi tiết hệ thống liên quan

### Kiến trúc tổng quan

```
┌──────────────────────────────────────────────────────────────┐
│                    ANDROID APP (Kotlin)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │   screens/   │  │  adapters/   │  │    api/      │       │
│  │  (Activity)  │  │ (RecyclerView│  │ (Retrofit)   │       │
│  │  30 files    │  │   Adapter)   │  │  4 files     │       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       │
│         │                 │                  │                │
│         └─────────────────┴──────────────────┘                │
│                           │                                   │
│                    SharedPreferences                           │
│                    (JWT Token, User Info)                      │
└───────────────────────────┬──────────────────────────────────┘
                            │ HTTP/REST
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                  BACKEND (FastAPI + Python)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  main.py     │  │  models.py   │  │  schemas.py  │       │
│  │  (Routes)    │  │ (SQLAlchemy) │  │ (Pydantic)   │       │
│  └──────┬───────┘  └──────┬───────┘  └──────────────┘       │
│         │                 │                                   │
│         └─────────────────┘                                   │
│                           │                                   │
│                    PostgreSQL                                  │
└──────────────────────────────────────────────────────────────┘
```

### 2.1 Thành phần hệ thống

#### Android App

| Thành phần | Vai trò | Input | Output | Liên quan |
|------------|---------|-------|--------|-----------|
| `screens/home.kt` | Màn hình chính, hiển thị tất cả món ăn | API `GET /menu-items/` | RecyclerView danh sách món | MenuItemAdapter, food_detail |
| `screens/list_restaurant.kt` | Danh sách nhà hàng | API `GET /restaurants/` | RecyclerView nhà hàng | RestaurantAdapter, restaurant_profile |
| `screens/restaurant_profile.kt` | Chi tiết nhà hàng | API `GET /restaurants/{id}/` | Thông tin nhà hàng + menu | MenuItemAdapterSmall, food_detail |
| `screens/food_detail.kt` | Chi tiết món ăn | API `GET /menu-items/{id}/` | Thông tin món + đánh giá | cart, pre_order |
| `screens/cart.kt` | Giỏ hàng | Danh sách `CartItem` (static) | Tổng tiền, danh sách đã chọn | order |
| `screens/order.kt` | Xác nhận đặt hàng | Cart items, Address, Voucher | API `POST /orders/` | payment_methods, discouts |
| `screens/OrderTracking.kt` | Theo dõi đơn hàng | API `GET /users/{id}/orders/` | Danh sách đơn (pending/completed) | OrderTrackingDetail |
| `screens/OrderTrackingDetail.kt` | Chi tiết đơn + đánh giá | API `GET /orders/{id}/detail` | Trạng thái, items, form đánh giá | Reviews |
| `screens/PointsDetail.kt` | Chi tiết tích điểm | API `GET /users/{id}/orders/` | Điểm theo đơn hàng | OrderTrackingDetail |
| `screens/profile.kt` | Hồ sơ người dùng | API `GET /users/{id}/profile-summary` | Tên, điểm tích lũy | PointsDetail |

#### Backend

| Thành phần | Vai trò | Input | Output | Liên quan |
|------------|---------|-------|--------|-----------|
| `main.py` | API Routes | HTTP Request | HTTP Response | models, schemas |
| `models.py` | ORM Models | — | SQLAlchemy objects | Database |
| `schemas.py` | Pydantic schemas | Request body | Validated data | main.py |
| `notification_service.py` | Push notification | user_id, title, body | FCM notification | Firebase |
| `auth.py` | JWT authentication | username/password | access_token | main.py |

### 2.2 Luồng xử lý

#### Đặt món từ nhà hàng địa phương

```
Khách hàng
    │
    ▼
[Màn hình chính] ──GET /menu-items/──→ [Hiển thị danh sách món]
    │
    ├── Tìm kiếm ──GET /menu-items/search/──→ [Kết quả tìm kiếm]
    │
    ├── Chọn nhà hàng ──GET /restaurants/──→ [Danh sách nhà hàng]
    │       │
    │       ▼
    │   [restaurant_profile] ──GET /restaurants/{id}/──→ [Menu nhà hàng]
    │       │
    │       ▼
    │   [food_detail] ──GET /menu-items/{id}/──→ [Chi tiết món + đánh giá]
    │       │
    │       ▼
    │   [Thêm vào giỏ] ──→ cart.cartList (in-memory)
    │
    ▼
[Giỏ hàng] ──→ Chọn món → Điều chỉnh số lượng
    │
    ▼
[Xác nhận đơn hàng] ──→ Nhập/chọn địa chỉ
    │                    Chọn voucher (GET /promotions/)
    │                    Chọn phương thức thanh toán
    │
    ├── COD ──→ POST /orders/ ──→ PUT /orders/{id}/status "confirmed"
    │
    └── VNPay ──→ GET /create-payment ──→ WebView VNPay
                    │
                    ▼
                vnp_ResponseCode=00?
                    │
                ┌───┴───┐
               YES      NO
                │        │
                ▼        ▼
          "paid"    "cancelled"
                │
                ▼
        [payment_successful]
```

#### Theo dõi đơn hàng

```
Khách hàng
    │
    ▼
[OrderTracking] ──GET /users/{id}/orders/──→ [Tất cả đơn hàng]
    │
    ├── Pending orders (đỏ): "Chưa giao"
    │       │
    │       ▼
    │   [OrderTrackingDetail] ──GET /orders/{id}/detail──→
    │       │                   [Chi tiết đơn hàng]
    │       │                   [Trạng thái: Chờ xác nhận →
    │       │                    Đã xác nhận → Đang giao]
    │       │
    │       └── Nút "Đánh giá" DISABLED
    │           (text: "Chờ giao hàng để đánh giá")
    │
    └── Completed orders (xanh): "Đã giao"
            │
            ▼
        [OrderTrackingDetail] ──GET /orders/{id}/detail──→
            │                   [Chi tiết đơn hàng]
            │                   [Trạng thái: Hoàn thành]
            │
            └── Nút "Đánh giá" ENABLED
                → Xem phần Đánh giá

Backend push notification:
    PUT /orders/{id}/status → notify_user()
        → "Đơn hàng được đặt thành công!" (paid/confirmed)
        → "Đơn hàng đang giao" (delivering)
        → "Đơn hàng đã giao" (completed)
```

#### Tích điểm khách hàng

```
Đơn hàng hoàn thành
    │
    ▼
[profile] ──GET /users/{id}/profile-summary──→
    │
    │   Backend tính toán:
    │   points = total_spent // 10000
    │   delivered_orders = COUNT(status IN ('completed','delivered','done'))
    │   total_spent = SUM(orders.totalprice)
    │
    ▼
[Hiển thị điểm trên profile]
    │
    ├── Click vào điểm → [OrderTracking]
    │                       └── loadPointsSummary()
    │
    └── [PointsDetail] ──GET /users/{id}/orders/──→
            │           (filtered: status = "completed")
            │
            └── Mỗi đơn: điểm = totalprice / 1000
                Click → [OrderTrackingDetail]
```

#### Đánh giá và phản hồi

```
Khách hàng
    │
    ▼
[OrderTrackingDetail] (chỉ khi đơn Đã giao)
    │
    ├── Chọn sao (1-5) ──→ updateStarColors()
    │
    ├── Nhập bình luận ──→ etFeedback
    │
    └── Nhấn "Đánh giá"
            │
            ▼
        Validation:
        - rating > 0
        - feedback không trống
        - userId > 0
        - orderId > 0
            │
            ▼
        POST /reviews/
        { rating, comment, orderid, userid }
            │
            ▼
        Backend:
        - Lưu Review vào DB
        - Tính lại avg_rating cho Restaurant
        - Cập nhật restaurant.rating
            │
            ▼
        Toast: "Cảm ơn bạn đã đánh giá!"
            │
            ▼
        [food_detail] ──GET /menu-items/{id}/──→
            │           Hiển thị đánh giá mới
            └── displayReviews() (tối đa 10 review)
```

---

## 3. Code đáp ứng chức năng

### 3.1 File liên quan

| File | Vai trò | Chức năng |
|------|---------|-----------|
| `screens/home.kt` | Màn hình chính | Hiển thị tất cả món ăn, tìm kiếm |
| `screens/list_restaurant.kt` | Danh sách nhà hàng | Tìm kiếm và hiển thị nhà hàng |
| `screens/restaurant_profile.kt` | Chi tiết nhà hàng | Thông tin nhà hàng, menu |
| `screens/food_detail.kt` | Chi tiết món ăn | Thông tin món, đánh giá, thêm giỏ |
| `screens/cart.kt` | Giỏ hàng | Quản lý món đã chọn |
| `screens/pre_order.kt` | Đặt trước | Chọn ngày/giao giao hàng |
| `screens/pre_order_cart.kt` | Giỏ đặt trước | Quản lý đơn đặt trước |
| `screens/order.kt` | Xác nhận đơn | Địa chỉ, voucher, thanh toán |
| `screens/discouts.kt` | Voucher | Chọn mã giảm giá |
| `screens/payment_methods.kt` | Thanh toán | COD / VNPay |
| `screens/VNPayActivity.kt` | VNPay | WebView thanh toán online |
| `screens/payment_successful.kt` | Thành công | Xác nhận đã thanh toán |
| `screens/OrderTracking.kt` | Theo dõi đơn | Danh sách đơn, phân trang |
| `screens/OrderTrackingDetail.kt` | Chi tiết đơn | Trạng thái, đánh giá |
| `screens/order_history.kt` | Lịch sử | 2 đơn gần nhất |
| `screens/delivered_order_details.kt` | Chi tiết đã giao | Thông tin đơn, đặt lại |
| `screens/PointsDetail.kt` | Tích điểm | Điểm theo đơn hàng |
| `screens/profile.kt` | Hồ sơ | Tên, điểm tích lũy |
| `adapters/MenuItemAdapter.kt` | Adapter món ăn | Hiển thị trên home |
| `adapters/MenuItemAdapterSmall.kt` | Adapter nhỏ | Hiển thị trên restaurant_profile |
| `adapters/RestaurantAdapter.kt` | Adapter nhà hàng | Hiển thị danh sách nhà hàng |
| `adapters/CartItemAdapter.kt` | Adapter giỏ hàng | Hiển thị trong order |
| `api/ApiService.kt` | API definitions | Data classes + Retrofit interface |
| `api/RetrofitClient.kt` | HTTP Client | Singleton + auth interceptor |
| `api/NetworkConfig.kt` | Config | Base URL |

### 3.2 Class liên quan

| Class | File | Vai trò |
|-------|------|---------|
| `home` | screens/home.kt | Activity màn hình chính |
| `list_restaurant` | screens/list_restaurant.kt | Activity danh sách nhà hàng |
| `restaurant_profile` | screens/restaurant_profile.kt | Activity chi tiết nhà hàng |
| `food_detail` | screens/food_detail.kt | Activity chi tiết món ăn |
| `cart` | screens/cart.kt | Activity giỏ hàng + companion object |
| `CartAdapter` | screens/cart.kt | Adapter hiển thị giỏ hàng |
| `CartItem` | screens/cart.kt | Data class sản phẩm trong giỏ |
| `pre_order` | screens/pre_order.kt | Activity đặt trước |
| `pre_order_cart` | screens/pre_order_cart.kt | Activity giỏ đặt trước |
| `PreOrderItem` | screens/pre_order_cart.kt | Data class đơn đặt trước |
| `order` | screens/order.kt | Activity xác nhận đơn |
| `discouts` | screens/discouts.kt | Activity chọn voucher |
| `payment_methods` | screens/payment_methods.kt | Activity chọn thanh toán |
| `VNPayActivity` | screens/VNPayActivity.kt | Activity VNPay WebView |
| `payment_successful` | screens/payment_successful.kt | Activity thanh toán thành công |
| `OrderTracking` | screens/OrderTracking.kt | Activity theo dõi đơn |
| `OrderTrackingDetail` | screens/OrderTrackingDetail.kt | Activity chi tiết đơn + đánh giá |
| `order_history` | screens/order_history.kt | Activity lịch sử đơn |
| `delivered_order_details` | screens/delivered_order_details.kt | Activity chi tiết đã giao |
| `PointsDetail` | screens/PointsDetail.kt | Activity tích điểm |
| `profile` | screens/profile.kt | Activity hồ sơ |
| `MenuItemAdapter` | adapters/MenuItemAdapter.kt | Adapter món ăn (home) |
| `MenuItemAdapterSmall` | adapters/MenuItemAdapterSmall.kt | Adapter nhỏ (restaurant) |
| `RestaurantAdapter` | adapters/RestaurantAdapter.kt | Adapter nhà hàng |
| `CartItemAdapter` | screens/CartItemAdapter.kt | Adapter giỏ hàng (order) |
| `RetrofitClient` | api/RetrofitClient.kt | Singleton HTTP client |
| `ApiService` | api/ApiService.kt | Retrofit interface |

### 3.3 Hàm liên quan

#### Đặt món

| Hàm | File | Input | Output | Chức năng |
|-----|------|-------|--------|-----------|
| `fetchMenuItems()` | home.kt | — | `List<MenuItem>` | Tải tất cả món ăn |
| `searchMenuItems(query)` | home.kt | String | `List<MenuItem>` | Tìm kiếm món theo tên |
| `searchByCategory(query)` | home.kt | String | `List<MenuItem>` | Tìm kiếm theo danh mục |
| `fetchRestaurants()` | list_restaurant.kt | — | `List<Restaurant>` | Tải danh sách nhà hàng |
| `searchRestaurants(query)` | list_restaurant.kt | String | `List<Restaurant>` | Tìm kiếm nhà hàng |
| `fetchRestaurantDetails(id)` | restaurant_profile.kt | Int | `Restaurant` | Tải chi tiết nhà hàng |
| `fetchFoodDetails(id)` | food_detail.kt | Int | `MenuItem` | Tải chi tiết món ăn |
| `addToCart()` | food_detail.kt | MenuItem, Int | — | Thêm vào giỏ hàng |
| `updateSummary()` | cart.kt | — | — | Cập nhật tổng tiền |
| `createOrderFromCart()` | order.kt | — | `OrderResponse` | Tạo đơn hàng |

#### Theo dõi đơn hàng

| Hàm | File | Input | Output | Chức năng |
|-----|------|-------|--------|-----------|
| `loadAllOrders()` | OrderTracking.kt | — | `List<OrderResponse>` | Tải tất cả đơn hàng |
| `loadOrdersDetail(pending, completed)` | OrderTracking.kt | List, List | — | Tải chi tiết từng đơn |
| `bindAllOrders(pending, completed)` | OrderTracking.kt | List, List | — | Hiển thị danh sách |
| `addOrderCard()` | OrderTracking.kt | OrderDetailResponse | — | Tạo card đơn hàng |
| `isCompletedStatus(status)` | OrderTracking.kt | String | Boolean | Kiểm tra trạng thái hoàn thành |
| `loadOrderSummary(orderId)` | OrderTrackingDetail.kt | Int | `OrderDetailResponse` | Tải chi tiết đơn |
| `populateOrderDetails(order)` | OrderTrackingDetail.kt | OrderDetailResponse | — | Hiển thị thông tin đơn |
| `formatOrderDate()` | OrderTrackingDetail.kt | — | String | Định dạng ngày |

#### Tích điểm

| Hàm | File | Input | Output | Chức năng |
|-----|------|-------|--------|-----------|
| `loadPointsSummary()` | OrderTracking.kt | — | `UserProfileSummary` | Tải tổng điểm |
| `loadOrders()` | PointsDetail.kt | — | `List<OrderResponse>` | Tải đơn đã hoàn thành |
| `bindOrderCard()` | PointsDetail.kt | OrderResponse | — | Hiển thị card điểm |
| `resolveUserIdForPoints()` | PointsDetail.kt | — | Int | Lấy user ID |

#### Đánh giá

| Hàm | File | Input | Output | Chức năng |
|-----|------|-------|--------|-----------|
| `updateStarColors(stars)` | OrderTrackingDetail.kt | Array<ImageView> | — | Cập nhật màu sao |
| `createReview()` | OrderTrackingDetail.kt | ReviewCreateRequest | `ReviewResponse` | Gửi đánh giá |
| `displayReviews()` | food_detail.kt | — | — | Hiển thị đánh giá |
| `setupUI()` | food_detail.kt | — | — | Thiết lập RatingBar |

### 3.4 Database

| Bảng | File schema | Chức năng | Quan hệ |
|------|-------------|-----------|---------|
| `Restaurant` | models.py, main.sql | Thông tin nhà hàng | 1-N: MenuItem, Orders, Review |
| `MenuItem` | models.py, main.sql | Món ăn trong menu | N-1: Restaurant, Category; 1-N: OrderItem, Review |
| `Category` | models.py, main.sql | Danh mục món ăn | 1-N: MenuItem |
| `Orders` | models.py, main.sql | Đơn hàng | N-1: User, Restaurant, Address; 1-N: OrderItem, Review |
| `OrderItem` | models.py, main.sql | Chi tiết đơn hàng | N-1: Orders, MenuItem |
| `Address` | models.py, main.sql | Địa chỉ giao hàng | N-1: User; 1-N: Orders |
| `User` | models.py, main.sql | Người dùng | 1-N: Orders, Address, Review, Notification |
| `Review` | models.py, main.sql | Đánh giá | N-1: User, MenuItem, Restaurant, Orders |
| `Payment` | models.py, main.sql | Thanh toán | N-1: Orders |
| `Promotion` | models.py, main.sql | Mã giảm giá | 1-N: UsedPromotion |
| `UsedPromotion` | models.py, main.sql | Mã đã sử dụng | N-1: Promotion, Orders |
| `LoyaltyPoint` | models.py, main.sql | Điểm tích lũy (KHÔNG DÙNG) | N-1: User, MenuItem |
| `Notification` | models.py, main.sql | Thông báo | N-1: User, Orders |
| `Delivery` | models.py, main.sql | Giao hàng | N-1: Orders, Shipper |
| `Shipper` | models.py, main.sql | Người giao hàng | 1-N: Delivery |

**Chi tiết bảng chính:**

**Orders**
- Primary Key: `id` (Integer, auto-increment)
- Foreign Key: `restaurantid` → `Restaurant.id`
- Foreign Key: `addressid` → `Address.id`
- Foreign Key: `userid` → `User.id`
- Các trường: `status`, `createdat`, `preorderdate`, `preordertime`, `totalprice`

**Review**
- Primary Key: `id` (Integer, auto-increment)
- Foreign Key: `menuitemid` → `MenuItem.id`
- Foreign Key: `restaurantid` → `Restaurant.id`
- Foreign Key: `userid` → `User.id`
- Foreign Key: `orderid` → `Orders.id`
- Các trường: `rating` (Integer), `comment` (Text)

**LoyaltyPoint** (tồn tại nhưng KHÔNG được sử dụng)
- Primary Key: `id`
- Foreign Key: `userid` → `User.id`
- Foreign Key: `menuitemid` → `MenuItem.id`
- Các trường: `points` (default 0), `updatedat`
- **Ghi chú**: Điểm được tính động từ `total_spent // 10000` trong API, không dùng bảng này

### 3.5 API

| Endpoint | Method | File xử lý | Chức năng |
|----------|--------|------------|-----------|
| `/restaurants/` | GET | main.py | Danh sách nhà hàng |
| `/restaurants/search/` | GET | main.py | Tìm kiếm nhà hàng theo tên |
| `/restaurants/{id}/` | GET | main.py | Chi tiết nhà hàng + menu |
| `/menu-items/` | GET | main.py | Danh sách tất cả món ăn |
| `/menu-items/{id}/` | GET | main.py | Chi tiết món ăn + đánh giá |
| `/menu-items/search/` | GET | main.py | Tìm kiếm món theo tên |
| `/menu-items/category/search/` | GET | main.py | Tìm kiếm theo danh mục |
| `/orders/` | POST | main.py | Tạo đơn hàng mới |
| `/users/{id}/orders/` | GET | main.py | Danh sách đơn hàng của user |
| `/orders/{id}/detail` | GET | main.py | Chi tiết đơn hàng |
| `/orders/{id}/status` | PUT | main.py | Cập nhật trạng thái đơn |
| `/reviews/` | POST | main.py | Tạo đánh giá mới |
| `/users/{id}/profile-summary` | GET | main.py | Tổng quan hồ sơ + điểm |
| `/promotions/` | GET | main.py | Danh sách mã giảm giá |
| `/create-payment` | GET | main.py | Tạo URL thanh toán VNPay |
| `/vnpay_return` | GET | main.py | Callback từ VNPay |
| `/users/{id}/addresses/` | GET | main.py | Danh sách địa chỉ |
| `/addresses/` | POST | main.py | Tạo địa chỉ mới |
| `/addresses/{id}/` | PUT | main.py | Cập nhật địa chỉ |

---

## 4. Hướng dẫn cài đặt và triển khai

### Yêu cầu môi trường

- **Android**: Android Studio, JDK 17, Android SDK 24-34
- **Backend**: Python 3.10+, pip/uv
- **Database**: PostgreSQL
- **Docker** (tùy chọn): Docker Desktop
- **Firebase**: Tài khoản Firebase cho FCM

### Các bước cài đặt

#### Backend

```bash
# 1. Clone source
git clone <repository-url>
cd foot-delivery-android/backend

# 2. Cài dependency
pip install -r requirements.txt
# hoặc: uv pip install -r requirements.txt

# 3. Cấu hình
cp .env.example .env
# Chỉnh sửa .env với database URL, JWT secret, Firebase credentials

# 4. Chạy database
docker-compose up -d postgres
# hoặc dùng PostgreSQL đã cài đặt

# 5. Khởi tạo database
psql -U postgres -f main.sql

# 6. Chạy backend
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

#### Android App

```bash
# 1. Mở project trong Android Studio
# File → Open → chọn thư mục foot-delivery-android/app

# 2. Sync Gradle
# Android Studio sẽ tự động sync

# 3. Cấu hình API URL
# Chỉnh sửa api/NetworkConfig.kt
# - Emulator: http://10.0.2.2:8000/
# - Device: http://<your-ip>:8000/

# 4. Chạy app
# Chọn device/emulator → Run (Shift+F10)
```

### Lưu ý

- **Port**: Backend chạy trên port 8000, Android emulator dùng `10.0.2.2` để truy cập localhost
- **Biến môi trường**: Cần cấu hình `DATABASE_URL`, `JWT_SECRET_KEY`, Firebase credentials
- **Firebase**: Cần tải `google-services.json` từ Firebase Console đặt vào `app/app/`
- **Network Security**: Android app cho phép HTTP traffic qua `network_security_config.xml`
- **Lỗi thường gặp**:
  - `Connection refused`: Backend chưa chạy hoặc sai URL
  - `401 Unauthorized`: Token hết hạn, cần đăng nhập lại
  - `Firebase not initialized`: Thiếu `google-services.json`

---

# PHẦN 2: CODE

## 1. Phần cá nhân thực hiện

| File | Nội dung thực hiện | Chức năng |
|------|-------------------|-----------|
| `screens/home.kt` | Màn hình chính | Hiển thị món ăn, tìm kiếm debounce 500ms |
| `screens/list_restaurant.kt` | Danh sách nhà hàng | Tìm kiếm và hiển thị nhà hàng |
| `screens/restaurant_profile.kt` | Chi tiết nhà hàng | Banner, thông tin, menu |
| `screens/food_detail.kt` | Chi tiết món ăn | Đánh giá, số lượng, thêm giỏ |
| `screens/cart.kt` | Giỏ hàng | Quản lý món, tính tổng |
| `screens/order.kt` | Xác nhận đơn | Địa chỉ, voucher, thanh toán |
| `screens/OrderTracking.kt` | Theo dõi đơn | Phân trang, phân loại pending/completed |
| `screens/OrderTrackingDetail.kt` | Chi tiết đơn + đánh giá | Trạng thái, form đánh giá 1-5 sao |
| `screens/PointsDetail.kt` | Tích điểm | Điểm theo đơn hàng |
| `screens/profile.kt` | Hồ sơ | Hiển thị điểm tích lũy |
| `adapters/MenuItemAdapter.kt` | Adapter món ăn | Hiển thị trên home |
| `adapters/RestaurantAdapter.kt` | Adapter nhà hàng | Hiển thị danh sách |
| `api/ApiService.kt` | API definitions | Tất cả data classes và endpoints |
| `api/RetrofitClient.kt` | HTTP Client | Auth interceptor |

## 2. Tối ưu source code

### Đã tối ưu

| Vấn đề | Giải pháp | File |
|--------|-----------|------|
| Tìm kiếm gây nhiều request | Debounce 500ms | home.kt, list_restaurant.kt |
| Load ảnh từ URL | Picasso với placeholder + error | MenuItemAdapter, RestaurantAdapter |
| Auth token | OkHttp Interceptor tự động đính kèm | RetrofitClient.kt |
| Giỏ hàng | Static companion object (in-memory) | cart.kt |

### Có thể bổ sung

| Vấn đề | Đề xuất |
|--------|---------|
| Không có pagination cho menu items | Thêm infinite scroll |
| Gọi `getOrderDetail()` N lần trong OrderTracking | Batch API hoặc dùng response đã có |
| `LoyaltyPoint` table không dùng | Xóa hoặc tích hợp vào tính điểm |
| Không cache API response | Thêm OkHttp cache hoặc Room |
| Cart mất khi đóng app | Lưu vào SharedPreferences hoặc Room |

## 3. Kiểm tra comment

### Tình trạng hiện tại

| Loại | Đã comment | Chưa comment |
|------|-----------|--------------|
| Class | ❌ Không | Tất cả Activity, Adapter |
| Hàm | ❌ Không | Tất cả hàm |
| API interface | ❌ Không | ApiService.kt |
| Data class | ❌ Không | Tất cả data class |
| Logic phức tạp | ❌ Không | Debounce, pagination, payment flow |

### Vị trí nên thêm comment

| File | Vị trí | Loại comment |
|------|--------|--------------|
| `home.kt` | `searchMenuItems()` | KDoc: debounce logic |
| `cart.kt` | `companion object { cartList }` | KDoc: giải thích static cart |
| `order.kt` | `createOrderFromCart()` | KDoc: flow đặt hàng |
| `order.kt` | `calculateDiscountAmount()` | KDoc: logic tính giảm giá |
| `OrderTracking.kt` | `loadAllOrders()` | KDoc: phân trang |
| `OrderTracking.kt` | `isCompletedStatus()` | KDoc: logic trạng thái |
| `OrderTrackingDetail.kt` | `updateStarColors()` | KDoc: UI star rating |
| `OrderTrackingDetail.kt` | `createReview()` | KDoc: validation + submit |
| `payment_methods.kt` | `startVnPayPayment()` | KDoc: VNPay flow |
| `VNPayActivity.kt` | `shouldOverrideUrlLoading()` | KDoc: intercept callback |
| `ApiService.kt` | Toàn bộ interface | KDoc cho mỗi endpoint |
| `ApiService.kt` | Tất cả data class | KDoc + @property |
| `RetrofitClient.kt` | Interceptor | KDoc: auth flow |

---

# PHỤ LỤC

## Danh sách tất cả data classes liên quan

```kotlin
// Đặt món
data class MenuItem(id, name, image_url, price, is_available, description, restaurantid, categoryid, restaurant_name, reviews, avg_rating)
data class Restaurant(id, name, image_url, address, rating, open_time, close_time, phone_number, status, description, menu_items)
data class CartItem(id, name, price, qty, imageUrl, isSelected)           // trong cart.kt
data class PreOrderItem(id, name, price, qty, imageUrl, deliveryTime, isSelected)  // trong pre_order_cart.kt

// Đặt hàng
data class OrderCreateRequest(status, preorderdate, preordertime, totalprice, restaurantid, addressid, userid, order_items)
data class OrderItemRequest(quantity, price, menuitemid)
data class OrderResponse(id, status, createdat, preorderdate, preordertime, totalprice, restaurantid, addressid, userid)
data class OrderDetailResponse(id, status, createdat, preorderdate, preordertime, totalprice, restaurantid, addressid, userid, restaurant_name, address_detail, order_items)
data class OrderItemDetailResponse(id, quantity, price, menuitemid, menuitem_name, image_url)
data class OrderStatusUpdateRequest(status)

// Thanh toán
data class VNPayResponse(payment_url)
data class PromotionResponse(id, code, discounttype, discountvalue, expiredate, minordervalue, status)

// Đánh giá
data class ReviewDetail(id, rating, comment, userid, user_name)
data class ReviewCreateRequest(rating, comment, orderid, userid, menuitemid, restaurantid)
data class ReviewResponse(id, rating, comment, orderid, menuitemid, restaurantid, userid)

// Người dùng
data class UserProfileSummary(user_id, user_name, points, delivered_orders, total_spent)
data class Address(id, detail, phone, userid)
data class AddressCreateRequest(detail, phone, userid)
data class AddressUpdateRequest(detail, phone)
data class LoginRequest(username, password)
data class LoginResponse(access_token, token_type)
data class RegisterRequest(name, username, email, phone, role, password)
data class RegisterResponse(name, email, phone, role, id)

// Thông báo
data class Notification(id, title, type, content, isread, createdat, userid, orderid, sessionid)
data class NotificationCreate(title, type, content, isread, userid, orderid, sessionid)
data class DeviceTokenResponse(message)

// Chat
data class ChatMessageRequest(message)
data class ChatMessageResponse(message, id, senderrole, sentat, sessionid)
data class FAQ(id, question, answer, isactive)
```

## Trạng thái đơn hàng

| Trạng thái | Tiếng Việt | Mô tả |
|------------|-----------|-------|
| `pending` | Chờ xác nhận | Đơn mới tạo |
| `paid` | Đã thanh toán | Đã thanh toán VNPay |
| `confirmed` | Đã xác nhận | Xác nhận từ nhà hàng |
| `delivering` | Đang giao | Shipper đang giao |
| `completed` | Đã giao | Giao hàng thành công |
| `cancelled` | Đã hủy | Đơn bị hủy |
