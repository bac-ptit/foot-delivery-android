"""
Module định nghĩa SQLAlchemy ORM models cho database.

Chứa tất cả các bảng trong database của hệ thống đặt đồ ăn.
Sử dụng SQLAlchemy ORM với PostgreSQL.

Các bảng chính:
- User: Người dùng (customer, shipper, admin)
- Restaurant: Nhà hàng
- MenuItem: Món ăn
- Orders: Đơn hàng
- OrderItem: Chi tiết đơn hàng
- Review: Đánh giá
- Address: Địa chỉ giao hàng
- Payment: Thanh toán
- Notification: Thông báo
- Promotion: Mã giảm giá
"""

from sqlalchemy import Column, Integer, String, Boolean, Numeric, Text, Date, Time, ForeignKey, DateTime, func
from sqlalchemy.orm import relationship
from database import Base


class Category(Base):
    """
    Danh mục món ăn (VD: Cơm, Phở, Bún, Đồ uống...).

    Attributes:
        id: Định danh duy nhất.
        name: Tên danh mục.
        type: Loại danh mục (có thể null).
        description: Mô tả danh mục (có thể null).
        menu_items: Danh sách món ăn thuộc danh mục.
    """
    __tablename__ = "category"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    type = Column(String(255))
    description = Column(Text)
    menu_items = relationship("MenuItem", back_populates="category")


class FAQ(Base):
    """
    Câu hỏi thường gặp (FAQ).

    Attributes:
        id: Định danh duy nhất.
        question: Nội dung câu hỏi.
        answer: Câu trả lời.
        isactive: Có hiển thị hay không.
    """
    __tablename__ = "faq"
    id = Column(Integer, primary_key=True, index=True)
    question = Column(String(255))
    answer = Column(Text)
    isactive = Column(Boolean, default=True)
    user_faqs = relationship("UserFAQ", back_populates="faq")


class User(Base):
    """
    Người dùng trong hệ thống.

    Attributes:
        id: Định danh duy nhất.
        name: Họ tên.
        username: Tên đăng nhập (duy nhất).
        email: Địa chỉ email (duy nhất).
        password: Mật khẩu đã hash (bcrypt).
        phone: Số điện thoại.
        role: Vai trò: "customer", "shipper", "admin".
    """
    __tablename__ = "User"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    username = Column(String(255), nullable=False)
    email = Column(String(255), nullable=False, unique=True)
    password = Column(String(255), nullable=False)
    phone = Column(String(255), nullable=False)
    role = Column(String(255), nullable=False)

    addresses = relationship("Address", back_populates="user")
    orders = relationship("Orders", back_populates="user")
    reviews = relationship("Review", back_populates="user")
    user_devices = relationship("UserDevice", back_populates="user")
    user_faqs = relationship("UserFAQ", back_populates="user")
    chat_sessions = relationship("ChatSession", back_populates="user")
    notifications = relationship("Notification", back_populates="user")
    loyalty_points = relationship("LoyaltyPoint", back_populates="user")
    social_shares = relationship("SocialShare", back_populates="user")


class Shipper(Base):
    """
    Người giao hàng (shipper).

    Attributes:
        id: Định danh duy nhất.
        name: Họ tên.
        phone: Số điện thoại.
        rating: Điểm đánh giá (1-5).
        description: Mô tả.
        status: Trạng thái: "available", "busy", "offline".
    """
    __tablename__ = "shipper"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    phone = Column(String(255), nullable=False)
    rating = Column(Integer)
    description = Column(Text)
    status = Column(String(255), nullable=False)
    deliveries = relationship("Delivery", back_populates="shipper")


class Promotion(Base):
    """
    Mã giảm giá / khuyến mãi.

    Attributes:
        id: Định danh duy nhất.
        code: Mã giảm giá.
        discounttype: Loại giảm: "percentage" hoặc "fixed".
        discountvalue: Giá trị giảm.
        expiredate: Ngày hết hạn.
        minordervalue: Giá trị đơn tối thiểu.
        status: Trạng thái: "active", "expired".
    """
    __tablename__ = "promotion"
    id = Column(Integer, primary_key=True, index=True)
    code = Column(String(255))
    discounttype = Column(String(255))
    discountvalue = Column(Integer)
    expiredate = Column(Date)
    minordervalue = Column(Integer)
    status = Column(String(255), nullable=False)
    used_promotions = relationship("UsedPromotion", back_populates="promotion")


