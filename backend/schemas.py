"""
Module: schemas.py

Định nghĩa tất cả Pydantic v2 schemas cho request/response validation.
Bao gồm schema cho Category, MenuItem, Restaurant, User, Order, Payment, Review, v.v.
"""

from pydantic import BaseModel, ConfigDict
from typing import List, Optional
from datetime import date, time, datetime
from decimal import Decimal

# ─── Base Schemas ────────────────────────────────────────────────────────────

class CategoryBase(BaseModel):
    """Schema cơ sở cho danh mục món ăn."""
    name: str
    type: Optional[str] = None
    description: Optional[str] = None

class CategoryCreate(CategoryBase):
    pass

class Category(CategoryBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

class MenuItemBase(BaseModel):
    """Schema cơ sở cho món ăn trong thực đơn."""
    name: str
    image_url: Optional[str] = None
    price: int
    is_available: bool = True
    description: Optional[str] = None
    restaurantid: int
    categoryid: int

class MenuItemCreate(MenuItemBase):
    pass

class MenuItem(MenuItemBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

class MenuItemWithRestaurant(MenuItemBase):
    id: int
    restaurant_name: Optional[str] = None
    model_config = ConfigDict(from_attributes=True)

class RestaurantBase(BaseModel):
    """Schema cơ sở cho nhà hàng."""
    name: str
    image_url: Optional[str] = None
    address: Optional[str] = None
    rating: Optional[int] = None
    open_time: Optional[time] = None
    close_time: Optional[time] = None
    phone_number: str
    status: str
    description: Optional[str] = None

class RestaurantCreate(RestaurantBase):
    pass

class Restaurant(RestaurantBase):
    id: int
    menu_items: List[MenuItem] = []
    model_config = ConfigDict(from_attributes=True)

class UserBase(BaseModel):
    """Schema cơ sở cho người dùng."""
    name: str
    username: str
    email: str
    phone: str
    role: str

class UserCreate(UserBase):
    password: str

class User(UserBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

class Token(BaseModel):
    """Schema phản hồi JWT token khi đăng nhập thành công."""
    access_token: str
    token_type: str

class TokenData(BaseModel):
    """Schema dữ liệu giải mã từ JWT token."""
    email: Optional[str] = None

class AddressBase(BaseModel):
    """Schema cơ sở cho địa chỉ giao hàng."""
    detail: str
    phone: Optional[str] = None
    userid: int

class AddressCreate(AddressBase):
    pass

class AddressUpdate(BaseModel):
    detail: Optional[str] = None
    phone: Optional[str] = None

class Address(AddressBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

class OrderItemBase(BaseModel):
    """Schema cơ sở cho chi tiết món trong đơn hàng."""
    quantity: int
    price: int
    menuitemid: int

class OrderItemCreate(OrderItemBase):
    pass

class OrderItem(OrderItemBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

class OrderBase(BaseModel):
    """Schema cơ sở cho đơn hàng."""
    status: str
    preorderdate: Optional[date] = None
    preordertime: Optional[time] = None
    totalprice: int
    restaurantid: int
    addressid: int
    userid: int

class OrderCreate(OrderBase):
    order_items: List[OrderItemCreate]

class Order(OrderBase):
    id: int
    createdat: datetime
    order_items: List[OrderItem] = []
    model_config = ConfigDict(from_attributes=True)

class OrderItemDetail(BaseModel):
    """Schema chi tiết món ăn trong đơn hàng kèm tên và ảnh."""
    id: int
    quantity: int
    price: int
    menuitemid: int
    menuitem_name: Optional[str] = None
    image_url: Optional[str] = None

class OrderDetail(OrderBase):
    """Schema chi tiết đơn hàng đầy đủ kèm tên nhà hàng và địa chỉ."""
    id: int
    createdat: datetime
    restaurant_name: Optional[str] = None
    address_detail: Optional[str] = None
    order_items: List[OrderItemDetail] = []

class OrderStatusUpdate(BaseModel):
    """Schema cập nhật trạng thái đơn hàng."""
    status: str

class UserProfileSummary(BaseModel):
    """Schema tóm tắt hồ sơ người dùng: điểm, đơn hàng, tổng chi tiêu."""
    user_id: int
    user_name: str
    points: int
    delivered_orders: int
    total_spent: int

# ─── Chat Schemas ────────────────────────────────────────────────────────────

class ChatMessageBase(BaseModel):
    """Schema cơ sở cho tin nhắn chat."""
    message: str

class ChatMessageCreate(ChatMessageBase):
    pass

class ChatMessage(ChatMessageBase):
    id: int
    senderrole: str
    sentat: datetime
    sessionid: int
    model_config = ConfigDict(from_attributes=True)

class ChatSessionCreate(BaseModel):
    """Schema tạo phiên trò chuyện chatbot mới."""
    userid: int

class ChatSession(BaseModel):
    id: int
    createdat: datetime
    status: str
    messages: List[ChatMessage] = []
    model_config = ConfigDict(from_attributes=True)

# ─── UserDevice Schemas ──────────────────────────────────────────────────────

class UserDeviceBase(BaseModel):
    """Schema cơ sở cho thiết bị người dùng (FCM token)."""
    device_token: str
    device_type: Optional[str] = None

class UserDeviceCreate(UserDeviceBase):
    userid: int

class UserDevice(UserDeviceBase):
    id: int
    last_active: datetime
    userid: int
    model_config = ConfigDict(from_attributes=True)

# ─── UserFAQ Schemas ─────────────────────────────────────────────────────────

class UserFAQBase(BaseModel):
    """Schema cơ sở cho lịch sử xem FAQ của người dùng."""
    userid: int
    faqid: int

class UserFAQCreate(UserFAQBase):
    pass

class UserFAQ(UserFAQBase):
    id: int
    viewedat: datetime
    model_config = ConfigDict(from_attributes=True)

# ─── FAQ Schemas ─────────────────────────────────────────────────────────────

class FAQBase(BaseModel):
    """Schema cơ sở cho câu hỏi thường gặp."""
    question: str
    answer: str
    isactive: bool = True

class FAQCreate(FAQBase):
    pass

class FAQ(FAQBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

# ─── Shipper Schemas ─────────────────────────────────────────────────────────

class ShipperBase(BaseModel):
    """Schema cơ sở cho người giao hàng."""
    name: str
    phone: str
    rating: Optional[int] = None
    description: Optional[str] = None
    status: str

class ShipperCreate(ShipperBase):
    pass

class Shipper(ShipperBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

# ─── Promotion Schemas ───────────────────────────────────────────────────────

class PromotionBase(BaseModel):
    """Schema cơ sở cho mã khuyến mãi."""
    code: str
    discounttype: str
    discountvalue: int
    expiredate: date
    minordervalue: int
    status: str

class PromotionCreate(PromotionBase):
    pass

class Promotion(PromotionBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

# ─── Payment Schemas ─────────────────────────────────────────────────────────

class PaymentBase(BaseModel):
    """Schema cơ sở cho thông tin thanh toán."""
    status: str
    method: str
    orderid: int

class PaymentCreate(PaymentBase):
    pass

class Payment(PaymentBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

# ─── Delivery Schemas ────────────────────────────────────────────────────────

class DeliveryBase(BaseModel):
    """Schema cơ sở cho thông tin giao hàng."""
    status: str
    deliverytime: Optional[time] = None
    orderid: int
    shipperid: int

class DeliveryCreate(DeliveryBase):
    pass

class Delivery(DeliveryBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

# ─── Review Schemas ──────────────────────────────────────────────────────────

class ReviewBase(BaseModel):
    """Schema cơ sở cho đánh giá của người dùng."""
    rating: int
    comment: Optional[str] = None
    orderid: int
    userid: int
    menuitemid: Optional[int] = None
    restaurantid: Optional[int] = None

class ReviewCreate(ReviewBase):
    pass

class Review(ReviewBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

class ReviewResponse(BaseModel):
    id: int
    rating: int
    comment: Optional[str] = None
    userid: int
    user_name: Optional[str] = None
    model_config = ConfigDict(from_attributes=True)

class MenuItemWithReviews(MenuItemBase):
    """Schema món ăn kèm danh sách đánh giá và điểm trung bình."""
    id: int
    restaurant_name: Optional[str] = None
    reviews: List["ReviewResponse"] = []
    avg_rating: float = 0.0
    model_config = ConfigDict(from_attributes=True)

# ─── Notification Schemas ────────────────────────────────────────────────────

class NotificationBase(BaseModel):
    """Schema cơ sở cho thông báo đẩy."""
    title: str
    type: str
    content: str
    isread: bool = False
    userid: int
    orderid: Optional[int] = None
    sessionid: Optional[int] = None

class NotificationCreate(NotificationBase):
    pass

class Notification(NotificationBase):
    id: int
    createdat: datetime
    model_config = ConfigDict(from_attributes=True)

# ─── UsedPromotion Schemas ───────────────────────────────────────────────────

class UsedPromotionBase(BaseModel):
    """Schema cơ sở cho mã khuyến mãi đã sử dụng."""
    usedat: date
    promotionid: int
    orderid: int

class UsedPromotionCreate(UsedPromotionBase):
    pass

class UsedPromotion(UsedPromotionBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

# ─── LoyaltyPoint Schemas ────────────────────────────────────────────────────

class LoyaltyPointBase(BaseModel):
    """Schema cơ sở cho điểm tích lũy thành viên."""
    points: int = 0
    userid: int
    menuitemid: int

class LoyaltyPointCreate(LoyaltyPointBase):
    pass

class LoyaltyPoint(LoyaltyPointBase):
    id: int
    updatedat: date
    model_config = ConfigDict(from_attributes=True)

# ─── SocialShare Schemas ─────────────────────────────────────────────────────

class SocialShareBase(BaseModel):
    """Schema cơ sở cho lượt chia sẻ mạng xã hội."""
    sharetype: str
    platform: str
    context: Optional[str] = None
    userid: int
    restaurantid: int
    menuitemid: int

class SocialShareCreate(SocialShareBase):
    pass

class SocialShare(SocialShareBase):
    id: int
    createdat: date
    model_config = ConfigDict(from_attributes=True)
