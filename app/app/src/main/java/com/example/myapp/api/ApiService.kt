package com.example.myapp.screens.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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
 * Yêu cầu đăng ký tài khoản.
 *
 * @property name Họ tên người dùng.
 * @property username Tên đăng nhập (duy nhất).
 * @property email Địa chỉ email.
 * @property phone Số điện thoại.
 * @property role Vai trò: "customer", "shipper", hoặc "admin".
 * @property password Mật khẩu.
 */
data class RegisterRequest(
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val role: String,
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

/**
 * Phản hồi đăng ký từ server.
 *
 * @property name Họ tên.
 * @property email Email.
 * @property phone Số điện thoại.
 * @property role Vai trò.
 * @property id ID người dùng mới tạo.
 */
data class RegisterResponse(
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val id: Int
)

/**
 * Món ăn trong thực đơn.
 *
 * @property id Định danh duy nhất.
 * @property name Tên món ăn.
 * @property image_url URL hình ảnh, có thể null.
 * @property price Giá tiền (VNĐ).
 * @property is_available Còn phục vụ hay không.
 * @property description Mô tả món ăn, có thể null.
 * @property restaurantid ID nhà hàng sở hữu.
 * @property categoryid ID danh mục.
 * @property restaurant_name Tên nhà hàng (kèm khi lấy danh sách).
 * @property reviews Danh sách đánh giá, có thể null.
 * @property avg_rating Điểm đánh giá trung bình, có thể null.
 */
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

/**
 * Chi tiết đánh giá từ người dùng.
 *
 * @property id ID đánh giá.
 * @property rating Số sao (1-5).
 * @property comment Bình luận, có thể null.
 * @property userid ID người đánh giá.
 * @property user_name Tên người đánh giá, có thể null.
 */
data class ReviewDetail(
    val id: Int,
    val rating: Int,
    val comment: String?,
    val userid: Int,
    val user_name: String?
)

/**
 * Nhà hàng trong hệ thống.
 *
 * @property id Định danh duy nhất.
 * @property name Tên nhà hàng.
 * @property image_url URL hình ảnh, có thể null.
 * @property address Địa chỉ, có thể null.
 * @property rating Điểm đánh giá (1-5), có thể null.
 * @property open_time Giờ mở cửa "HH:mm", có thể null.
 * @property close_time Giờ đóng cửa "HH:mm", có thể null.
 * @property phone_number Số điện thoại liên hệ.
 * @property status Trạng thái: "open", "closed", "temporarily_closed".
 * @property description Mô tả nhà hàng, có thể null.
 * @property menu_items Danh sách món ăn thuộc nhà hàng.
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


/**
 * Thông báo người dùng.
 *
 * @property id ID thông báo.
 * @property title Tiêu đề.
 * @property type Loại thông báo.
 * @property content Nội dung.
 * @property isread Đã đọc hay chưa.
 * @property createdat Thời gian tạo (ISO 8601).
 * @property userid ID người nhận.
 * @property orderid ID đơn hàng liên quan, có thể null.
 * @property sessionid ID phiên chat liên quan, có thể null.
 */
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

/**
 * Yêu cầu tạo thông báo mới.
 *
 * @property title Tiêu đề.
 * @property type Loại thông báo.
 * @property content Nội dung.
 * @property isread Trạng thái đọc, mặc định false.
 * @property userid ID người nhận.
 * @property orderid ID đơn hàng, có thể null.
 * @property sessionid ID phiên chat, có thể null.
 */
data class NotificationCreate(
    val title: String,
    val type: String,
    val content: String,
    val isread: Boolean = false,
    val userid: Int,
    val orderid: Int? = null,
    val sessionid: Int? = null
)

/**
 * Câu hỏi thường gặp (FAQ).
 *
 * @property id ID câu hỏi.
 * @property question Nội dung câu hỏi.
 * @property answer Câu trả lời.
 * @property isactive Có hiển thị hay không.
 */
data class FAQ(
    val id: Int,
    val question: String,
    val answer: String,
    val isactive: Boolean
)


/**
 * Yêu cầu gửi tin nhắn chat.
 *
 * @property message Nội dung tin nhắn.
 */
data class ChatMessageRequest(
    val message: String
)

/**
 * Phản hồi tin nhắn chat từ server.
 *
 * @property message Nội dung phản hồi.
 * @property id ID tin nhắn.
 * @property senderrole Vai trò người gửi: "user" hoặc "bot".
 * @property sentat Thời gian gửi (ISO 8601).
 * @property sessionid ID phiên chat.
 */
data class ChatMessageResponse(
    val message: String,
    val id: Int,
    val senderrole: String,
    val sentat: String,
    val sessionid: Int
)

/**
 * Địa chỉ giao hàng.
 *
 * @property id ID địa chỉ.
 * @property detail Địa chỉ chi tiết.
 * @property phone Số điện thoại, có thể null.
 * @property userid ID chủ sở hữu.
 */
data class Address(
    val id: Int,
    val detail: String,
    val phone: String?,
    val userid: Int
)

/**
 * Yêu cầu tạo địa chỉ mới.
 *
 * @property detail Địa chỉ chi tiết.
 * @property phone Số điện thoại, có thể null.
 * @property userid ID chủ sở hữu.
 */
data class AddressCreateRequest(
    val detail: String,
    val phone: String?,
    val userid: Int
)

/**
 * Yêu cầu cập nhật địa chỉ.
 *
 * @property detail Địa chỉ mới, có thể null nếu không thay đổi.
 * @property phone Số điện thoại mới, có thể null nếu không thay đổi.
 */
data class AddressUpdateRequest(
    val detail: String?,
    val phone: String?
)

/**
 * Phản hồi từ API tạo thanh toán VNPay.
 *
 * @property payment_url URL thanh toán VNPay để mở trong WebView.
 */
data class VNPayResponse(
    val payment_url: String
)

/**
 * Phản hồi khi cập nhật FCM token.
 *
 * @property message Thông báo kết quả.
 */
data class DeviceTokenResponse(
    val message: String
)

/**
 * Tổng quan hồ sơ người dùng.
 *
 * @property user_id ID người dùng.
 * @property user_name Tên hiển thị.
 * @property points Điểm tích lũy (tính từ total_spent / 10000).
 * @property delivered_orders Số đơn đã giao thành công.
 * @property total_spent Tổng chi tiêu (VNĐ).
 */
data class UserProfileSummary(
    val user_id: Int,
    val user_name: String,
    val points: Int,
    val delivered_orders: Int,
    val total_spent: Int
)

/**
 * Chi tiết một món trong đơn hàng (khi tạo đơn).
 *
 * @property quantity Số lượng.
 * @property price Đơn giá (VNĐ).
 * @property menuitemid ID món ăn.
 */
data class OrderItemRequest(
    val quantity: Int,
    val price: Int,
    val menuitemid: Int
)

/**
 * Yêu cầu tạo đơn hàng mới.
 *
 * @property status Trạng thái ban đầu: "pending".
 * @property preorderdate Ngày giao dự kiến (đơn đặt trước), có thể null.
 * @property preordertime Giờ giao dự kiến (đơn đặt trước), có thể null.
 * @property totalprice Tổng tiền (VNĐ).
 * @property restaurantid ID nhà hàng.
 * @property addressid ID địa chỉ giao hàng.
 * @property userid ID người đặt.
 * @property order_items Danh sách món ăn trong đơn.
 */
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

/**
 * Phản hồi đơn hàng từ server.
 *
 * @property id ID đơn hàng.
 * @property status Trạng thái: "pending", "paid", "confirmed", "delivering", "completed", "cancelled".
 * @property createdat Thời gian tạo (ISO 8601).
 * @property preorderdate Ngày giao dự kiến, có thể null.
 * @property preordertime Giờ giao dự kiến, có thể null.
 * @property totalprice Tổng tiền (VNĐ).
 * @property restaurantid ID nhà hàng.
 * @property addressid ID địa chỉ giao hàng.
 * @property userid ID người đặt.
 */
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

/**
 * Chi tiết một món trong đơn hàng (khi xem chi tiết).
 *
 * @property id ID OrderItem.
 * @property quantity Số lượng.
 * @property price Đơn giá (VNĐ).
 * @property menuitemid ID món ăn.
 * @property menuitem_name Tên món ăn, có thể null.
 * @property image_url URL hình ảnh, có thể null.
 */
data class OrderItemDetailResponse(
    val id: Int,
    val quantity: Int,
    val price: Int,
    val menuitemid: Int,
    val menuitem_name: String?,
    val image_url: String? = null
)

/**
 * Chi tiết đầy đủ đơn hàng (kèm nhà hàng, địa chỉ, danh sách món).
 *
 * @property id ID đơn hàng.
 * @property status Trạng thái.
 * @property createdat Thời gian tạo.
 * @property preorderdate Ngày giao dự kiến, có thể null.
 * @property preordertime Giờ giao dự kiến, có thể null.
 * @property totalprice Tổng tiền (VNĐ).
 * @property restaurantid ID nhà hàng.
 * @property addressid ID địa chỉ.
 * @property userid ID người đặt.
 * @property restaurant_name Tên nhà hàng, có thể null.
 * @property address_detail Địa chỉ chi tiết, có thể null.
 * @property order_items Danh sách món ăn trong đơn.
 */
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

/**
 * Yêu cầu cập nhật trạng thái đơn hàng.
 *
 * @property status Trạng thái mới: "paid", "confirmed", "delivering", "completed", "cancelled".
 */
data class OrderStatusUpdateRequest(
    val status: String
)

/**
 * Yêu cầu tạo đánh giá.
 *
 * @property rating Số sao (1-5).
 * @property comment Bình luận, có thể null.
 * @property orderid ID đơn hàng được đánh giá.
 * @property userid ID người đánh giá.
 * @property menuitemid ID món ăn, có thể null (auto-fill từ đơn).
 * @property restaurantid ID nhà hàng, có thể null (auto-fill từ đơn).
 */
data class ReviewCreateRequest(
    val rating: Int,
    val comment: String? = null,
    val orderid: Int,
    val userid: Int,
    val menuitemid: Int? = null,
    val restaurantid: Int? = null
)

/**
 * Phản hồi đánh giá từ server.
 *
 * @property id ID đánh giá.
 * @property rating Số sao.
 * @property comment Bình luận, có thể null.
 * @property orderid ID đơn hàng.
 * @property menuitemid ID món ăn, có thể null.
 * @property restaurantid ID nhà hàng, có thể null.
 * @property userid ID người đánh giá.
 */
data class ReviewResponse(
    val id: Int,
    val rating: Int,
    val comment: String?,
    val orderid: Int,
    val menuitemid: Int?,
    val restaurantid: Int?,
    val userid: Int
)

/**
 * Mã giảm giá / khuyến mãi.
 *
 * @property id ID khuyến mãi.
 * @property code Mã giảm giá.
 * @property discounttype Loại giảm giá: "percentage" hoặc "fixed".
 * @property discountvalue Giá trị giảm.
 * @property expiredate Ngày hết hạn (ISO 8601).
 * @property minordervalue Giá trị đơn tối thiểu.
 * @property status Trạng thái: "active", "expired".
 */
data class PromotionResponse(
    val id: Int,
    val code: String,
    val discounttype: String,
    val discountvalue: Int,
    val expiredate: String,
    val minordervalue: Int,
    val status: String
)


/**
 * Định nghĩa các endpoint API cho ứng dụng.
 *
 * Tất cả request sử dụng Retrofit với Call<T> và enqueue().
 * Token xác thực được tự động đính kèm qua OkHttp Interceptor trong [RetrofitClient].
 */
interface ApiService {

    /**
     * Đăng nhập và lấy access token.
     *
     * @param username Tên đăng nhập.
     * @param password Mật khẩu.
     * @param grantType Loại grant, mặc định "password".
     * @return [LoginResponse] chứa access_token.
     */
    @FormUrlEncoded
    @POST("token")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password"
    ): Call<LoginResponse>

    /**
     * Đăng ký tài khoản mới.
     *
     * @param request Thông tin đăng ký.
     * @return [RegisterResponse] chứa thông tin người dùng mới.
     */
    @POST("users/")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    /**
     * Lấy thông tin người dùng hiện tại (dựa vào token).
     *
     * @return [RegisterResponse] chứa thông tin user.
     */
    @GET("users/me/")
    fun getCurrentUser(): Call<RegisterResponse>

    /**
     * Lấy tổng quan hồ sơ người dùng (điểm, đơn đã giao, tổng chi tiêu).
     *
     * @param userId ID người dùng.
     * @return [UserProfileSummary] chứa điểm tích lũy.
     */
    @GET("users/{user_id}/profile-summary")
    fun getProfileSummary(@Path("user_id") userId: Int): Call<UserProfileSummary>

    /**
     * Lấy danh sách tất cả nhà hàng.
     *
     * @return Danh sách [Restaurant].
     */
    @GET("restaurants/")
    fun getRestaurants(): Call<List<Restaurant>>


    /**
     * Tìm kiếm nhà hàng theo tên.
     *
     * @param name Từ khóa tìm kiếm.
     * @return Danh sách [Restaurant] khớp với từ khóa.
     */
    @GET("restaurants/search/")
    fun searchRestaurantsByName(@retrofit2.http.Query("name") name: String): Call<List<Restaurant>>

    /**
     * Lấy chi tiết nhà hàng kèm danh sách món ăn.
     *
     * @param restaurantId ID nhà hàng.
     * @return [Restaurant] với menu_items.
     */
    @GET("restaurants/{restaurant_id}/")
    fun getRestaurant(@Path("restaurant_id") restaurantId: Int): Call<Restaurant>

    /**
     * Lấy danh sách thông báo của người dùng.
     *
     * @param userId ID người dùng.
     * @return Danh sách [Notification].
     */
    @GET("users/{user_id}/notifications/")
    fun getUserNotifications(@Path("user_id") userId: Int): Call<List<Notification>>

    /**
     * Tạo thông báo mới.
     *
     * @param notification Thông tin thông báo.
     * @return [Notification] đã tạo.
     */
    @POST("notifications/")
    fun createNotification(@Body notification: NotificationCreate): Call<Notification>

    /**
     * Đánh dấu thông báo đã đọc.
     *
     * @param notificationId ID thông báo.
     */
    @retrofit2.http.PUT("notifications/{notification_id}/read")
    fun markNotificationAsRead(@Path("notification_id") notificationId: Int): Call<Void>

    /**
     * Lấy tất cả món ăn (kèm đánh giá).
     *
     * @return Danh sách [MenuItem] với reviews và avg_rating.
     */
    @GET("menu-items/")
    fun getAllMenuItems(): Call<List<MenuItem>>

    /**
     * Lấy chi tiết một món ăn (kèm đánh giá).
     *
     * @param menuItemId ID món ăn.
     * @return [MenuItem] với reviews và avg_rating.
     */
    @GET("menu-items/{menu_item_id}/")
    fun getMenuItemDetail(@Path("menu_item_id") menuItemId: Int): Call<MenuItem>

    /**
     * Tìm kiếm món ăn theo tên.
     *
     * @param name Từ khóa tìm kiếm.
     * @return Danh sách [MenuItem] khớp.
     */
    @GET("menu-items/search/")
    fun searchMenuItemsByName(@retrofit2.http.Query("name") name: String): Call<List<MenuItem>>

    /**
     * Lấy danh sách mã giảm giá đang hoạt động.
     *
     * @return Danh sách [PromotionResponse].
     */
    @GET("promotions/")
    fun getPromotions(): Call<List<PromotionResponse>>

    /**
     * Tìm kiếm món ăn theo tên danh mục.
     *
     * @param categoryName Tên danh mục.
     * @return Danh sách [MenuItem] thuộc danh mục.
     */
    @GET("menu-items/category/search/")
    fun searchMenuItemsByCategory(@retrofit2.http.Query("category_name") categoryName: String): Call<List<MenuItem>>

    /**
     * Lấy danh sách câu hỏi thường gặp.
     *
     * @return Danh sách [FAQ].
     */
    @GET("faqs/")
    fun getFAQs(): Call<List<FAQ>>

    /**
     * Gửi tin nhắn chatbot.
     *
     * @param request Nội dung tin nhắn.
     * @param sessionId ID phiên chat, có thể null để tạo phiên mới.
     * @return [ChatMessageResponse] chứa phản hồi từ bot.
     */
    @POST("chat/")
    fun sendMessage(@Body request: ChatMessageRequest, @retrofit2.http.Query("session_id") sessionId: Int? = null): Call<ChatMessageResponse>

    /**
     * Lấy danh sách địa chỉ của người dùng.
     *
     * @param userId ID người dùng.
     * @return Danh sách [Address].
     */
    @GET("users/{user_id}/addresses/")
    fun getUserAddresses(@Path("user_id") userId: Int): Call<List<Address>>

    /**
     * Tạo địa chỉ mới.
     *
     * @param address Thông tin địa chỉ.
     * @return [Address] đã tạo.
     */
    @POST("addresses/")
    fun createAddress(@Body address: AddressCreateRequest): Call<Address>

    /**
     * Tạo URL thanh toán VNPay.
     *
     * @param amount Số tiền (VNĐ).
     * @param orderId ID đơn hàng.
     * @return [VNPayResponse] chứa payment_url.
     */
    @GET("create-payment")
    fun getVNPayUrl(
        @retrofit2.http.Query("amount") amount: Int,
        @retrofit2.http.Query("order_id") orderId: String
    ): Call<VNPayResponse>

    /**
     * Cập nhật địa chỉ.
     *
     * @param addressId ID địa chỉ cần cập nhật.
     * @param address Thông tin mới.
     * @return [Address] đã cập nhật.
     */
    @retrofit2.http.PUT("addresses/{address_id}/")
    fun updateAddress(@Path("address_id") addressId: Int, @Body address: AddressUpdateRequest): Call<Address>

    /**
     * Tạo đơn hàng mới.
     *
     * @param request Thông tin đơn hàng kèm danh sách món.
     * @return [OrderResponse] đơn hàng đã tạo.
     */
    @POST("orders/")
    fun createOrder(@Body request: OrderCreateRequest): Call<OrderResponse>

    /**
     * Lấy danh sách đơn hàng của người dùng.
     *
     * @param userId ID người dùng.
     * @return Danh sách [OrderResponse].
     */
    @GET("users/{user_id}/orders/")
    fun getUserOrders(@Path("user_id") userId: Int): Call<List<OrderResponse>>

    /**
     * Lấy chi tiết đơn hàng (kèm nhà hàng, địa chỉ, danh sách món).
     *
     * @param orderId ID đơn hàng.
     * @return [OrderDetailResponse] đầy đủ thông tin.
     */
    @GET("orders/{order_id}/detail")
    fun getOrderDetail(@Path("order_id") orderId: Int): Call<OrderDetailResponse>

    /**
     * Cập nhật FCM token cho thiết bị.
     *
     * @param token FCM token từ Firebase.
     * @param deviceType Loại thiết bị, mặc định "android".
     * @return [DeviceTokenResponse].
     */
    @FormUrlEncoded
    @POST("devices/token")
    fun updateDeviceToken(
        @Field("token") token: String,
        @Field("device_type") deviceType: String = "android"
    ): Call<DeviceTokenResponse>

    /**
     * Cập nhật trạng thái đơn hàng.
     *
     * @param orderId ID đơn hàng.
     * @param request Trạng thái mới.
     * @return [OrderResponse] đã cập nhật.
     */
    @retrofit2.http.PUT("orders/{order_id}/status")
    fun updateOrderStatus(
        @Path("order_id") orderId: Int,
        @Body request: OrderStatusUpdateRequest
    ): Call<OrderResponse>

    /**
     * Tạo đánh giá cho đơn hàng已完成.
     *
     * @param request Thông tin đánh giá (rating, comment, orderid, userid).
     * @return [ReviewResponse] đánh giá đã tạo.
     */
    @POST("reviews/")
    fun createReview(@Body request: ReviewCreateRequest): Call<ReviewResponse>
}

