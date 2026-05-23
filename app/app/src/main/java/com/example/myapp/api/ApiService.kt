package com.example.myapp.screens.api


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


data class LoginRequest(
    val username: String,
    val password: String
)


data class RegisterRequest(
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val role: String,
    val password: String
)


data class LoginResponse(
    val access_token: String,
    val token_type: String
)


data class RegisterResponse(
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val id: Int
)


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

data class ReviewDetail(
    val id: Int,
    val rating: Int,
    val comment: String?,
    val userid: Int,
    val user_name: String?
)


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


data class NotificationCreate(
    val title: String,
    val type: String,
    val content: String,
    val isread: Boolean = false,
    val userid: Int,
    val orderid: Int? = null,
    val sessionid: Int? = null
)


data class FAQ(
    val id: Int,
    val question: String,
    val answer: String,
    val isactive: Boolean
)


data class ChatMessageRequest(
    val message: String
)


data class ChatMessageResponse(
    val message: String,
    val id: Int,
    val senderrole: String,
    val sentat: String,
    val sessionid: Int
)


data class Address(
    val id: Int,
    val detail: String,
    val phone: String?,
    val userid: Int
)


data class AddressCreateRequest(
    val detail: String,
    val phone: String?,
    val userid: Int
)


data class AddressUpdateRequest(
    val detail: String?,
    val phone: String?
)


data class VNPayResponse(
    val payment_url: String
)

data class DeviceTokenResponse(
    val message: String
)

data class UserProfileSummary(
    val user_id: Int,
    val user_name: String,
    val points: Int,
    val delivered_orders: Int,
    val total_spent: Int
)

data class OrderItemRequest(
    val quantity: Int,
    val price: Int,
    val menuitemid: Int
)

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

data class OrderItemDetailResponse(
    val id: Int,
    val quantity: Int,
    val price: Int,
    val menuitemid: Int,
    val menuitem_name: String?,
    val image_url: String? = null
)

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

data class OrderStatusUpdateRequest(
    val status: String
)

data class ReviewCreateRequest(
    val rating: Int,
    val comment: String? = null,
    val orderid: Int,
    val userid: Int,
    val menuitemid: Int? = null,
    val restaurantid: Int? = null
)

data class ReviewResponse(
    val id: Int,
    val rating: Int,
    val comment: String?,
    val orderid: Int,
    val menuitemid: Int?,
    val restaurantid: Int?,
    val userid: Int
)

data class PromotionResponse(
    val id: Int,
    val code: String,
    val discounttype: String,
    val discountvalue: Int,
    val expiredate: String,
    val minordervalue: Int,
    val status: String
)


interface ApiService {
    @FormUrlEncoded
    @POST("token")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password"


    ): Call<LoginResponse>


    @POST("users/")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>


    @GET("users/me/")
    fun getCurrentUser(): Call<RegisterResponse>

    @GET("users/{user_id}/profile-summary")
    fun getProfileSummary(@Path("user_id") userId: Int): Call<UserProfileSummary>


    @GET("restaurants/")
    fun getRestaurants(): Call<List<Restaurant>>


    @GET("restaurants/search/")
    fun searchRestaurantsByName(@retrofit2.http.Query("name") name: String): Call<List<Restaurant>>


    @GET("restaurants/{restaurant_id}/")
    fun getRestaurant(@Path("restaurant_id") restaurantId: Int): Call<Restaurant>


    @GET("users/{user_id}/notifications/")
    fun getUserNotifications(@Path("user_id") userId: Int): Call<List<Notification>>


    @POST("notifications/")
    fun createNotification(@Body notification: NotificationCreate): Call<Notification>


    @retrofit2.http.PUT("notifications/{notification_id}/read")
    fun markNotificationAsRead(@Path("notification_id") notificationId: Int): Call<Void>


    @GET("menu-items/")
    fun getAllMenuItems(): Call<List<MenuItem>>

    @GET("menu-items/{menu_item_id}/")
    fun getMenuItemDetail(@Path("menu_item_id") menuItemId: Int): Call<MenuItem>

    @GET("menu-items/search/")
    fun searchMenuItemsByName(@retrofit2.http.Query("name") name: String): Call<List<MenuItem>>

    @GET("promotions/")
    fun getPromotions(): Call<List<PromotionResponse>>


    @GET("menu-items/category/search/")
    fun searchMenuItemsByCategory(@retrofit2.http.Query("category_name") categoryName: String): Call<List<MenuItem>>


    @GET("faqs/")
    fun getFAQs(): Call<List<FAQ>>


    @POST("chat/")
    fun sendMessage(@Body request: ChatMessageRequest, @retrofit2.http.Query("session_id") sessionId: Int? = null): Call<ChatMessageResponse>


    @GET("users/{user_id}/addresses/")
    fun getUserAddresses(@Path("user_id") userId: Int): Call<List<Address>>


    @POST("addresses/")
    fun createAddress(@Body address: AddressCreateRequest): Call<Address>




    // ... trong interface ApiService
    @GET("create-payment") // Thay bằng đường dẫn API thật của bạn
    fun getVNPayUrl(
        @retrofit2.http.Query("amount") amount: Int,
        @retrofit2.http.Query("order_id") orderId: String
    ): Call<VNPayResponse> // Giả sử backend trả về JSON {"url": "..."}


    @retrofit2.http.PUT("addresses/{address_id}/")
    fun updateAddress(@Path("address_id") addressId: Int, @Body address: AddressUpdateRequest): Call<Address>

    @POST("orders/")
    fun createOrder(@Body request: OrderCreateRequest): Call<OrderResponse>

    @GET("users/{user_id}/orders/")
    fun getUserOrders(@Path("user_id") userId: Int): Call<List<OrderResponse>>

    @GET("orders/{order_id}/detail")
    fun getOrderDetail(@Path("order_id") orderId: Int): Call<OrderDetailResponse>

    @FormUrlEncoded
    @POST("devices/token")
    fun updateDeviceToken(
        @Field("token") token: String,
        @Field("device_type") deviceType: String = "android"
    ): Call<DeviceTokenResponse>

    @retrofit2.http.PUT("orders/{order_id}/status")
    fun updateOrderStatus(
        @Path("order_id") orderId: Int,
        @Body request: OrderStatusUpdateRequest
    ): Call<OrderResponse>

    @POST("reviews/")
    fun createReview(@Body request: ReviewCreateRequest): Call<ReviewResponse>




}