class Restaurant(Base):
    """
    Nhà hàng trong hệ thống.

    Attributes:
        id: Định danh duy nhất.
        name: Tên nhà hàng.
        image_url: URL hình ảnh.
        address: Địa chỉ.
        rating: Điểm đánh giá trung bình (1-5).
        open_time: Giờ mở cửa.
        close_time: Giờ đóng cửa.
        phone_number: Số điện thoại liên hệ.
        status: Trạng thái: "open", "closed", "temporarily_closed".
        description: Mô tả nhà hàng.
    """
    __tablename__ = "restaurant"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    image_url = Column(Text)
    address = Column(String(255))
    rating = Column(Integer)
    open_time = Column(Time)
    close_time = Column(Time)
    phone_number = Column(String(255), nullable=False)
    status = Column(String(255), nullable=False)
    description = Column(Text)

    menu_items = relationship("MenuItem", back_populates="restaurant")
    orders = relationship("Orders", back_populates="restaurant")
    reviews = relationship("Review", back_populates="restaurant")
    social_shares = relationship("SocialShare", back_populates="restaurant")


class MenuItem(Base):
    """
    Món ăn trong thực đơn.

    Attributes:
        id: Định danh duy nhất.
        name: Tên món ăn.
        image_url: URL hình ảnh.
        price: Giá tiền (VNĐ).
        is_available: Còn phục vụ hay không.
        description: Mô tả món ăn.
        restaurantid: ID nhà hàng (FK).
        categoryid: ID danh mục (FK).
    """
    __tablename__ = "menuitem"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    image_url = Column(Text)
    price = Column(Integer)
    is_available = Column(Boolean, default=True)
    description = Column(Text)
    restaurantid = Column(Integer, ForeignKey("restaurant.id"))
    categoryid = Column(Integer, ForeignKey("category.id"))

    restaurant = relationship("Restaurant", back_populates="menu_items")
    category = relationship("Category", back_populates="menu_items")
    order_items = relationship("OrderItem", back_populates="menu_item")
    reviews = relationship("Review", back_populates="menu_item")
    social_shares = relationship("SocialShare", back_populates="menu_item")


class Address(Base):
    """
    Địa chỉ giao hàng của người dùng.

    Attributes:
        id: Định danh duy nhất.
        detail: Địa chỉ chi tiết.
        phone: Số điện thoại liên hệ (có thể null).
        userid: ID chủ sở hữu (FK).
    """
    __tablename__ = "address"
    id = Column(Integer, primary_key=True, index=True)
    detail = Column(Text)
    phone = Column(String(255))
    userid = Column(Integer, ForeignKey("User.id"))

    user = relationship("User", back_populates="addresses")
    orders = relationship("Orders", back_populates="address")


class Orders(Base):
    """
    Đơn hàng trong hệ thống.

    Attributes:
        id: Định danh duy nhất.
        status: Trạng thái: "pending", "paid", "confirmed", "delivering", "completed", "cancelled".
        createdat: Thời gian tạo (tự động).
        preorderdate: Ngày giao dự kiến (đơn đặt trước).
        preordertime: Giờ giao dự kiến (đơn đặt trước).
        totalprice: Tổng tiền (VNĐ).
        restaurantid: ID nhà hàng (FK).
        addressid: ID địa chỉ giao hàng (FK).
        userid: ID người đặt (FK).
    """
    __tablename__ = "orders"
    id = Column(Integer, primary_key=True, index=True)
    status = Column(String(255), nullable=False)
    createdat = Column(DateTime, server_default=func.now())
    preorderdate = Column(Date)
    preordertime = Column(Time)
    totalprice = Column(Integer)
    restaurantid = Column(Integer, ForeignKey("restaurant.id"))
    addressid = Column(Integer, ForeignKey("address.id"))
    userid = Column(Integer, ForeignKey("User.id"))

    user = relationship("User", back_populates="orders")
    restaurant = relationship("Restaurant", back_populates="orders")
    address = relationship("Address", back_populates="orders")
    order_items = relationship("OrderItem", back_populates="order")
    payments = relationship("Payment", back_populates="order")
    delivery = relationship("Delivery", back_populates="order")
    used_promotions = relationship("UsedPromotion", back_populates="order")
    notifications = relationship("Notification", back_populates="order")


