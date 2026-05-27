"""
Module xử lý Firebase Storage (upload ảnh).

Sử dụng Firebase Admin SDK để upload ảnh lên Firebase Storage.
Ảnh được lưu với tên unique (UUID) và trả về public URL.

Cấu hình từ biến môi trường:
- FIREBASE_CREDENTIALS_PATH: Đường dẫn file credentials JSON
- FIREBASE_STORAGE_BUCKET: Tên storage bucket
"""

import firebase_admin
from firebase_admin import credentials, storage
import os
from dotenv import load_dotenv
import uuid
import anyio

load_dotenv()

FIREBASE_CREDENTIALS_PATH = os.getenv("FIREBASE_CREDENTIALS_PATH")
FIREBASE_STORAGE_BUCKET = os.getenv("FIREBASE_STORAGE_BUCKET")


def init_firebase() -> bool:
    """
    Khởi tạo Firebase Admin SDK.

    Đọc credentials từ file JSON và khởi tạo app.
    Trả về False nếu file không tồn tại hoặc lỗi.

    Returns:
        True nếu khởi tạo thành công, False nếu lỗi.
    """
    if not FIREBASE_CREDENTIALS_PATH or not os.path.exists(FIREBASE_CREDENTIALS_PATH):
        print(f"Warning: Firebase credentials not found at {FIREBASE_CREDENTIALS_PATH}. Image upload will fail.")
        return False

    try:
        cred = credentials.Certificate(FIREBASE_CREDENTIALS_PATH)
        firebase_admin.initialize_app(cred, {
            'storageBucket': FIREBASE_STORAGE_BUCKET
        })
        return True
    except Exception as e:
        print(f"Error initializing Firebase: {e}")
        return False


def _upload_image_sync(file_content: bytes, filename: str) -> str:
    """
    Upload ảnh lên Firebase Storage (sync version).

    Args:
        file_content: Nội dung file ảnh (bytes).
        filename: Tên file gốc (VD: "photo.jpg").

    Returns:
        str: Public URL của ảnh đã upload, hoặc None nếu lỗi.
    """
    if not firebase_admin._apps:
        if not init_firebase():
            return None

    try:
        bucket = storage.bucket()
        ext = filename.split('.')[-1]
        unique_filename = f"images/{uuid.uuid4()}.{ext}"
        blob = bucket.blob(unique_filename)
        blob.upload_from_string(file_content, content_type=f"image/{ext}")
        blob.make_public()
        return blob.public_url
    except Exception as e:
        print(f"Error uploading image: {e}")
        return None


async def upload_image(file_content: bytes, filename: str) -> str:
    """
    Upload ảnh lên Firebase Storage (async version).

    Chạy _upload_image_sync trong thread pool để không block event loop.

    Args:
        file_content: Nội dung file ảnh (bytes).
        filename: Tên file gốc.

    Returns:
        str: Public URL của ảnh, hoặc None nếu lỗi.
    """
    return await anyio.to_thread.run_sync(_upload_image_sync, file_content, filename)
