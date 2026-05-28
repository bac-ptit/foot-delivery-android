package com.example.myapp.screens.api

/**
 * @file ApiService.kt
 * @brief Định nghĩa giao diện API Retrofit và các data class cho ứng dụng giao đồ ăn.
 *        Bao gồm các model request/response và endpoint gọi API đến backend.
 */

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


/** Dữ liệu yêu cầu đăng nhập gồm tên đăng nhập và mật khẩu. */
data class LoginRequest(
    val username: String,
    val password: String
)


/** Dữ liệu yêu cầu đăng ký tài khoản mới với thông tin cá nhân và vai trò. */
data class RegisterRequest(
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val role: String,
    val password: String
)


/** Phản hồi đăng nhập chứa access token và loại token. */
data class LoginResponse(
    val access_token: String,
    val token_type: String
)


/** Phản hồi đăng ký trả về thông tin người dùng đã tạo. */
data class RegisterResponse(
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val id: Int
)


/** Thông tin món ăn bao gồm giá, trạng thái, đánh giá và nhà hàng thuộc về. */
data class MenuItem(
    val id: Int,
    val name: String,
    val image_url: String?,
    val price: Int,
    val is_available: Boolean,
    val description: String?,
    val restaurantid: Int,
    val categoryid: Int,
    val restaurant_name: String? = null,
    val reviews: List<ReviewDetail>? = null,
    val avg_rating: Float? = null
)

/** Chi tiết đánh giá của người dùng cho một món ăn. */
data class ReviewDetail(
    val id: Int,
    val rating: Int,
    val comment: String?,
    val userid: Int,
    val user_name: String?
)


/** Thông tin nhà hàng bao gồm địa chỉ, giờ mở cửa, trạng thái và danh sách món ăn. */
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


/** Thông báo gửi đến người dùng, liên kết với đơn hàng hoặc phiên chat. */
data class Notification(
    val id: Int,
    val title: String,
    val type: String,
    val content: String,
    val isread: Boolean,
    val createdat: String,
    val userid: Int,
    val orderid: Int?,
    val sessionid: Int?
)


/** Dữ liệu tạo thông báo mới với tiêu đề, nội dung và tùy chọn liên kết đơn hàng/phiên. */
data class NotificationCreate(
    val title: String,
    val type: String,
    val content: String,
    val isread: Boolean = false,
    val userid: Int,
    val orderid: Int? = null,
    val sessionid: Int? = null
)


/** Câu hỏi thường gặp (FAQ) với trạng thái kích hoạt. */
data class FAQ(
    val id: Int,
    val question: String,
    val answer: String,
    val isactive: Boolean
)


/** Yêu cầu gửi tin nhắn chat từ người dùng. */
data class ChatMessageRequest(
    val message: String
)


/** Phản hồi tin nhắn chat chứa nội dung, vai trò người gửi và mã phiên. */
data class ChatMessageResponse(
    val message: String,
    val id: Int,
    val senderrole: String,
    val sentat: String,
    val sessionid: Int
)


/** Địa chỉ giao hàng của người dùng. */
data class Address(
    val id: Int,
    val detail: String,
    val phone: String?,
    val userid: Int
)


/** Yêu cầu tạo địa chỉ giao hàng mới. */
data class AddressCreateRequest(
    val detail: String,
    val phone: String?,
    val userid: Int
)


/** Yêu cầu cập nhật thông tin địa chỉ giao hàng. */
data class AddressUpdateRequest(
    val detail: String?,
    val phone: String?
)


/** Phản hồi từ cổng thanh toán VNPay chứa URL thanh toán. */
data class VNPayResponse(
    val payment_url: String
)

/** Phản hồi khi đăng ký thiết bị nhận thông báo đẩy. */
data class DeviceTokenResponse(
    val message: String
)

/** Tóm tắt hồ sơ người dùng bao gồm điểm tích lũy, số đơn đã giao và tổng chi tiêu. */
data class UserProfileSummary(
    val user_id: Int,
    val user_name: String,
    val points: Int,
    val delivered_orders: Int,
    val total_spent: Int
)

