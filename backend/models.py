from sqlalchemy import Column, Integer, String, Boolean, Numeric, Text, Date, Time, ForeignKey, DateTime, func
from sqlalchemy.orm import relationship
from database import Base

class Category(Base):
    __tablename__ = "category"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    type = Column(String(255))
    description = Column(Text)
    menu_items = relationship("MenuItem", back_populates="category")

class FAQ(Base):
    __tablename__ = "faq"
    id = Column(Integer, primary_key=True, index=True)
    question = Column(String(255))
    answer = Column(Text)
    isactive = Column(Boolean, default=True)
    user_faqs = relationship("UserFAQ", back_populates="faq")

class User(Base):
    __tablename__ = "User"  # Quoted in SQL as "User"
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
    __tablename__ = "shipper"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    phone = Column(String(255), nullable=False)
    rating = Column(Integer)
    description = Column(Text)
    status = Column(String(255), nullable=False)
    deliveries = relationship("Delivery", back_populates="shipper")

class Promotion(Base):
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
    __tablename__ = "address"
    id = Column(Integer, primary_key=True, index=True)
    detail = Column(Text)
    phone = Column(String(255))
    userid = Column(Integer, ForeignKey("User.id"))

    user = relationship("User", back_populates="addresses")
    orders = relationship("Orders", back_populates="address")

class Orders(Base):
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
    __tablename__ = "orderitem"
    id = Column(Integer, primary_key=True, index=True)
    quantity = Column(Integer, nullable=False)
    price = Column(Integer, nullable=False)
    menuitemid = Column(Integer, ForeignKey("menuitem.id"))
    orderid = Column(Integer, ForeignKey("orders.id"))

    order = relationship("Orders", back_populates="order_items")
    menu_item = relationship("MenuItem", back_populates="order_items")

class Payment(Base):
    __tablename__ = "payment"
    id = Column(Integer, primary_key=True, index=True)
    status = Column(String(255), nullable=False)
    method = Column(String(255))
    orderid = Column(Integer, ForeignKey("orders.id"))
    order = relationship("Orders", back_populates="payments")

class Delivery(Base):
    __tablename__ = "delivery"
    id = Column(Integer, primary_key=True, index=True)
    status = Column(String(255), nullable=False)
    deliverytime = Column(Time)
    orderid = Column(Integer, ForeignKey("orders.id"))
    shipperid = Column(Integer, ForeignKey("shipper.id"))

    order = relationship("Orders", back_populates="delivery")
    shipper = relationship("Shipper", back_populates="deliveries")

class Review(Base):
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
    __tablename__ = "chatsession"
    id = Column(Integer, primary_key=True, index=True)
    createdat = Column(DateTime, server_default=func.now())
    status = Column(String(255), nullable=False)
    userid = Column(Integer, ForeignKey("User.id"))

    user = relationship("User", back_populates="chat_sessions")
    messages = relationship("ChatMessage", back_populates="session")
    notifications = relationship("Notification", back_populates="session")

class ChatMessage(Base):
    __tablename__ = "chatmessage"
    id = Column(Integer, primary_key=True, index=True)
    senderrole = Column(String(255))  # "user" hoặc "bot"
    message = Column(Text)
    sentat = Column(DateTime, server_default=func.now())
    sessionid = Column(Integer, ForeignKey("chatsession.id"))

    session = relationship("ChatSession", back_populates="messages")

class UserDevice(Base):
    __tablename__ = "userdevice"
    id = Column(Integer, primary_key=True, index=True)
    device_token = Column(String(255), nullable=False)
    device_type = Column(String(255))
    last_active = Column(DateTime, server_default=func.now())
    userid = Column(Integer, ForeignKey("User.id"))

    user = relationship("User", back_populates="user_devices")

class UserFAQ(Base):
    __tablename__ = "userfaq"
    id = Column(Integer, primary_key=True, index=True)
    viewedat = Column(DateTime, server_default=func.now())
    userid = Column(Integer, ForeignKey("User.id"))
    faqid = Column(Integer, ForeignKey("faq.id"))

    user = relationship("User", back_populates="user_faqs")
    faq = relationship("FAQ", back_populates="user_faqs")

class UsedPromotion(Base):
    __tablename__ = "usedpromotion"
    id = Column(Integer, primary_key=True, index=True)
    usedat = Column(Date)
    promotionid = Column(Integer, ForeignKey("promotion.id"))
    orderid = Column(Integer, ForeignKey("orders.id"))

    promotion = relationship("Promotion", back_populates="used_promotions")
    order = relationship("Orders", back_populates="used_promotions")

class LoyaltyPoint(Base):
    __tablename__ = "loyaltypoint"
    id = Column(Integer, primary_key=True, index=True)
    points = Column(Integer, default=0)
    updatedat = Column(Date)
    userid = Column(Integer, ForeignKey("User.id"))
    menuitemid = Column(Integer, ForeignKey("menuitem.id"))

    user = relationship("User", back_populates="loyalty_points")
    menu_item = relationship("MenuItem")

class SocialShare(Base):
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

