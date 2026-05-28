"""
Module: notification_service.py

Service gửi thông báo đẩy qua Firebase Cloud Messaging (FCM).
Lưu thông báo vào database và gửi push notification đến tất cả thiết bị của người dùng.
"""

from firebase_admin import messaging
import firebase_admin
from firebase_utils import init_firebase
from sqlalchemy.orm import Session
import models

def send_fcm_notification(token: str, title: str, body: str, data: dict = None):
    """Gửi thông báo tới một thiết bị Android cụ thể qua FCM."""
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

def notify_user(db: Session, user_id: int, title: str, body: str, order_id: int = None):
    """Gửi thông báo cho tất cả thiết bị của một người dùng."""
    # 1. Lưu thông báo vào bảng Notification trong DB
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

    # 2. Lấy danh sách FCM Token của người dùng đó
    devices = db.query(models.UserDevice).filter(models.UserDevice.userid == user_id).all()
    
    # 3. Gửi thông báo thực tới điện thoại
    for device in devices:
        send_fcm_notification(
            token=device.device_token,
            title=title,
            body=body,
            data={"order_id": str(order_id)} if order_id else None
        )