/** Thông tin một mục trong đơn hàng khi tạo đơn, gồm số lượng và giá. */
data class OrderItemRequest(
    val quantity: Int,
    val price: Int,
    val menuitemid: Int
)

/** Yêu cầu tạo đơn hàng mới với danh sách món, địa chỉ giao và tùy chọn đặt trước. */
data class OrderCreateRequest(
    val status: String,
    val preorderdate: String? = null,
    val preordertime: String? = null,
    val totalprice: Int,
    val restaurantid: Int,
    val addressid: Int,
    val userid: Int,
    val order_items: List<OrderItemRequest>
)

/** Phản hồi thông tin đơn hàng từ server. */
data class OrderResponse(
    val id: Int,
    val status: String,
    val createdat: String,
    val preorderdate: String?,
    val preordertime: String?,
    val totalprice: Int,
    val restaurantid: Int,
    val addressid: Int,
    val userid: Int
)

/** Chi tiết từng mục trong đơn hàng, bao gồm tên và hình ảnh món ăn. */
data class OrderItemDetailResponse(
    val id: Int,
    val quantity: Int,
    val price: Int,
    val menuitemid: Int,
    val menuitem_name: String?,
    val image_url: String? = null
)

/** Phản hồi chi tiết đơn hàng kèm tên nhà hàng, địa chỉ và danh sách món. */
data class OrderDetailResponse(
    val id: Int,
    val status: String,
    val createdat: String,
    val preorderdate: String?,
    val preordertime: String?,
    val totalprice: Int,
    val restaurantid: Int,
    val addressid: Int,
    val userid: Int,
    val restaurant_name: String?,
    val address_detail: String?,
    val order_items: List<OrderItemDetailResponse>
)

/** Yêu cầu cập nhật trạng thái đơn hàng. */
data class OrderStatusUpdateRequest(
    val status: String
)

/** Yêu cầu tạo đánh giá cho đơn hàng, có thể đánh giá món ăn hoặc nhà hàng. */
data class ReviewCreateRequest(
    val rating: Int,
    val comment: String? = null,
    val orderid: Int,
    val userid: Int,
    val menuitemid: Int? = null,
    val restaurantid: Int? = null
)

/** Phản hồi thông tin đánh giá đã tạo từ server. */
data class ReviewResponse(
    val id: Int,
    val rating: Int,
    val comment: String?,
    val orderid: Int,
    val menuitemid: Int?,
    val restaurantid: Int?,
    val userid: Int
)

/** Thông tin mã khuyến mãi bao gồm loại giảm giá, giá trị và ngày hết hạn. */
data class PromotionResponse(
    val id: Int,
    val code: String,
    val discounttype: String,
    val discountvalue: Int,
    val expiredate: String,
    val minordervalue: Int,
    val status: String
)


