"""
Module: chatbot_service.py

Service chatbot sử dụng Google Gemini AI để trả lời câu hỏi của người dùng.
Tự động lấy dữ liệu nhà hàng, món ăn và đơn hàng từ DB làm ngữ cảnh cho AI.
"""

import os
import anyio
from google import genai
from google.genai import errors as genai_errors
from sqlalchemy.orm import Session
import models
from dotenv import load_dotenv

load_dotenv()

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")

class ChatBotService:
    """Service xử lý chatbot AI, kết nối Google Gemini và truy vấn database."""

    def __init__(self, db: Session, user_id: int = None):
        self.db = db
        self.user_id = user_id
        # Khởi tạo Client bằng SDK chính thức
        if GEMINI_API_KEY:
            self.client = genai.Client(api_key=GEMINI_API_KEY)
        else:
            self.client = None

    def get_app_context(self):
        """Lấy dữ liệu từ DB để làm ngữ cảnh cho AI."""
        try:
            restaurants = self.db.query(models.Restaurant).all()
            menu_items = self.db.query(models.MenuItem).all()
            # Lấy đơn hàng theo user_id nếu có, ngược lại lấy tất cả
            if self.user_id:
                orders = self.db.query(models.Orders).filter(models.Orders.userid == self.user_id).all()
            else:
                orders = self.db.query(models.Orders).all()
            
            context = "Dưới đây là thông tin về ứng dụng Đặt Món:\n\n"
            
            context += "NHÀ HÀNG:\n"
            for r in restaurants:
                context += f"- {r.name}: {r.description} (Địa chỉ: {r.address}, Trạng thái: {r.status})\n"
                
            context += "\nMÓN ĂN:\n"
            for m in menu_items:
                res_name = next((r.name for r in restaurants if r.id == m.restaurantid), "Không rõ")
                context += f"- {m.name}: Giá {m.price}đ, Mô tả: {m.description} (Tại nhà hàng: {res_name})\n"
            
            context += "\nĐƠN HÀNG:\n"
            for o in orders:
                user_name = "Không rõ"
                try:
                    user = self.db.query(models.User).filter(models.User.id == o.userid).first()
                    if user:
                        user_name = user.name
                except:
                    pass
                context += f"- Đơn hàng #{o.id}: Trạng thái {o.status}, Tổng tiền {o.totalprice}đ, Khách hàng: {user_name}\n"
                
            return context
        except Exception as e:
            print(f"Lỗi lấy dữ liệu DB: {e}")
            return "Hiện chưa có thông tin cụ thể."

    def _call_gemini_sdk(self, prompt: str):
        """Hàm đồng bộ gọi AI thông qua SDK."""
        try:
            # Nâng cấp lên model thế hệ 3 mới nhất
            response = self.client.models.generate_content(
                model='gemini-3-flash-preview',
                contents=prompt
            )
            return response.text
        except genai_errors.ClientError as e:
            error_code = getattr(e, 'code', None)
            error_message = str(e)
            
            # Xử lý lỗi 429 - Quota exceeded
            if error_code == 429 or 'RESOURCE_EXHAUSTED' in error_message:
                print(f"Lỗi 429 - Quota exceeded")
                raise Exception("QUOTA_EXCEEDED")
            else:
                print(f"Lỗi SDK Gemini: {e}")
                raise e
        except Exception as e:
            print(f"Lỗi SDK Gemini: {e}")
            raise e

    async def get_response(self, user_message: str):
        """Nhận tin nhắn người dùng, xây dựng prompt và gọi Gemini AI trả lời."""
        if not self.client:
            return "Xin lỗi, ChatBot chưa được cấu hình API Key trong file .env."

        app_context = self.get_app_context()
        
        prompt = f"""
        Bạn là trợ lý ảo của ứng dụng 'Đặt Món' - Food Delivery Việt Nam.
        
        {app_context}
        
        HƯỚNG DẪN:
        1. Trả lời dựa trên dữ liệu nhà hàng, món ăn và đơn hàng được cung cấp ở trên.
        2. Thân thiện, ngắn gọn, trả lời bằng tiếng Việt.
        3. Nếu khách hàng hỏi về đơn hàng, hãy kiểm tra thông tin đơn hàng trong dữ liệu.
        
        Câu hỏi khách hàng: {user_message}
        """

        try:
            # Chạy trong thread pool để đảm bảo non-blocking cho FastAPI
            return await anyio.to_thread.run_sync(self._call_gemini_sdk, prompt)
        except Exception as e:
            error_message = str(e)
            if "QUOTA_EXCEEDED" in error_message:
                return "⚠️ Xin lỗi, hệ thống AI tạm thời quá tải do đã đạt giới hạn sử dụng miễn phí. Vui lòng thử lại sau 1-2 phút hoặc liên hệ admin để nâng cấp API."
            return "Rất tiếc, AI đang gặp một chút trục trặc. Bạn thử lại sau nhé!"
