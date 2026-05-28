package com.example.myapp.screens

/**
 * @file start.kt
 * @brief Màn hình khởi động (Splash/Welcome) của ứng dụng giao đồ ăn.
 *
 * Màn hình đầu tiên người dùng nhìn thấy khi mở ứng dụng.
 * Cung cấp hai lựa chọn: Đăng nhập (Sign In) và Đăng ký (Sign Up).
 * Đồng thời khởi tạo RetrofitClient và đồng bộ FCM token cho thông báo đẩy.
 */

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.example.myapp.screens.api.RetrofitClient


// Thêm các bản import này
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.ContextCompat
import com.example.myapp.R
import com.example.myapp.screens.api.FcmTokenRegistrar

/**
 * Activity màn hình khởi động/chào mừng.
 *
 * Chức năng chính:
 * - Hiển thị nút Đăng nhập và Đăng ký
 * - Khởi tạo RetrofitClient để kết nối API
 * - Đồng bộ FCM token để nhận thông báo đẩy
 * - Yêu cầu quyền thông báo trên Android 13+
 */
class start : AppCompatActivity() {
    /** Launcher yêu cầu quyền POST_NOTIFICATIONS cho Android 13+ */
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("FCM_DEBUG", "POST_NOTIFICATIONS granted: $granted")
        }

    /**
     * Khởi tạo màn hình chào mừng.
     *
     * Thiết lập layout, khởi tạo RetrofitClient, đồng bộ FCM token,
     * yêu cầu quyền thông báo và gắn sự kiện click cho nút Đăng nhập/Đăng ký.
     *
     * @param savedInstanceState Trạng thái đã lưu của Activity (nếu có)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start)

        RetrofitClient.init(this)

        requestNotificationPermissionIfNeeded()

        FcmTokenRegistrar.syncCurrentToken(this, "start")

        val btnSignIn: View = findViewById(R.id.btnSignIn)
        btnSignIn.setOnClickListener {
            startActivity(Intent(this, signin::class.java))
        }

        val btnSignUp: View = findViewById(R.id.btnSignUp)
        btnSignUp.setOnClickListener {
            startActivity(Intent(this, signup::class.java))
        }
    }

    /**
     * Yêu cầu quyền thông báo đẩy nếu cần thiết.
     *
     * Chỉ yêu cầu trên Android 13 (TIRAMISU) trở lên khi quyền
     * POST_NOTIFICATIONS chưa được cấp.
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val permissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}