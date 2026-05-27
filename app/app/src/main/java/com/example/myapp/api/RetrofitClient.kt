package com.example.myapp.screens.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client với xác thực tự động.
 *
 * Đọc JWT token từ SharedPreferences ("user_prefs") và đính kèm
 * vào mọi request qua header "Authorization: Bearer {token}".
 *
 * Sử dụng:
 * - Khởi tạo: `RetrofitClient.init(context)` trong Application.onCreate()
 * - Gọi API: `RetrofitClient.apiService.someMethod()`
 *
 * Cấu hình timeout:
 * - Connect: 30 giây
 * - Read: 60 giây
 * - Write: 30 giây
 *
 * @see ApiService
 * @see NetworkConfig
 */
object RetrofitClient {
    // Sử dụng NetworkConfig để lấy BASE_URL
    // Thay đổi USE_EMULATOR trong NetworkConfig.kt tùy theo môi trường
    private val BASE_URL = NetworkConfig.BASE_URL
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

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

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