class OrderItem(Base):
    """
    Chi tiết một món ăn trong đơn hàng.

    Attributes:
        id: Định danh duy nhất.
        quantity: Số lượng.
        price: Đơn giá (VNĐ).
        menuitemid: ID món ăn (FK).
        orderid: ID đơn hàng (FK).
    """
    __tablename__ = "orderitem"
    id = Column(Integer, primary_key=True, index=True)
    quantity = Column(Integer, nullable=False)
    price = Column(Integer, nullable=False)
    menuitemid = Column(Integer, ForeignKey("menuitem.id"))
    orderid = Column(Integer, ForeignKey("orders.id"))

    order = relationship("Orders", back_populates="order_items")
    menu_item = relationship("MenuItem", back_populates="order_items")


class Payment(Base):
    """
    Thông tin thanh toán.

    Attributes:
        id: Định danh duy nhất.
        status: Trạng thái: "success", "failed", "pending".
        method: Phương thức: "vnpay", "cod".
        orderid: ID đơn hàng (FK).
    """
    __tablename__ = "payment"
    id = Column(Integer, primary_key=True, index=True)
    status = Column(String(255), nullable=False)
    method = Column(String(255))
    orderid = Column(Integer, ForeignKey("orders.id"))
    order = relationship("Orders", back_populates="payments")


class Delivery(Base):
    """
    Thông tin giao hàng.

    Attributes:
        id: Định danh duy nhất.
        status: Trạng thái giao hàng.
        deliverytime: Thời gian giao.
        orderid: ID đơn hàng (FK).
        shipperid: ID người giao hàng (FK).
    """
    __tablename__ = "delivery"
    id = Column(Integer, primary_key=True, index=True)
    status = Column(String(255), nullable=False)
    deliverytime = Column(Time)
    orderid = Column(Integer, ForeignKey("orders.id"))
    shipperid = Column(Integer, ForeignKey("shipper.id"))

    order = relationship("Orders", back_populates="delivery")
    shipper = relationship("Shipper", back_populates="deliveries")


class Review(Base):
    """
    Đánh giá của người dùng cho đơn hàng.

    Attributes:
        id: Định danh duy nhất.
        rating: Số sao (1-5).
        comment: Bình luận (có thể null).
        menuitemid: ID món ăn (FK, có thể null).
        restaurantid: ID nhà hàng (FK).
        userid: ID người đánh giá (FK).
        orderid: ID đơn hàng (FK).
    """
    __tablename__ = "review"
    id = Column(Integer, primary_key=True, index=True)
    rating = Column(Integer)
    comment = Column(Text)
    menuitemid = Column(Integer, ForeignKey("menuitem.id"))
    restaurantid = Column(Integer, ForeignKey("restaurant.id"))
    userid = Column(Integer, ForeignKey("User.id"))
    orderid = Column(Integer, ForeignKey("orders.id"))

    user = relationship("User", back_populates="reviews")
    menu_item = relationship("MenuItem", back_populates="reviews")
    restaurant = relationship("Restaurant", back_populates="reviews")
    order = relationship("Orders")


class Notification(Base):
    """
    Thông báo cho người dùng.

    Attributes:
        id: Định danh duy nhất.
        title: Tiêu đề.
        type: Loại thông báo: "order_update", "promotion"...
        content: Nội dung.
        isread: Đã đọc hay chưa.
        createdat: Thời gian tạo (tự động).
        userid: ID người nhận (FK).
        orderid: ID đơn hàng liên quan (FK, có thể null).
        sessionid: ID phiên chat liên quan (FK, có thể null).
    """
    __tablename__ = "notification"
    id = Column(Integer, primary_key=True, index=True)
    title = Column(String(255))
    type = Column(String(255))
    content = Column(Text)
    isread = Column(Boolean, default=False)
    createdat = Column(DateTime, server_default=func.now())
    userid = Column(Integer, ForeignKey("User.id"))
    orderid = Column(Integer, ForeignKey("orders.id"), nullable=True)
    sessionid = Column(Integer, ForeignKey("chatsession.id"), nullable=True)

    user = relationship("User", back_populates="notifications")
    order = relationship("Orders", back_populates="notifications")
    session = relationship("ChatSession", back_populates="notifications")


class ChatSession(Base):
    """
    Phiên chat với chatbot.

    Attributes:
        id: Định danh duy nhất.
        createdat: Thời gian tạo (tự động).
        status: Trạng thái: "active", "closed".
        userid: ID người dùng (FK).
    """
    __tablename__ = "chatsession"
    id = Column(Integer, primary_key=True, index=True)
    createdat = Column(DateTime, server_default=func.now())
    status = Column(String(255), nullable=False)
    userid = Column(Integer, ForeignKey("User.id"))

    user = relationship("User", back_populates="chat_sessions")
    messages = relationship("ChatMessage", back_populates="session")
    notifications = relationship("Notification", back_populates="session")


