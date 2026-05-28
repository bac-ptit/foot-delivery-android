package com.example.myapp.screens.api

/**
 * @file RetrofitClient.kt
 * @brief Singleton quản lý Retrofit client với interceptor xác thực tự động.
 *        Tự động gắn token Authorization vào mọi request HTTP.
 */

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Singleton quản lý instance Retrofit và OkHttpClient với interceptor xác thực. */
object RetrofitClient {
    /** URL cơ sở được lấy từ cấu hình NetworkConfig. */
    // Sử dụng NetworkConfig để lấy BASE_URL
    // Thay đổi USE_EMULATOR trong NetworkConfig.kt tùy theo môi trường
    private val BASE_URL = NetworkConfig.BASE_URL
    /** Context ứng dụng dùng để truy cập SharedPreferences cho token xác thực. */
    private var appContext: Context? = null

    /**
     * Khởi tạo RetrofitClient với context ứng dụng.
     * Phải được gọi trước khi sử dụng apiService.
     * @param context Context của ứng dụng
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /** Interceptor tự động gắn header Authorization Bearer token vào mọi request. */
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val sharedPref = appContext?.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPref?.getString("access_token", null)
        
        if (token != null) {
            val newRequest = request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(request)
        }
    }

    /** OkHttpClient được cấu hình với interceptor xác thực và thời gian chờ phù hợp. */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Instance Retrofit được khởi tạo lazy với Gson converter và OkHttpClient. */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** Instance ApiService được khởi tạo lazy, dùng để gọi các endpoint API. */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
