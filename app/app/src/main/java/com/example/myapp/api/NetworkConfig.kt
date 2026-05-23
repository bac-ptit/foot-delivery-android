package com.example.myapp.screens.api


object NetworkConfig {
    // Cấu hình URL cho backend API
    // Thay đổi giá trị này tùy theo môi trường sử dụng


    // Cho Android Emulator (máy ảo)
    private const val EMULATOR_URL = "http://10.0.2.2:8000/"


    // Cho thiết bị thật - thay IP bằng địa chỉ IP của máy tính chạy backend
    // Cách lấy IP: Mở CMD -> gõ ipconfig -> tìm "IPv4 Address"
    private const val DEVICE_URL = "http://192.168.1.100:8000/"  // Thay IP này


    // Chọn URL phù hợp:
    // - true: dùng cho emulator
    // - false: dùng cho thiết bị thật
    private const val USE_EMULATOR = true


    val BASE_URL: String
        get() = if (USE_EMULATOR) EMULATOR_URL else DEVICE_URL


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

