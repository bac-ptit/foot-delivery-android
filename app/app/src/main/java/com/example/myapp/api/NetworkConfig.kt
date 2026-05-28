package com.example.myapp.screens.api

/**
 * @file NetworkConfig.kt
 * @brief Cấu hình URL cơ sở (base URL) cho kết nối API backend.
 *        Hỗ trợ chuyển đổi giữa môi trường emulator và thiết bị thật.
 */

/** Đối tượng cấu hình địa chỉ mạng cho ứng dụng. */
object NetworkConfig {
    // Cấu hình URL cho backend API
    // Thay đổi giá trị này tùy theo môi trường sử dụng


    /** URL backend khi chạy trên Android Emulator (dùng 10.0.2.2 để truy cập localhost của máy host). */
    // Cho Android Emulator (máy ảo)
    private const val EMULATOR_URL = "http://10.0.2.2:8000/"


    /** URL backend khi chạy trên thiết bị thật, cần thay đổi IP theo mạng cục bộ. */
    // Cho thiết bị thật - thay IP bằng địa chỉ IP của máy tính chạy backend
    // Cách lấy IP: Mở CMD -> gõ ipconfig -> tìm "IPv4 Address"
    private const val DEVICE_URL = "http://192.168.1.100:8000/"  // Thay IP này


    /** Cờ chọn môi trường: true = emulator, false = thiết bị thật. */
    // Chọn URL phù hợp:
    // - true: dùng cho emulator
    // - false: dùng cho thiết bị thật
    private const val USE_EMULATOR = true


    /** URL cơ sở được chọn dựa trên cờ USE_EMULATOR. */
    val BASE_URL: String
        get() = if (USE_EMULATOR) EMULATOR_URL else DEVICE_URL


    /**
     * Lấy địa chỉ IPv4 cục bộ của máy tính đang chạy.
     * Hữu ích để cấu hình URL backend cho thiết bị thật.
     * @return Chuỗi địa chỉ IPv4 hoặc "unknown" nếu không tìm thấy
     */
    // Helper function để lấy IP máy tính (chạy trên máy tính)
    fun getLocalIpAddress(): String {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        return address.hostAddress ?: "unknown"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "unknown"
    }
}