/** Giao diện định nghĩa tất cả các endpoint API của ứng dụng giao đồ ăn. */
interface ApiService {
    /**
     * Đăng nhập người dùng bằng tên đăng nhập và mật khẩu.
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @param grantType Loại cấp quyền, mặc định là "password"
     * @return Đối tượng Call chứa LoginResponse với access token
     */
    @FormUrlEncoded
    @POST("token")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password"


    ): Call<LoginResponse>


    /**
     * Đăng ký tài khoản người dùng mới.
     * @param request Thông tin đăng ký bao gồm tên, email, số điện thoại, vai trò
     * @return Đối tượng Call chứa RegisterResponse với thông tin người dùng đã tạo
     */
    @POST("users/")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>


    /**
     * Lấy thông tin người dùng hiện tại dựa trên token xác thực.
     * @return Đối tượng Call chứa RegisterResponse với thông tin người dùng
     */
    @GET("users/me/")
    fun getCurrentUser(): Call<RegisterResponse>

    /**
     * Lấy tóm tắt hồ sơ người dùng bao gồm điểm tích lũy và thống kê đơn hàng.
     * @param userId ID của người dùng
     * @return Đối tượng Call chứa UserProfileSummary
     */
    @GET("users/{user_id}/profile-summary")
    fun getProfileSummary(@Path("user_id") userId: Int): Call<UserProfileSummary>


    /**
     * Lấy danh sách tất cả nhà hàng.
     * @return Đối tượng Call chứa danh sách Restaurant
     */
    @GET("restaurants/")
    fun getRestaurants(): Call<List<Restaurant>>


    /**
     * Tìm kiếm nhà hàng theo tên.
     * @param name Từ khóa tìm kiếm tên nhà hàng
     * @return Đối tượng Call chứa danh sách Restaurant khớp với từ khóa
     */
    @GET("restaurants/search/")
    fun searchRestaurantsByName(@retrofit2.http.Query("name") name: String): Call<List<Restaurant>>


    /**
     * Lấy thông tin chi tiết của một nhà hàng theo ID.
     * @param restaurantId ID của nhà hàng
     * @return Đối tượng Call chứa Restaurant
     */
    @GET("restaurants/{restaurant_id}/")
    fun getRestaurant(@Path("restaurant_id") restaurantId: Int): Call<Restaurant>


    /**
     * Lấy danh sách thông báo của người dùng.
     * @param userId ID của người dùng
     * @return Đối tượng Call chứa danh sách Notification
     */
    @GET("users/{user_id}/notifications/")
    fun getUserNotifications(@Path("user_id") userId: Int): Call<List<Notification>>


    /**
     * Tạo thông báo mới cho người dùng.
     * @param notification Dữ liệu thông báo cần tạo
     * @return Đối tượng Call chứa Notification đã tạo
     */
    @POST("notifications/")
    fun createNotification(@Body notification: NotificationCreate): Call<Notification>


    /**
     * Đánh dấu thông báo đã được đọc.
     * @param notificationId ID của thông báo cần đánh dấu
     * @return Đối tượng Call chứa Void
     */
    @retrofit2.http.PUT("notifications/{notification_id}/read")
    fun markNotificationAsRead(@Path("notification_id") notificationId: Int): Call<Void>


    /**
     * Lấy danh sách tất cả món ăn từ mọi nhà hàng.
     * @return Đối tượng Call chứa danh sách MenuItem
     */
    @GET("menu-items/")
    fun getAllMenuItems(): Call<List<MenuItem>>

    /**
     * Lấy thông tin chi tiết của một món ăn theo ID.
     * @param menuItemId ID của món ăn
     * @return Đối tượng Call chứa MenuItem
     */
    @GET("menu-items/{menu_item_id}/")
    fun getMenuItemDetail(@Path("menu_item_id") menuItemId: Int): Call<MenuItem>

    /**
     * Tìm kiếm món ăn theo tên.
     * @param name Từ khóa tìm kiếm tên món ăn
     * @return Đối tượng Call chứa danh sách MenuItem khớp với từ khóa
     */
    @GET("menu-items/search/")
    fun searchMenuItemsByName(@retrofit2.http.Query("name") name: String): Call<List<MenuItem>>

    /**
     * Lấy danh sách tất cả mã khuyến mãi hiện có.
     * @return Đối tượng Call chứa danh sách PromotionResponse
     */
    @GET("promotions/")
    fun getPromotions(): Call<List<PromotionResponse>>


    /**
     * Tìm kiếm món ăn theo danh mục.
     * @param categoryName Tên danh mục cần tìm
     * @return Đối tượng Call chứa danh sách MenuItem thuộc danh mục
     */
    @GET("menu-items/category/search/")
    fun searchMenuItemsByCategory(@retrofit2.http.Query("category_name") categoryName: String): Call<List<MenuItem>>


    /**
     * Lấy danh sách câu hỏi thường gặp (FAQ).
     * @return Đối tượng Call chứa danh sách FAQ
     */
    @GET("faqs/")
    fun getFAQs(): Call<List<FAQ>>


    /**
     * Gửi tin nhắn chat đến hệ thống hỗ trợ.
     * @param request Nội dung tin nhắn cần gửi
     * @param sessionId ID phiên chat (tùy chọn, null để tạo phiên mới)
     * @return Đối tượng Call chứa ChatMessageResponse
     */
    @POST("chat/")
    fun sendMessage(@Body request: ChatMessageRequest, @retrofit2.http.Query("session_id") sessionId: Int? = null): Call<ChatMessageResponse>


    /**
     * Lấy danh sách địa chỉ giao hàng của người dùng.
     * @param userId ID của người dùng
     * @return Đối tượng Call chứa danh sách Address
     */
    @GET("users/{user_id}/addresses/")
    fun getUserAddresses(@Path("user_id") userId: Int): Call<List<Address>>


    /**
     * Tạo địa chỉ giao hàng mới cho người dùng.
     * @param address Thông tin địa chỉ cần tạo
     * @return Đối tượng Call chứa Address đã tạo
     */
    @POST("addresses/")
    fun createAddress(@Body address: AddressCreateRequest): Call<Address>




    /**
     * Tạo URL thanh toán VNPay cho đơn hàng.
     * @param amount Tổng số tiền cần thanh toán
     * @param orderId Mã đơn hàng
     * @return Đối tượng Call chứa VNPayResponse với URL thanh toán
     */
    // ... trong interface ApiService
    @GET("create-payment") // Thay bằng đường dẫn API thật của bạn
    fun getVNPayUrl(
        @retrofit2.http.Query("amount") amount: Int,
        @retrofit2.http.Query("order_id") orderId: String
    ): Call<VNPayResponse> // Giả sử backend trả về JSON {"url": "..."}


    /**
     * Cập nhật thông tin địa chỉ giao hàng hiện có.
     * @param addressId ID của địa chỉ cần cập nhật
     * @param address Thông tin địa chỉ mới
     * @return Đối tượng Call chứa Address đã cập nhật
     */
    @retrofit2.http.PUT("addresses/{address_id}/")
    fun updateAddress(@Path("address_id") addressId: Int, @Body address: AddressUpdateRequest): Call<Address>

    /**
     * Tạo đơn hàng mới.
     * @param request Thông tin đơn hàng bao gồm danh sách món, địa chỉ, nhà hàng
     * @return Đối tượng Call chứa OrderResponse
     */
    @POST("orders/")
    fun createOrder(@Body request: OrderCreateRequest): Call<OrderResponse>

    /**
     * Lấy danh sách đơn hàng của người dùng.
     * @param userId ID của người dùng
     * @return Đối tượng Call chứa danh sách OrderResponse
     */
    @GET("users/{user_id}/orders/")
    fun getUserOrders(@Path("user_id") userId: Int): Call<List<OrderResponse>>

    /**
     * Lấy chi tiết đơn hàng bao gồm danh sách món và thông tin nhà hàng.
     * @param orderId ID của đơn hàng
     * @return Đối tượng Call chứa OrderDetailResponse
     */
    @GET("orders/{order_id}/detail")
    fun getOrderDetail(@Path("order_id") orderId: Int): Call<OrderDetailResponse>

    /**
     * Đăng ký hoặc cập nhật FCM token của thiết bị để nhận thông báo đẩy.
     * @param token FCM token của thiết bị
     * @param deviceType Loại thiết bị, mặc định là "android"
     * @return Đối tượng Call chứa DeviceTokenResponse
     */
    @FormUrlEncoded
    @POST("devices/token")
    fun updateDeviceToken(
        @Field("token") token: String,
        @Field("device_type") deviceType: String = "android"
    ): Call<DeviceTokenResponse>

    /**
     * Cập nhật trạng thái của đơn hàng (ví dụ: đang xử lý, đang giao, đã giao).
     * @param orderId ID của đơn hàng
     * @param request Chứa trạng thái mới
     * @return Đối tượng Call chứa OrderResponse đã cập nhật
     */
    @retrofit2.http.PUT("orders/{order_id}/status")
    fun updateOrderStatus(
        @Path("order_id") orderId: Int,
        @Body request: OrderStatusUpdateRequest
    ): Call<OrderResponse>

    /**
     * Tạo đánh giá mới cho đơn hàng (món ăn hoặc nhà hàng).
     * @param request Thông tin đánh giá bao gồm số sao, bình luận, ID đơn hàng
     * @return Đối tượng Call chứa ReviewResponse
     */
    @POST("reviews/")
    fun createReview(@Body request: ReviewCreateRequest): Call<ReviewResponse>




}

