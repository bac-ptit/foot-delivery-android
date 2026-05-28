"""
Module: firebase_utils.py

Khởi tạo Firebase Admin SDK và upload ảnh lên Firebase Storage.
Hỗ trợ upload bất đồng bộ qua anyio để không chặn event loop.
"""

import firebase_admin
from firebase_admin import credentials, storage
import os
from dotenv import load_dotenv
import uuid
import anyio

load_dotenv()

# Đường dẫn file credentials Firebase (JSON service account)
FIREBASE_CREDENTIALS_PATH = os.getenv("FIREBASE_CREDENTIALS_PATH")
# Tên bucket Firebase Storage để upload ảnh
FIREBASE_STORAGE_BUCKET = os.getenv("FIREBASE_STORAGE_BUCKET")

def init_firebase():
    """Khởi tạo Firebase Admin SDK với credentials từ file service account."""
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

def _upload_image_sync(file_content, filename):
    """Upload ảnh đồng bộ lên Firebase Storage, trả về URL công khai."""
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

async def upload_image(file_content, filename):
    """Bản async của hàm upload ảnh, không gây nghẽn event loop."""
    return await anyio.to_thread.run_sync(_upload_image_sync, file_content, filename)
