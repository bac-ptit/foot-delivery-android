"""
Script đã sửa để hiển thị chi tiết thuộc tính của từng bản ghi
"""

from sqlalchemy.orm import Session
from database import SessionLocal, engine
from models import (
    Category, FAQ, User, Shipper, Promotion, Restaurant, MenuItem,
    Address, Orders, OrderItem, Payment, Delivery, Review, Notification,
    ChatSession, ChatMessage, UserDevice, UserFAQ, UsedPromotion,
    LoyaltyPoint, SocialShare
)
from sqlalchemy import inspect

def get_all_tables():
    """Lấy danh sách tất cả các bảng"""
    inspector = inspect(engine)
    return inspector.get_table_names()

def view_table_data(db: Session, model_class, table_name: str, limit: int = 10):
    """Xem dữ liệu của một bảng với đầy đủ thuộc tính"""
    print(f"\n{'='*60}")
    print(f"BẢNG: {table_name}")
    print(f"{'='*60}")
    
    try:
        total = db.query(model_class).count()
        print(f"Tổng số bản ghi: {total}")
        
        records = db.query(model_class).limit(limit).all()
        
        if not records:
            print("Không có dữ liệu trong bảng này.")
            return
        
        print(f"\nHiển thị {len(records)} bản ghi đầu tiên:")
        print("-" * 60)
        
        # SỬA ĐOẠN NÀY ĐỂ HIỂN THỊ THUỘC TÍNH
        for i, record in enumerate(records, 1):
            # Lấy danh sách các cột thực tế từ Model
            columns = [column.key for column in inspect(record).mapper.column_attrs]
            
            # Tạo dictionary chứa dữ liệu của các cột đó
            data = {col: getattr(record, col) for col in columns}
            
            # In ra dưới dạng đẹp hơn
            print(f"[{i}] {data}")
            
    except Exception as e:
        print(f"Lỗi khi truy vấn bảng {table_name}: {e}")

# ... (Các hàm view_all_data và view_specific_table giữ nguyên như cũ) ...

def view_all_data():
    """Xem dữ liệu tất cả các bảng"""
    db = SessionLocal()
    try:
        print("\n" + "="*60)
        print("DỮ LIỆU HIỆN TẠI TRONG DATABASE")
        print("="*60)
        tables = [
            (Category, "category"), (FAQ, "faq"), (User, "User"),
            (Shipper, "shipper"), (Promotion, "promotion"), (Restaurant, "restaurant"),
            (MenuItem, "menuitem"), (Address, "address"), (Orders, "orders"),
            (OrderItem, "orderitem"), (Payment, "payment"), (Delivery, "delivery"),
            (Review, "review"), (Notification, "notification"), (ChatSession, "chatsession"),
            (ChatMessage, "chatmessage"), (UserDevice, "userdevice"), (UserFAQ, "userfaq"),
            (UsedPromotion, "usedpromotion"), (LoyaltyPoint, "loyaltypoint"), (SocialShare, "socialshare"),
        ]
        for model_class, table_name in tables:
            view_table_data(db, model_class, table_name, limit=20)
    finally:
        db.close()

def view_specific_table(table_name: str, limit: int = 20):
    db = SessionLocal()
    try:
        table_map = {
            "category": Category, "faq": FAQ, "user": User, "shipper": Shipper,
            "promotion": Promotion, "restaurant": Restaurant, "menuitem": MenuItem,
            "address": Address, "orders": Orders, "orderitem": OrderItem,
            "payment": Payment, "delivery": Delivery, "review": Review,
            "notification": Notification, "chatsession": ChatSession, "chatmessage": ChatMessage,
            "userdevice": UserDevice, "userfaq": UserFAQ, "usedpromotion": UsedPromotion,
            "loyaltypoint": LoyaltyPoint, "socialshare": SocialShare,
        }
        model_class = table_map.get(table_name.lower())
        if not model_class:
            print(f"Không tìm thấy bảng '{table_name}'")
            return
        view_table_data(db, model_class, table_name, limit)
    finally:
        db.close()

if __name__ == "__main__":
    import sys
    if len(sys.argv) > 1:
        table_name = sys.argv[1]
        limit = int(sys.argv[2]) if len(sys.argv) > 2 else 20
        view_specific_table(table_name, limit)
    else:
        view_all_data()