"""
Module: main.py

FastAPI application chính của hệ thống Đặt Món.
Định nghĩa tất cả REST API endpoints: auth, user, restaurant, menu, order, payment, chatbot, notification.
"""

from fastapi import FastAPI, Depends, HTTPException, File, UploadFile, Form, status
from fastapi.security import OAuth2PasswordRequestForm
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import List, Optional
from decimal import Decimal
from datetime import timedelta, datetime


import models, schemas, auth, os
from database import engine, get_db
from firebase_utils import upload_image
from chatbot_service import ChatBotService
from notification_service import notify_user


from payment_service import generate_vnpay_url
from fastapi import Request


# Create tables in the database (optional if using migrations like Alembic)
models.Base.metadata.create_all(bind=engine)


app = FastAPI(title="Đặt Món Food Delivery API")


# CORS middleware - Cho phép Android app kết nối
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Cho phép tất cả origins (development)
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
def read_root():
    """Endpoint gốc, trả lời chào mừng API."""
    return {"message": "Welcome to Đặt Món Food Delivery API"}


# --- AUTH ENDPOINTS ---


@app.post("/token", response_model=schemas.Token)
async def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    """Đăng nhập bằng username/password, trả về JWT access token."""
    user = db.query(models.User).filter(models.User.username == form_data.username).first()
    if not user or not auth.verify_password(form_data.password, user.password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    access_token_expires = timedelta(minutes=auth.ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = auth.create_access_token(
        data={"sub": user.email}, expires_delta=access_token_expires
    )
    return {"access_token": access_token, "token_type": "bearer"}


# --- USER ENDPOINTS ---


@app.post("/users/", response_model=schemas.User)
def create_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    """Đăng ký tài khoản người dùng mới."""
    # Check if user exists
    db_user = db.query(models.User).filter(models.User.email == user.email).first()
    if db_user:
        raise HTTPException(status_code=400, detail="Email already registered")
       
    hashed_password = auth.get_password_hash(user.password)
    db_user = models.User(
        name=user.name,
        username=user.username,
        email=user.email,
        password=hashed_password,
        phone=user.phone,
        role=user.role
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user


@app.get("/users/me/", response_model=schemas.User)
async def read_users_me(current_user: models.User = Depends(auth.get_current_user)):
    """Lấy thông tin người dùng hiện tại từ JWT token."""
    return current_user


@app.post("/devices/token")
async def update_device_token(
    token: str = Form(...),
    device_type: str = Form("android"),
    current_user: models.User = Depends(auth.get_current_user),
    db: Session = Depends(get_db)
):
    """Android App gọi API này để gửi FCM Token lên Server."""
    # Kiểm tra xem token này đã có chưa
    db_device = db.query(models.UserDevice).filter(models.UserDevice.device_token == token).first()
    if not db_device:
        db_device = models.UserDevice(device_token=token, device_type=device_type, userid=current_user.id)
        db.add(db_device)
    else:
        db_device.userid = current_user.id
        # Cập nhật thời gian hoạt động
   
    db.commit()
    return {"message": "Device token updated successfully"}


@app.get("/users/", response_model=List[schemas.User])
def read_users(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    """Lấy danh sách tất cả người dùng (phân trang)."""
    users = db.query(models.User).offset(skip).limit(limit).all()
    return users


# --- ADDRESS ENDPOINTS ---


@app.post("/addresses/", response_model=schemas.Address)
def create_address(address: schemas.AddressCreate, db: Session = Depends(get_db)):
    """Tạo địa chỉ giao hàng mới cho người dùng."""
    db_address = models.Address(detail=address.detail, phone=address.phone, userid=address.userid)
    db.add(db_address)
    db.commit()
    db.refresh(db_address)
    return db_address


@app.get("/users/{user_id}/addresses/", response_model=List[schemas.Address])
def read_user_addresses(user_id: int, db: Session = Depends(get_db)):
    """Lấy danh sách địa chỉ giao hàng của người dùng."""
    return db.query(models.Address).filter(models.Address.userid == user_id).all()


@app.put("/addresses/{address_id}/", response_model=schemas.Address)
def update_address(address_id: int, address: schemas.AddressUpdate, db: Session = Depends(get_db)):
    """Cập nhật địa chỉ giao hàng."""
    db_address = db.query(models.Address).filter(models.Address.id == address_id).first()
    if not db_address:
        raise HTTPException(status_code=404, detail="Address not found")
   
    # Cập nhật các trường
    if address.detail is not None:
        db_address.detail = address.detail
    if address.phone is not None:
        db_address.phone = address.phone
   
    db.commit()
    db.refresh(db_address)
    return db_address


# --- CATEGORY ENDPOINTS ---


@app.get("/categories/", response_model=List[schemas.Category])
def read_categories(db: Session = Depends(get_db)):
    """Lấy danh sách tất cả danh mục món ăn."""
    return db.query(models.Category).all()


@app.get("/promotions/", response_model=List[schemas.Promotion])
def read_promotions(active_only: bool = True, db: Session = Depends(get_db)):
    """Lấy danh sách mã khuyến mãi, mặc định chỉ lấy mã đang active."""
    query = db.query(models.Promotion)
    if active_only:
        query = query.filter(models.Promotion.status.ilike("active"))
    return query.all()


# --- CHAT ENDPOINTS ---


@app.post("/chat/", response_model=schemas.ChatMessage)
async def chat_with_bot(
    """Gửi tin nhắn đến chatbot AI và nhận phản hồi."""
    chat_input: schemas.ChatMessageCreate,
    session_id: Optional[int] = None,
    current_user: models.User = Depends(auth.get_current_user),
    db: Session = Depends(get_db)
):
    # 1. Tìm hoặc tạo ChatSession
    if session_id:
        session = db.query(models.ChatSession).filter(models.ChatSession.id == session_id).first()
        if not session:
            raise HTTPException(status_code=404, detail="Chat Session not found")
    else:
        session = models.ChatSession(status="active", userid=current_user.id)
        db.add(session)
        db.commit()
        db.refresh(session)


    # 2. Lưu tin nhắn người dùng
    user_msg = models.ChatMessage(senderrole="user", message=chat_input.message, sessionid=session.id)
    db.add(user_msg)
    db.commit() # Lưu trước để AI có thể đọc lịch sử nếu cần (trong tương lai)
   
    # 3. Gọi AI xử lý
    bot_service = ChatBotService(db, user_id=current_user.id)
    bot_response_text = await bot_service.get_response(chat_input.message)
   
    # 4. Lưu phản hồi của AI
    bot_msg = models.ChatMessage(senderrole="bot", message=bot_response_text, sessionid=session.id)
    db.add(bot_msg)
   
    db.commit()
    db.refresh(bot_msg)
   
    return bot_msg


# --- RESTAURANT ENDPOINTS ---


@app.post("/restaurants/", response_model=schemas.Restaurant)
async def create_restaurant(
    """Tạo nhà hàng mới với ảnh upload lên Firebase Storage."""
    name: str = Form(...),
    address: Optional[str] = Form(None),
    phone_number: str = Form(...),
    status: str = Form(...),
    description: Optional[str] = Form(None),
    image: Optional[UploadFile] = File(None),
    db: Session = Depends(get_db)
):
    image_url = None
    if image:
        content = await image.read()
        image_url = await upload_image(content, image.filename)
   
    db_restaurant = models.Restaurant(
        name=name,
        address=address,
        phone_number=phone_number,
        status=status,
        description=description,
        image_url=image_url
    )
    db.add(db_restaurant)
    db.commit()
    db.refresh(db_restaurant)
    return db_restaurant


@app.get("/restaurants/", response_model=List[schemas.Restaurant])
def read_restaurants(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    """Lấy danh sách tất cả nhà hàng (phân trang)."""
    restaurants = db.query(models.Restaurant).offset(skip).limit(limit).all()
    return restaurants


@app.get("/restaurants/search/", response_model=List[schemas.Restaurant])
def search_restaurants_by_name(name: str, skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    """Tìm kiếm nhà hàng theo tên (không phân biệt hoa thường)."""
    restaurants = db.query(models.Restaurant).filter(
        models.Restaurant.name.ilike(f"%{name}%")
    ).offset(skip).limit(limit).all()
    return restaurants


@app.get("/restaurants/{restaurant_id}/", response_model=schemas.Restaurant)
def read_restaurant(restaurant_id: int, db: Session = Depends(get_db)):
    """Lấy chi tiết nhà hàng theo ID."""
    restaurant = db.query(models.Restaurant).filter(models.Restaurant.id == restaurant_id).first()
    if not restaurant:
        raise HTTPException(status_code=404, detail="Restaurant not found")
    return restaurant


# --- MENU ITEM ENDPOINTS ---


@app.post("/menu-items/", response_model=schemas.MenuItem)
async def create_menu_item(
    """Tạo món ăn mới với ảnh upload lên Firebase Storage."""
    name: str = Form(...),
    price: Decimal = Form(...),
    description: Optional[str] = Form(None),
    restaurantid: int = Form(...),
    categoryid: int = Form(...),
    image: Optional[UploadFile] = File(None),
    db: Session = Depends(get_db)
):
    image_url = None
    if image:
        content = await image.read()
        image_url = await upload_image(content, image.filename)
       
    db_item = models.MenuItem(
        name=name,
        price=price,
        description=description,
        restaurantid=restaurantid,
        categoryid=categoryid,
        image_url=image_url
    )
    db.add(db_item)
    db.commit()
    db.refresh(db_item)
    return db_item


def build_menuitem_with_reviews(item: models.MenuItem, db: Session):
    """Helper function to build MenuItem with reviews and avg_rating."""
    restaurant = db.query(models.Restaurant).filter(models.Restaurant.id == item.restaurantid).first()
    restaurant_name = restaurant.name if restaurant else ""
    
    # Fetch reviews for this menu item
    reviews = db.query(models.Review).filter(models.Review.menuitemid == item.id).all()
    reviews_data = []
    total_rating = 0
    
    for review in reviews:
        user = db.query(models.User).filter(models.User.id == review.userid).first()
        user_name = user.name if user else "Unknown"
        reviews_data.append({
            "id": review.id,
            "rating": review.rating,
            "comment": review.comment,
            "userid": review.userid,
            "user_name": user_name
        })
        total_rating += review.rating
    
    avg_rating = total_rating / len(reviews) if reviews else 0.0
    
    return {
        "id": item.id,
        "name": item.name,
        "image_url": item.image_url,
        "price": item.price,
        "is_available": item.is_available,
        "description": item.description,
        "restaurantid": item.restaurantid,
        "categoryid": item.categoryid,
        "restaurant_name": restaurant_name,
        "reviews": reviews_data,
        "avg_rating": avg_rating
    }


@app.get("/menu-items/", response_model=List[schemas.MenuItemWithReviews])
def read_all_menu_items(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    """Lấy danh sách tất cả món ăn từ tất cả nhà hàng, bao gồm reviews."""
    menu_items = db.query(models.MenuItem).offset(skip).limit(limit).all()
   
    result = []
    for item in menu_items:
        item_dict = build_menuitem_with_reviews(item, db)
        result.append(item_dict)
    return result


@app.get("/restaurants/{restaurant_id}/menu/", response_model=List[schemas.MenuItem])
def read_restaurant_menu(restaurant_id: int, db: Session = Depends(get_db)):
    """Lấy danh sách món ăn của một nhà hàng."""
    return db.query(models.MenuItem).filter(models.MenuItem.restaurantid == restaurant_id).all()


@app.get("/menu-items/search/", response_model=List[schemas.MenuItemWithReviews])
def search_menu_items_by_name(name: str, skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    """Tìm kiếm món ăn theo tên (không phân biệt hoa thường)."""
    menu_items = db.query(models.MenuItem).filter(
        models.MenuItem.name.ilike(f"%{name}%")
    ).offset(skip).limit(limit).all()
   
    result = []
    for item in menu_items:
        item_dict = build_menuitem_with_reviews(item, db)
        result.append(item_dict)
    return result


@app.get("/menu-items/category/search/", response_model=List[schemas.MenuItemWithReviews])
def search_menu_items_by_category_name(category_name: str, skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    """Tìm kiếm món ăn theo tên danh mục (không phân biệt hoa thường)."""
    # Tìm category theo tên
    categories = db.query(models.Category).filter(
        models.Category.name.ilike(f"%{category_name}%")
    ).all()
    category_ids = [cat.id for cat in categories]
   
    if not category_ids:
        return []
   
    menu_items = db.query(models.MenuItem).filter(
        models.MenuItem.categoryid.in_(category_ids)
    ).offset(skip).limit(limit).all()
   
    result = []
    for item in menu_items:
        item_dict = build_menuitem_with_reviews(item, db)
        result.append(item_dict)
    return result


@app.get("/menu-items/{menu_item_id}/", response_model=schemas.MenuItemWithReviews)
def read_menu_item_detail(menu_item_id: int, db: Session = Depends(get_db)):
    """Lấy chi tiết món ăn bao gồm reviews."""
    item = db.query(models.MenuItem).filter(models.MenuItem.id == menu_item_id).first()
    if not item:
        raise HTTPException(status_code=404, detail="Menu item not found")
    
    return build_menuitem_with_reviews(item, db)
# --- VNPAY RETURN ---
@app.get("/vnpay_return")
async def vnpay_return(request: Request, db: Session = Depends(get_db)):
    """Callback từ VNPay sau khi thanh toán, cập nhật trạng thái đơn hàng."""


    params = dict(request.query_params)


    response_code = params.get("vnp_ResponseCode")
    order_ref = params.get("vnp_TxnRef")  # Format: DH{order_id}
    
    # Trích xuất order_id từ reference (VD: "DH123" -> 123)
    try:
        # Nếu là định dạng "DH{id}"
        if order_ref and order_ref.startswith("DH"):
            order_id = int(order_ref[2:])
        else:
            order_id = int(order_ref) if order_ref else 0
    except (ValueError, AttributeError):
        order_id = 0
    

    if response_code == "00":
        # Thanh toán thành công - cập nhật đơn hàng trong DB
        if order_id > 0:
            order = db.query(models.Orders).filter(models.Orders.id == order_id).first()
            if order:
                order.status = "paid"
                
                # Tạo Payment record
                payment_record = models.Payment(
                    status="success",
                    method="vnpay",
                    orderid=order.id
                )
                db.add(payment_record)
                db.commit()
                db.refresh(order)
                
                # Gửi notification
                total_formatted = f"{order.totalprice:,.0f}".replace(",", ".")
                notify_user(
                    db=db,
                    user_id=order.userid,
                    title="Đơn hàng được đặt thành công! 🎉",
                    body=f"Đơn hàng #{order.id} đã được đặt thành công. Tổng tiền: {total_formatted}đ. Đang chờ quán xác nhận.",
                    order_id=order.id
                )
        
        return {
            "status": "success",
            "message": f"Thanh toán thành công cho đơn {order_ref}"
        }
    else:
        # Thanh toán thất bại - cập nhật Payment record
        if order_id > 0:
            payment_record = models.Payment(
                status="failed",
                method="vnpay",
                orderid=order_id
            )
            db.add(payment_record)
            db.commit()
        
        return {
            "status": "failed",
            "message": "Thanh toán thất bại"
        }


    # --- PAYMENT ENDPOINTS ---


@app.get("/create-payment")
async def create_payment(
    """Tạo URL thanh toán VNPay cho đơn hàng."""
    order_id: str,
    amount: int,
    request: Request
):
    ip_address = request.client.host


    payment_url = generate_vnpay_url(
        order_id=str(order_id),
        amount=amount,
        ip_address=ip_address
    )


    return {
        "payment_url": payment_url
    }


# --- ORDER ENDPOINTS ---


@app.post("/orders/", response_model=schemas.Order)
def create_order(order: schemas.OrderCreate, db: Session = Depends(get_db)):
    """Tạo đơn hàng mới kèm danh sách món ăn và gửi thông báo."""
    db_order = models.Orders(
        status=order.status,
        preorderdate=order.preorderdate,
        preordertime=order.preordertime,
        totalprice=order.totalprice,
        restaurantid=order.restaurantid,
        addressid=order.addressid,
        userid=order.userid
    )
    db.add(db_order)
    db.commit()
    db.refresh(db_order)
   
    for item in order.order_items:
        db_order_item = models.OrderItem(
            quantity=item.quantity,
            price=item.price,
            menuitemid=item.menuitemid,
            orderid=db_order.id
        )
        db.add(db_order_item)
   
    db.commit()
    db.refresh(db_order)
    
    # Nếu là COD (status="confirmed"), gửi thông báo ngay lúc tạo đơn hàng
    if db_order.status == "confirmed":
        try:
            total_formatted = f"{db_order.totalprice:,.0f}".replace(",", ".")
            notify_user(
                db=db,
                user_id=db_order.userid,
                title="Đơn hàng được đặt thành công! 🎉",
                body=f"Đơn hàng #{db_order.id} đã được đặt thành công. Tổng tiền: {total_formatted}đ. Đang chờ quán xác nhận.",
                order_id=db_order.id
            )
        except Exception as e:
            print(f"Error sending notification: {e}")
   
    return db_order


@app.get("/users/{user_id}/orders/", response_model=List[schemas.Order])
def read_user_orders(user_id: int, db: Session = Depends(get_db)):
    """Lấy danh sách đơn hàng của người dùng."""
    return db.query(models.Orders).filter(models.Orders.userid == user_id).all()


@app.get("/orders/{order_id}/detail", response_model=schemas.OrderDetail)
def read_order_detail(order_id: int, db: Session = Depends(get_db)):
    """Lấy chi tiết đơn hàng kèm tên nhà hàng, địa chỉ và danh sách món."""
    order = db.query(models.Orders).filter(models.Orders.id == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")

    restaurant_name = order.restaurant.name if order.restaurant else None
    address_detail = order.address.detail if order.address else None

    order_items = []
    for item in order.order_items:
        order_items.append({
            "id": item.id,
            "quantity": item.quantity,
            "price": item.price,
            "menuitemid": item.menuitemid,
            "menuitem_name": item.menu_item.name if item.menu_item else None,
            "image_url": item.menu_item.image_url if item.menu_item else None
        })

    return {
        "id": order.id,
        "status": order.status,
        "preorderdate": order.preorderdate,
        "preordertime": order.preordertime,
        "totalprice": order.totalprice,
        "restaurantid": order.restaurantid,
        "addressid": order.addressid,
        "userid": order.userid,
        "createdat": order.createdat,
        "restaurant_name": restaurant_name,
        "address_detail": address_detail,
        "order_items": order_items
    }


@app.put("/orders/{order_id}/status", response_model=schemas.Order)
def update_order_status(order_id: int, payload: schemas.OrderStatusUpdate, db: Session = Depends(get_db)):
    """Cập nhật trạng thái đơn hàng và gửi thông báo tương ứng."""
    order = db.query(models.Orders).filter(models.Orders.id == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")

    old_status = order.status
    order.status = payload.status
    db.commit()
    db.refresh(order)
    
    print(f"DEBUG: Update order {order_id}: {old_status} -> {payload.status}")
    
    # Tạo thông báo khi trạng thái đơn hàng thay đổi
    try:
        notification_title = ""
        notification_body = ""
        
        if payload.status in ["paid", "confirmed"] and old_status not in ["paid", "confirmed"]:
            notification_title = "Đơn hàng được đặt thành công! ✅"
            total_formatted = f"{order.totalprice:,.0f}".replace(",", ".")
            notification_body = f"Đơn hàng #{order.id} đã được đặt thành công. Tổng tiền: {total_formatted}đ. Đang chờ quán xác nhận."
            print(f"DEBUG: Sending notification for order {order_id}")
        elif payload.status == "delivering":
            notification_title = "Đơn hàng đang giao 🚚"
            notification_body = f"Đơn hàng #{order.id} đang trên đường giao đến bạn"
        elif payload.status == "completed":
            notification_title = "Đơn hàng đã giao 🎉"
            notification_body = f"Đơn hàng #{order.id} đã giao thành công. Cảm ơn đã đặt hàng!"
        elif payload.status == "cancelled":
            notification_title = "Đơn hàng bị hủy ❌"
            notification_body = f"Đơn hàng #{order.id} đã bị hủy"
        
        # Nếu có thay đổi trạng thái, tạo thông báo
        if notification_title:
            print(f"DEBUG: Sending '{notification_title}' to user {order.userid}")
            notify_user(
                db=db,
                user_id=order.userid,
                title=notification_title,
                body=notification_body,
                order_id=order.id
            )
            print(f"DEBUG: Notification sent successfully")
    except Exception as e:
        print(f"Lỗi khi tạo thông báo trạng thái: {str(e)}")
    
    return order


@app.post("/reviews/", response_model=schemas.Review)
def create_review(review: schemas.ReviewCreate, db: Session = Depends(get_db)):
    """Tạo đánh giá cho đơn hàng và cập nhật điểm trung bình nhà hàng."""
    order = db.query(models.Orders).filter(models.Orders.id == review.orderid).first()
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")

    if order.userid != review.userid:
        raise HTTPException(status_code=403, detail="Review does not belong to this user")

    order_item = db.query(models.OrderItem).filter(models.OrderItem.orderid == order.id).first()
    if not order_item:
        raise HTTPException(status_code=400, detail="Order has no items to review")

    menuitemid = review.menuitemid or order_item.menuitemid
    restaurantid = review.restaurantid or order.restaurantid

    db_review = models.Review(
        rating=review.rating,
        comment=review.comment,
        orderid=review.orderid,
        menuitemid=menuitemid,
        restaurantid=restaurantid,
        userid=review.userid,
    )
    db.add(db_review)
    db.commit()

    # Tính lại điểm đánh giá trung bình của nhà hàng
    restaurant = db.query(models.Restaurant).filter(models.Restaurant.id == restaurantid).first()
    if restaurant:
        all_reviews = db.query(models.Review).filter(models.Review.restaurantid == restaurantid).all()
        if all_reviews:
            avg_rating = sum(r.rating for r in all_reviews) / len(all_reviews)
            restaurant.rating = int(round(avg_rating))
            db.add(restaurant)
            db.commit()

    db.refresh(db_review)
    return db_review


@app.get("/users/{user_id}/profile-summary", response_model=schemas.UserProfileSummary)
def read_user_profile_summary(user_id: int, db: Session = Depends(get_db)):
    """Lấy tóm tắt hồ sơ người dùng: điểm, đơn hàng đã giao, tổng chi tiêu."""
    user = db.query(models.User).filter(models.User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    orders = db.query(models.Orders).filter(models.Orders.userid == user_id).all()
    delivered_orders = [o for o in orders if (o.status or "").lower() in {"delivered", "completed", "done"}]
    total_spent = sum(o.totalprice or 0 for o in orders)

    # Quy ước điểm đơn giản để Android hiển thị động thay cho số cứng.
    points = total_spent // 10000

    return {
        "user_id": user.id,
        "user_name": user.name,
        "points": points,
        "delivered_orders": len(delivered_orders),
        "total_spent": total_spent
    }


# --- FAQ ENDPOINTS ---


@app.get("/faqs/", response_model=List[schemas.FAQ])
def read_faqs(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    """Lấy danh sách các câu hỏi thường gặp (FAQ)."""
    return db.query(models.FAQ).filter(models.FAQ.isactive == True).offset(skip).limit(limit).all()


# --- NOTIFICATION ENDPOINTS ---


@app.get("/users/{user_id}/notifications/", response_model=List[schemas.Notification])
def read_user_notifications(user_id: int, db: Session = Depends(get_db)):
    """Lấy danh sách thông báo của người dùng."""
    return db.query(models.Notification).filter(models.Notification.userid == user_id).order_by(models.Notification.createdat.desc()).all()


@app.post("/notifications/", response_model=schemas.Notification)
def create_notification(notification: schemas.NotificationCreate, db: Session = Depends(get_db)):
    """Tạo thông báo mới và gửi push notification."""
    # Sử dụng notify_user để tạo thông báo - nó sẽ vừa lưu vào DB vừa gửi push
    notify_user(
        db=db,
        user_id=notification.userid,
        title=notification.title,
        body=notification.content,
        order_id=notification.orderid
    )
    
    # Lấy thông báo vừa tạo để trả về
    db_notification = db.query(models.Notification).filter(
        models.Notification.userid == notification.userid,
        models.Notification.title == notification.title
    ).order_by(models.Notification.id.desc()).first()
    
    return db_notification if db_notification else {
        "id": 0,
        "title": notification.title,
        "type": notification.type,
        "content": notification.content,
        "isread": notification.isread,
        "createdat": datetime.now(),
        "userid": notification.userid,
        "orderid": notification.orderid,
        "sessionid": notification.sessionid
    }


@app.put("/notifications/{notification_id}/read")
def mark_notification_as_read(notification_id: int, db: Session = Depends(get_db)):
    """Đánh dấu thông báo đã đọc."""
    db_notification = db.query(models.Notification).filter(models.Notification.id == notification_id).first()
    if not db_notification:
        raise HTTPException(status_code=404, detail="Notification not found")
   
    db_notification.isread = True
    db.commit()
    return {"message": "Notification marked as read"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)



