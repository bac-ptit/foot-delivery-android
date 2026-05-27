"""
Module định nghĩa Pydantic schemas cho API request/response.

Sử dụng Pydantic v2 với ConfigDict(from_attributes=True) để
tương thích với SQLAlchemy ORM models.

Cấu trúc mỗi entity:
- *Base: Schema cơ bản (dùng chung)
- *Create: Schema cho request tạo mới
- *: Schema cho response (có id)
"""

from pydantic import BaseModel, ConfigDict
from typing import List, Optional
from datetime import date, time, datetime
from decimal import Decimal


# ══════════════════════════════════════════════════════════════
# CATEGORY SCHEMAS
# ══════════════════════════════════════════════════════════════

class CategoryBase(BaseModel):
    """Schema cơ bản cho danh mục món ăn."""
    name: str
    type: Optional[str] = None
    description: Optional[str] = None

class CategoryCreate(CategoryBase):
    """Schema tạo danh mục mới."""
    pass

class Category(CategoryBase):
    """Schema response danh mục (có id)."""
    id: int
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# MENU ITEM SCHEMAS
# ══════════════════════════════════════════════════════════════

class MenuItemBase(BaseModel):
    """Schema cơ bản cho món ăn."""
    name: str
    image_url: Optional[str] = None
    price: int
    is_available: bool = True
    description: Optional[str] = None
    restaurantid: int
    categoryid: int

class MenuItemCreate(MenuItemBase):
    """Schema tạo món ăn mới."""
    pass

class MenuItem(MenuItemBase):
    """Schema response món ăn (có id)."""
    id: int
    model_config = ConfigDict(from_attributes=True)

class MenuItemWithRestaurant(MenuItemBase):
    """Schema response món ăn kèm tên nhà hàng."""
    id: int
    restaurant_name: Optional[str] = None
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# RESTAURANT SCHEMAS
# ══════════════════════════════════════════════════════════════

class RestaurantBase(BaseModel):
    """Schema cơ bản cho nhà hàng."""
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
    """Schema tạo nhà hàng mới."""
    pass

class Restaurant(RestaurantBase):
    """Schema response nhà hàng (có id + menu_items)."""
    id: int
    menu_items: List[MenuItem] = []
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# USER SCHEMAS
# ══════════════════════════════════════════════════════════════

class UserBase(BaseModel):
    """Schema cơ bản cho người dùng."""
    name: str
    username: str
    email: str
    phone: str
    role: str

class UserCreate(UserBase):
    """Schema tạo người dùng mới (có password)."""
    password: str

class User(UserBase):
    """Schema response người dùng (có id)."""
    id: int
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# TOKEN SCHEMAS
# ══════════════════════════════════════════════════════════════

class Token(BaseModel):
    """Schema response JWT token."""
    access_token: str
    token_type: str

class TokenData(BaseModel):
    """Schema data trong JWT payload."""
    email: Optional[str] = None


# ══════════════════════════════════════════════════════════════
# ADDRESS SCHEMAS
# ══════════════════════════════════════════════════════════════

class AddressBase(BaseModel):
    """Schema cơ bản cho địa chỉ."""
    detail: str
    phone: Optional[str] = None
    userid: int

class AddressCreate(AddressBase):
    """Schema tạo địa chỉ mới."""
    pass

class AddressUpdate(BaseModel):
    """Schema cập nhật địa chỉ (chỉ các trường cần thay đổi)."""
    detail: Optional[str] = None
    phone: Optional[str] = None

class Address(AddressBase):
    """Schema response địa chỉ (có id)."""
    id: int
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# ORDER ITEM SCHEMAS
# ══════════════════════════════════════════════════════════════

class OrderItemBase(BaseModel):
    """Schema cơ bản cho chi tiết đơn hàng."""
    quantity: int
    price: int
    menuitemid: int

class OrderItemCreate(OrderItemBase):
    """Schema tạo chi tiết đơn hàng mới."""
    pass

class OrderItem(OrderItemBase):
    """Schema response chi tiết đơn hàng (có id)."""
    id: int
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# ORDER SCHEMAS
# ══════════════════════════════════════════════════════════════

class OrderBase(BaseModel):
    """Schema cơ bản cho đơn hàng."""
    status: str
    preorderdate: Optional[date] = None
    preordertime: Optional[time] = None
    totalprice: int
    restaurantid: int
    addressid: int
    userid: int

