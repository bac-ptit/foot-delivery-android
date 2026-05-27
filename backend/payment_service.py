"""
Module xử lý thanh toán VNPay.

Tạo URL thanh toán VNPay Sandbox để mở trong WebView.
Sử dụng HMAC-SHA512 để ký secure hash.

Cấu hình từ VNPay Test Portal:
- VNP_TMNCODE: Mã merchant
- VNP_HASH_SECRET: Secret key để ký
- VNP_RETURN_URL: URL callback sau thanh toán
"""

import hashlib
import hmac
import urllib.parse
from datetime import datetime

# Thông tin VNPay Sandbox
VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
VNP_TMNCODE = "2HON5OA2"
VNP_HASH_SECRET = "UGDPB7W11M55W6UTQ0N69X36I2VIE0UL"
VNP_RETURN_URL = "http://localhost:8000/vnpay_return"


def generate_vnpay_url(order_id: str, amount: int, ip_address: str) -> str:
    """
    Tạo URL thanh toán VNPay.

    Args:
        order_id: ID đơn hàng (string, format "DH{id}").
        amount: Số tiền (VNĐ).
        ip_address: IP address của client.

    Returns:
        str: URL thanh toán VNPay để mở trong WebView.
    """
    params = {
        "vnp_Version": "2.1.0",
        "vnp_Command": "pay",
        "vnp_TmnCode": VNP_TMNCODE,
        "vnp_Amount": int(amount) * 100,  # VNPay yêu cầu nhân 100
        "vnp_CurrCode": "VND",
        "vnp_TxnRef": order_id,
        "vnp_OrderInfo": f"Thanh toan don hang {order_id}",
        "vnp_OrderType": "other",
        "vnp_Locale": "vn",
        "vnp_ReturnUrl": VNP_RETURN_URL,
        "vnp_IpAddr": ip_address,
        "vnp_CreateDate": datetime.now().strftime("%Y%m%d%H%M%S"),
    }

    # Sắp xếp params theo alphabet
    sorted_params = sorted(params.items())

    # Tạo query string
    query_string = urllib.parse.urlencode(sorted_params)

    # Tạo secure hash bằng HMAC-SHA512
    hash_value = hmac.new(
        VNP_HASH_SECRET.encode(),
        query_string.encode(),
        hashlib.sha512
    ).hexdigest()

    # Tạo URL thanh toán
    payment_url = f"{VNP_URL}?{query_string}&vnp_SecureHash={hash_value}"

    return payment_url
