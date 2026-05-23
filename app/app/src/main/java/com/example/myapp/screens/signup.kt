package com.example.myapp.screens

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import com.example.myapp.R
import com.example.myapp.screens.api.RegisterRequest
import com.example.myapp.screens.api.RegisterResponse
import com.example.myapp.screens.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class signup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

//        click btnBack
        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

//        click btnSignIn
        val tvLogin: TextView = findViewById(R.id.tvLogin)
        tvLogin.setOnClickListener {
            val intent = Intent(this, signin::class.java)
            startActivity(intent)
        }

        //        click btnSignUp
        val btnSignUp: View = findViewById(R.id.btn_signup)
        btnSignUp.setOnClickListener {
            val etUsername: EditText = findViewById(R.id.et_username)
            val etPassword: EditText = findViewById(R.id.et_password)
            val etRePassword: EditText = findViewById(R.id.et_re_password)

            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val rePassword = etRePassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != rePassword) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gọi API đăng ký
            val registerRequest = RegisterRequest(
                name = username,
                username = username,
                email = "$username@example.com",
                phone = "0000000000",
                role = "user",
                password = password
            )
            RetrofitClient.apiService.register(registerRequest).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    if (response.isSuccessful) {
                        val registerResponse = response.body()
                        if (registerResponse != null) {
                            // Đăng ký thành công
                            Toast.makeText(this@signup, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                            // Chuyển đến màn hình đăng nhập
                            val intent = Intent(this@signup, signin::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Đăng ký thất bại
                            Toast.makeText(this@signup, "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Lấy thông báo lỗi từ server
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = if (errorBody != null) {
                            try {
                                val jsonObject = org.json.JSONObject(errorBody)
                                if (jsonObject.has("detail")) {
                                    jsonObject.getString("detail")
                                } else {
                                    "Đăng ký thất bại"
                                }
                            } catch (e: Exception) {
                                "Đăng ký thất bại"
                            }
                        } else {
                            "Đăng ký thất bại"
                        }
                        Toast.makeText(this@signup, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Toast.makeText(this@signup, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
