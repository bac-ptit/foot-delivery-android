"""
Module xử lý push notification qua Firebase Cloud Messaging (FCM).

Cung cấp 2 hàm chính:
- send_fcm_notification(): Gửi notification đến 1 thiết bị cụ thể
- notify_user(): Lưu notification vào DB + gửi đến tất cả thiết bị của user

Sử dụng Firebase Admin SDK để gửi FCM.
"""

from firebase_admin import messaging
import firebase_admin
from firebase_utils import init_firebase
from sqlalchemy.orm import Session
import models


def send_fcm_notification(token: str, title: str, body: str, data: dict = None) -> bool:
    """
    Gửi push notification đến một thiết bị Android qua FCM.

    Args:
        token: FCM device token.
        title: Tiêu đề thông báo.
        body: Nội dung thông báo.
        data: Dữ liệu thêm (optional), dạng dict.

    Returns:
        True nếu gửi thành công, False nếu lỗi.
    """
    if not firebase_admin._apps:
        init_firebase()

    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body,
        ),
        data=data,
        token=token,
    )

    try:
        response = messaging.send(message)
        print(f"Successfully sent message: {response}")
        return True
    except Exception as e:
        print(f"Error sending FCM message: {e}")
        return False


def notify_user(
    db: Session,
    user_id: int,
    title: str,
    body: str,
    order_id: int = None
) -> None:
    """
    Gửi thông báo cho tất cả thiết bị của một người dùng.

    Quy trình:
    1. Lưu thông báo vào bảng Notification trong DB
    2. Lấy danh sách FCM token của user từ bảng UserDevice
    3. Gửi push notification đến từng thiết bị

    Args:
        db: Database session.
        user_id: ID người nhận.
        title: Tiêu đề thông báo.
        body: Nội dung thông báo.
        order_id: ID đơn hàng liên quan (optional).
    """
    # 1. Lưu vào DB
    db_notification = models.Notification(
        title=title,
        content=body,
        type="order_update",
        userid=user_id,
        orderid=order_id,
        isread=False
    )
    db.add(db_notification)
    db.commit()

    # 2. Lấy danh sách thiết bị
    devices = db.query(models.UserDevice).filter(
        models.UserDevice.userid == user_id
    ).all()

    # 3. Gửi push notification
    for device in devices:
        send_fcm_notification(
            token=device.device_token,
            title=title,
            body=body,
            data={"order_id": str(order_id)} if order_id else None
        )
