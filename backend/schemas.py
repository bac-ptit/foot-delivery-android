from pydantic import BaseModel, ConfigDict
from typing import List, Optional
from datetime import date, time, datetime
from decimal import Decimal

# Base Schemas
class CategoryBase(BaseModel):
    name: str
    type: Optional[str] = None
    description: Optional[str] = None

class CategoryCreate(CategoryBase):
    pass

class Category(CategoryBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

class MenuItemBase(BaseModel):
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
    access_token: str
    token_type: str

class TokenData(BaseModel):
    email: Optional[str] = None

class AddressBase(BaseModel):
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
    quantity: int
    price: int
    menuitemid: int

class OrderItemCreate(OrderItemBase):
    pass

class OrderItem(OrderItemBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

class OrderBase(BaseModel):
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
    id: int
    quantity: int
    price: int
    menuitemid: int
    menuitem_name: Optional[str] = None
    image_url: Optional[str] = None

class OrderDetail(OrderBase):
    id: int
    createdat: datetime
    restaurant_name: Optional[str] = None
    address_detail: Optional[str] = None
    order_items: List[OrderItemDetail] = []

class OrderStatusUpdate(BaseModel):
    status: str

class UserProfileSummary(BaseModel):
    user_id: int
    user_name: str
    points: int
    delivered_orders: int
    total_spent: int

# Chat Schemas
class ChatMessageBase(BaseModel):
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
    userid: int

class ChatSession(BaseModel):
    id: int
    createdat: datetime
    status: str
    messages: List[ChatMessage] = []
    model_config = ConfigDict(from_attributes=True)

# UserDevice Schemas
class UserDeviceBase(BaseModel):
    device_token: str
    device_type: Optional[str] = None

class UserDeviceCreate(UserDeviceBase):
    userid: int

class UserDevice(UserDeviceBase):
    id: int
    last_active: datetime
    userid: int
    model_config = ConfigDict(from_attributes=True)

# UserFAQ Schemas
class UserFAQBase(BaseModel):
    userid: int
    faqid: int

class UserFAQCreate(UserFAQBase):
    pass

class UserFAQ(UserFAQBase):
    id: int
    viewedat: datetime
    model_config = ConfigDict(from_attributes=True)

# FAQ Schemas
class FAQBase(BaseModel):
    question: str
    answer: str
    isactive: bool = True

class FAQCreate(FAQBase):
    pass

class FAQ(FAQBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

# Shipper Schemas
class ShipperBase(BaseModel):
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

# Promotion Schemas
class PromotionBase(BaseModel):
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

# Payment Schemas
class PaymentBase(BaseModel):
    status: str
    method: str
    orderid: int

class PaymentCreate(PaymentBase):
    pass

class Payment(PaymentBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

# Delivery Schemas
class DeliveryBase(BaseModel):
    status: str
    deliverytime: Optional[time] = None
    orderid: int
    shipperid: int

class DeliveryCreate(DeliveryBase):
    pass

class Delivery(DeliveryBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

# Review Schemas
class ReviewBase(BaseModel):
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
    id: int
    restaurant_name: Optional[str] = None
    reviews: List["ReviewResponse"] = []
    avg_rating: float = 0.0
    model_config = ConfigDict(from_attributes=True)

# Notification Schemas
class NotificationBase(BaseModel):
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

# UsedPromotion Schemas
class UsedPromotionBase(BaseModel):
    usedat: date
    promotionid: int
    orderid: int

class UsedPromotionCreate(UsedPromotionBase):
    pass

class UsedPromotion(UsedPromotionBase):
    id: int
    model_config = ConfigDict(from_attributes=True)

# LoyaltyPoint Schemas
class LoyaltyPointBase(BaseModel):
    points: int = 0
    userid: int
    menuitemid: int

class LoyaltyPointCreate(LoyaltyPointBase):
    pass

class LoyaltyPoint(LoyaltyPointBase):
    id: int
    updatedat: date
    model_config = ConfigDict(from_attributes=True)

# SocialShare Schemas
class SocialShareBase(BaseModel):
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
