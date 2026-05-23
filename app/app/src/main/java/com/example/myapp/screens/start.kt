package com.example.myapp.screens

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

class start : AppCompatActivity() {
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("FCM_DEBUG", "POST_NOTIFICATIONS granted: $granted")
        }

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