class OrderCreate(OrderBase):
    """Schema tạo đơn hàng mới (kèm danh sách món)."""
    order_items: List[OrderItemCreate]

class Order(OrderBase):
    """Schema response đơn hàng (có id, createdat, order_items)."""
    id: int
    createdat: datetime
    order_items: List[OrderItem] = []
    model_config = ConfigDict(from_attributes=True)

class OrderItemDetail(BaseModel):
    """Schema chi tiết món trong đơn hàng (kèm tên và hình ảnh)."""
    id: int
    quantity: int
    price: int
    menuitemid: int
    menuitem_name: Optional[str] = None
    image_url: Optional[str] = None

class OrderDetail(OrderBase):
    """Schema response chi tiết đơn hàng đầy đủ (kèm nhà hàng, địa chỉ)."""
    id: int
    createdat: datetime
    restaurant_name: Optional[str] = None
    address_detail: Optional[str] = None
    order_items: List[OrderItemDetail] = []

class OrderStatusUpdate(BaseModel):
    """Schema cập nhật trạng thái đơn hàng."""
    status: str


# ══════════════════════════════════════════════════════════════
# USER PROFILE SCHEMAS
# ══════════════════════════════════════════════════════════════

class UserProfileSummary(BaseModel):
    """Schema tổng quan hồ sơ người dùng (điểm tích lũy)."""
    user_id: int
    user_name: str
    points: int
    delivered_orders: int
    total_spent: int


# ══════════════════════════════════════════════════════════════
# CHAT SCHEMAS
# ══════════════════════════════════════════════════════════════

class ChatMessageBase(BaseModel):
    """Schema cơ bản cho tin nhắn chat."""
    message: str

class ChatMessageCreate(ChatMessageBase):
    """Schema tạo tin nhắn mới."""
    pass

class ChatMessage(ChatMessageBase):
    """Schema response tin nhắn (có id, senderrole, sentat)."""
    id: int
    senderrole: str
    sentat: datetime
    sessionid: int
    model_config = ConfigDict(from_attributes=True)

class ChatSessionCreate(BaseModel):
    """Schema tạo phiên chat mới."""
    userid: int

class ChatSession(BaseModel):
    """Schema response phiên chat (có messages)."""
    id: int
    createdat: datetime
    status: str
    messages: List[ChatMessage] = []
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# USER DEVICE SCHEMAS
# ══════════════════════════════════════════════════════════════

class UserDeviceBase(BaseModel):
    """Schema cơ bản cho thiết bị người dùng."""
    device_token: str
    device_type: Optional[str] = None

class UserDeviceCreate(UserDeviceBase):
    """Schema tạo thiết bị mới."""
    userid: int

class UserDevice(UserDeviceBase):
    """Schema response thiết bị (có id, last_active)."""
    id: int
    last_active: datetime
    userid: int
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# FAQ SCHEMAS
# ══════════════════════════════════════════════════════════════

class UserFAQBase(BaseModel):
    """Schema cơ bản cho lịch sử xem FAQ."""
    userid: int
    faqid: int

class UserFAQCreate(UserFAQBase):
    """Schema tạo lịch sử xem FAQ."""
    pass

class UserFAQ(UserFAQBase):
    """Schema response lịch sử xem FAQ."""
    id: int
    viewedat: datetime
    model_config = ConfigDict(from_attributes=True)

class FAQBase(BaseModel):
    """Schema cơ bản cho câu hỏi thường gặp."""
    question: str
    answer: str
    isactive: bool = True

class FAQCreate(FAQBase):
    """Schema tạo FAQ mới."""
    pass

class FAQ(FAQBase):
    """Schema response FAQ (có id)."""
    id: int
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# SHIPPER SCHEMAS
# ══════════════════════════════════════════════════════════════

class ShipperBase(BaseModel):
    """Schema cơ bản cho người giao hàng."""
    name: str
    phone: str
    rating: Optional[int] = None
    description: Optional[str] = None
    status: str

class ShipperCreate(ShipperBase):
    """Schema tạo shipper mới."""
    pass

class Shipper(ShipperBase):
    """Schema response shipper (có id)."""
    id: int
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# PROMOTION SCHEMAS
# ══════════════════════════════════════════════════════════════

class PromotionBase(BaseModel):
    """Schema cơ bản cho mã giảm giá."""
    code: str
    discounttype: str
    discountvalue: int
    expiredate: date
    minordervalue: int
    status: str

