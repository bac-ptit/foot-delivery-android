package com.example.myapp.screens.api

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object FcmTokenRegistrar {
    private const val TAG = "FCM_TOKEN"
    private const val PREF_NAME = "user_prefs"
    private const val KEY_PENDING_FCM_TOKEN = "pending_fcm_token"
    private const val KEY_SYNCED_FCM_TOKEN = "synced_fcm_token"

    fun cacheToken(context: Context, token: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_FCM_TOKEN, token)
            .apply()
    }

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