class ChatMessage(Base):
    """
    Tin nhắn trong phiên chat.

    Attributes:
        id: Định danh duy nhất.
        senderrole: Vai trò người gửi: "user" hoặc "bot".
        message: Nội dung tin nhắn.
        sentat: Thời gian gửi (tự động).
        sessionid: ID phiên chat (FK).
    """
    __tablename__ = "chatmessage"
    id = Column(Integer, primary_key=True, index=True)
    senderrole = Column(String(255))
    message = Column(Text)
    sentat = Column(DateTime, server_default=func.now())
    sessionid = Column(Integer, ForeignKey("chatsession.id"))

    session = relationship("ChatSession", back_populates="messages")


class UserDevice(Base):
    """
    Thiết bị của người dùng (để gửi push notification).

    Attributes:
        id: Định danh duy nhất.
        device_token: FCM token.
        device_type: Loại thiết bị: "android", "ios".
        last_active: Thời gian hoạt động cuối.
        userid: ID người dùng (FK).
    """
    __tablename__ = "userdevice"
    id = Column(Integer, primary_key=True, index=True)
    device_token = Column(String(255), nullable=False)
    device_type = Column(String(255))
    last_active = Column(DateTime, server_default=func.now())
    userid = Column(Integer, ForeignKey("User.id"))

    user = relationship("User", back_populates="user_devices")


class UserFAQ(Base):
    """
    Lịch sử xem FAQ của người dùng.

    Attributes:
        id: Định danh duy nhất.
        viewedat: Thời gian xem (tự động).
        userid: ID người dùng (FK).
        faqid: ID câu hỏi (FK).
    """
    __tablename__ = "userfaq"
    id = Column(Integer, primary_key=True, index=True)
    viewedat = Column(DateTime, server_default=func.now())
    userid = Column(Integer, ForeignKey("User.id"))
    faqid = Column(Integer, ForeignKey("faq.id"))

    user = relationship("User", back_populates="user_faqs")
    faq = relationship("FAQ", back_populates="user_faqs")


class UsedPromotion(Base):
    """
    Mã giảm giá đã sử dụng.

    Attributes:
        id: Định danh duy nhất.
        usedat: Ngày sử dụng.
        promotionid: ID mã giảm giá (FK).
        orderid: ID đơn hàng (FK).
    """
    __tablename__ = "usedpromotion"
    id = Column(Integer, primary_key=True, index=True)
    usedat = Column(Date)
    promotionid = Column(Integer, ForeignKey("promotion.id"))
    orderid = Column(Integer, ForeignKey("orders.id"))

    promotion = relationship("Promotion", back_populates="used_promotions")
    order = relationship("Orders", back_populates="used_promotions")


class LoyaltyPoint(Base):
    """
    Điểm tích lũy của người dùng (KHÔNG DÙNG - điểm tính động từ orders).

    Attributes:
        id: Định danh duy nhất.
        points: Số điểm.
        updatedat: Ngày cập nhật.
        userid: ID người dùng (FK).
        menuitemid: ID món ăn (FK).
    """
    __tablename__ = "loyaltypoint"
    id = Column(Integer, primary_key=True, index=True)
    points = Column(Integer, default=0)
    updatedat = Column(Date)
    userid = Column(Integer, ForeignKey("User.id"))
    menuitemid = Column(Integer, ForeignKey("menuitem.id"))

    user = relationship("User", back_populates="loyalty_points")
    menu_item = relationship("MenuItem")


class SocialShare(Base):
    """
    Chia sẻ mạng xã hội.

    Attributes:
        id: Định danh duy nhất.
        sharetype: Loại chia sẻ.
        platform: Nền tảng: "facebook", "zalo"...
        context: Nội dung chia sẻ.
        createdat: Ngày chia sẻ.
        userid: ID người dùng (FK).
        restaurantid: ID nhà hàng (FK).
        menuitemid: ID món ăn (FK).
    """
    __tablename__ = "socialshare"
    id = Column(Integer, primary_key=True, index=True)
    sharetype = Column(String(255))
    platform = Column(String(255))
    context = Column(Text)
    createdat = Column(Date)
    userid = Column(Integer, ForeignKey("User.id"))
    restaurantid = Column(Integer, ForeignKey("restaurant.id"))
    menuitemid = Column(Integer, ForeignKey("menuitem.id"))

    user = relationship("User", back_populates="social_shares")
    restaurant = relationship("Restaurant", back_populates="social_shares")
    menu_item = relationship("MenuItem", back_populates="social_shares")