class PromotionCreate(PromotionBase):
    """Schema tạo mã giảm giá mới."""
    pass

class Promotion(PromotionBase):
    """Schema response mã giảm giá (có id)."""
    id: int
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# PAYMENT SCHEMAS
# ══════════════════════════════════════════════════════════════

class PaymentBase(BaseModel):
    """Schema cơ bản cho thanh toán."""
    status: str
    method: str
    orderid: int

class PaymentCreate(PaymentBase):
    """Schema tạo thanh toán mới."""
    pass

class Payment(PaymentBase):
    """Schema response thanh toán (có id)."""
    id: int
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# DELIVERY SCHEMAS
# ══════════════════════════════════════════════════════════════

class DeliveryBase(BaseModel):
    """Schema cơ bản cho giao hàng."""
    status: str
    deliverytime: Optional[time] = None
    orderid: int
    shipperid: int

class DeliveryCreate(DeliveryBase):
    """Schema tạo giao hàng mới."""
    pass

class Delivery(DeliveryBase):
    """Schema response giao hàng (có id)."""
    id: int
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# REVIEW SCHEMAS
# ══════════════════════════════════════════════════════════════

class ReviewBase(BaseModel):
    """Schema cơ bản cho đánh giá."""
    rating: int
    comment: Optional[str] = None
    orderid: int
    userid: int
    menuitemid: Optional[int] = None
    restaurantid: Optional[int] = None

class ReviewCreate(ReviewBase):
    """Schema tạo đánh giá mới."""
    pass

class Review(ReviewBase):
    """Schema response đánh giá (có id)."""
    id: int
    model_config = ConfigDict(from_attributes=True)

class ReviewResponse(BaseModel):
    """Schema response đánh giá (kèm user_name)."""
    id: int
    rating: int
    comment: Optional[str] = None
    userid: int
    user_name: Optional[str] = None
    model_config = ConfigDict(from_attributes=True)

class MenuItemWithReviews(MenuItemBase):
    """Schema response món ăn kèm reviews và avg_rating."""
    id: int
    restaurant_name: Optional[str] = None
    reviews: List["ReviewResponse"] = []
    avg_rating: float = 0.0
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# NOTIFICATION SCHEMAS
# ══════════════════════════════════════════════════════════════

class NotificationBase(BaseModel):
    """Schema cơ bản cho thông báo."""
    title: str
    type: str
    content: str
    isread: bool = False
    userid: int
    orderid: Optional[int] = None
    sessionid: Optional[int] = None

class NotificationCreate(NotificationBase):
    """Schema tạo thông báo mới."""
    pass

class Notification(NotificationBase):
    """Schema response thông báo (có id, createdat)."""
    id: int
    createdat: datetime
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# USED PROMOTION SCHEMAS
# ══════════════════════════════════════════════════════════════

class UsedPromotionBase(BaseModel):
    """Schema cơ bản cho mã giảm giá đã sử dụng."""
    usedat: date
    promotionid: int
    orderid: int

class UsedPromotionCreate(UsedPromotionBase):
    """Schema tạo mã đã sử dụng."""
    pass

class UsedPromotion(UsedPromotionBase):
    """Schema response mã đã sử dụng (có id)."""
    id: int
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# LOYALTY POINT SCHEMAS
# ══════════════════════════════════════════════════════════════

class LoyaltyPointBase(BaseModel):
    """Schema cơ bản cho điểm tích lũy."""
    points: int = 0
    userid: int
    menuitemid: int

class LoyaltyPointCreate(LoyaltyPointBase):
    """Schema tạo điểm tích lũy."""
    pass

class LoyaltyPoint(LoyaltyPointBase):
    """Schema response điểm tích lũy (có id, updatedat)."""
    id: int
    updatedat: date
    model_config = ConfigDict(from_attributes=True)


# ══════════════════════════════════════════════════════════════
# SOCIAL SHARE SCHEMAS
# ══════════════════════════════════════════════════════════════

class SocialShareBase(BaseModel):
    """Schema cơ bản cho chia sẻ mạng xã hội."""
    sharetype: str
    platform: str
    context: Optional[str] = None
    userid: int
    restaurantid: int
    menuitemid: int

class SocialShareCreate(SocialShareBase):
    """Schema tạo chia sẻ mới."""
    pass

class SocialShare(SocialShareBase):
    """Schema response chia sẻ (có id, createdat)."""
    id: int
    createdat: date
    model_config = ConfigDict(from_attributes=True)
