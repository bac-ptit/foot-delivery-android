package com.example.myapp.screens.api

/**
 * @file FcmTokenRegistrar.kt
 * @brief Quản lý việc đăng ký và đồng bộ FCM token cho thông báo đẩy.
 *        Hỗ trợ cache token trước khi đăng nhập và đồng bộ tự động sau khi đăng nhập.
 */

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/** Singleton quản lý việc cache và đồng bộ FCM token cho thông báo đẩy. */
object FcmTokenRegistrar {
    /** Tag dùng cho log hệ thống. */
    private const val TAG = "FCM_TOKEN"
    /** Tên file SharedPreferences lưu trữ token. */
    private const val PREF_NAME = "user_prefs"
    /** Key lưu token FCM đang chờ đồng bộ lên server. */
    private const val KEY_PENDING_FCM_TOKEN = "pending_fcm_token"
    /** Key lưu token FCM đã đồng bộ thành công lên server. */
    private const val KEY_SYNCED_FCM_TOKEN = "synced_fcm_token"

    /**
     * Lưu FCM token vào SharedPreferences để chờ đồng bộ sau khi đăng nhập.
     * @param context Context của ứng dụng
     * @param token FCM token cần cache
     */
    fun cacheToken(context: Context, token: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_FCM_TOKEN, token)
            .apply()
    }

    /**
     * Đồng bộ FCM token hiện tại. Nếu chưa có access token thì chỉ cache,
     * nếu đã đăng nhập thì đồng bộ lên server ngay.
     * @param context Context của ứng dụng
     * @param source Nguồn gọi (dùng cho log), ví dụ: "login", "app_start"
     */
    fun syncCurrentToken(context: Context, source: String) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", null)

        if (accessToken.isNullOrBlank()) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    cacheToken(context, token)
                    Log.d(TAG, "Cached FCM token from $source while waiting for login")
                }
                .addOnFailureListener { error ->
                    Log.w(TAG, "Failed to get FCM token from $source", error)
                }
            return
        }

        syncPendingToken(context, source)
    }

    /**
     * Đồng bộ token đang chờ lên server. Nếu chưa có token pending,
     * sẽ lấy token mới từ Firebase và gửi lên server.
     * @param context Context của ứng dụng
     * @param source Nguồn gọi (dùng cho log)
     */
    fun syncPendingToken(context: Context, source: String) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString("access_token", null)

        if (accessToken.isNullOrBlank()) {
            Log.d(TAG, "Skip syncing token from $source because access token is missing")
            return
        }

        val pendingToken = sharedPref.getString(KEY_PENDING_FCM_TOKEN, null)
        if (pendingToken.isNullOrBlank()) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    cacheToken(context, token)
                    pushToken(context, token, source)
                }
                .addOnFailureListener { error ->
                    Log.w(TAG, "Failed to get FCM token from $source", error)
                }
            return
        }

        if (pendingToken == sharedPref.getString(KEY_SYNCED_FCM_TOKEN, null)) {
            Log.d(TAG, "FCM token already synced from $source")
            return
        }

        pushToken(context, pendingToken, source)
    }

    /**
     * Gửi FCM token lên server thông qua API và lưu trạng thái đã đồng bộ.
     * @param context Context của ứng dụng
     * @param token FCM token cần gửi lên server
     * @param source Nguồn gọi (dùng cho log)
     */
    private fun pushToken(context: Context, token: String, source: String) {
        RetrofitClient.apiService.updateDeviceToken(token, "android")
            .enqueue(object : Callback<DeviceTokenResponse> {
                override fun onResponse(
                    call: Call<DeviceTokenResponse>,
                    response: Response<DeviceTokenResponse>
                ) {
                    if (response.isSuccessful) {
                        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString(KEY_SYNCED_FCM_TOKEN, token)
                            .apply()
                        Log.d(TAG, "Synced FCM token from $source")
                    } else {
                        Log.w(TAG, "Failed to sync FCM token from $source: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<DeviceTokenResponse>, t: Throwable) {
                    Log.w(TAG, "Failed to sync FCM token from $source", t)
                }
            })
    }
}