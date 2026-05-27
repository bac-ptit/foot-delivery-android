"""
Module chính của API Đặt Món Food Delivery.

Chứa tất cả các endpoint REST API cho ứng dụng đặt đồ ăn giao hàng.
Sử dụng FastAPI framework với SQLAlchemy ORM và PostgreSQL database.

Các nhóm endpoint chính:
- Auth: Xác thực JWT
- User: Quản lý người dùng
- Address: Quản lý địa chỉ giao hàng
- Restaurant: Quản lý nhà hàng
- MenuItem: Quản lý món ăn
- Order: Đặt hàng và theo dõi đơn
- Review: Đánh giá và phản hồi
- Payment: Thanh toán VNPay
- Notification: Thông báo push
- Promotion: Mã giảm giá
"""

from fastapi import FastAPI, Depends, HTTPException, Form, status
from fastapi.security import OAuth2PasswordRequestForm
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import List, Optional
from decimal import Decimal
from datetime import timedelta, datetime

import models, schemas, auth
from database import engine, get_db
from notification_service import notify_user
from payment_service import generate_vnpay_url
from fastapi import Request

# Tạo bảng trong database (nếu chưa có)
models.Base.metadata.create_all(bind=engine)

# Khởi tạo FastAPI app
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
    """Endpoint gốc - kiểm tra API có hoạt động không."""
    return {"message": "Welcome to Đặt Món Food Delivery API"}


# ══════════════════════════════════════════════════════════════
# AUTH ENDPOINTS - Xác thực
# ══════════════════════════════════════════════════════════════


@app.post("/token", response_model=schemas.Token)
async def login_for_access_token(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: Session = Depends(get_db)
):
    """
    Đăng nhập và lấy JWT access token.

    Xác thực người dùng bằng username và password.
    Trả về access_token nếu thành công, raise 401 nếu thất bại.

    Args:
        form_data: Form chứa username và password.
        db: Database session.

    Returns:
        dict: Chứa access_token và token_type.

    Raises:
        HTTPException: 401 nếu username hoặc password sai.
    """
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


# ══════════════════════════════════════════════════════════════
# USER ENDPOINTS - Người dùng
# ══════════════════════════════════════════════════════════════


@app.get("/users/me/", response_model=schemas.User)
async def read_users_me(current_user: models.User = Depends(auth.get_current_user)):
    """
    Lấy thông tin người dùng hiện tại (dựa vào JWT token).

    Args:
        current_user: Người dùng hiện tại (từ JWT token).

    Returns:
        schemas.User: Thông tin người dùng.
    """
    return current_user


@app.post("/devices/token")
async def update_device_token(
    token: str = Form(...),
    device_type: str = Form("android"),
    current_user: models.User = Depends(auth.get_current_user),
    db: Session = Depends(get_db)
):
    """
    Cập nhật FCM token cho thiết bị của người dùng.

    Android App gọi API này để gửi FCM Token lên Server.
    Nếu token đã tồn tại, cập nhật userid. Nếu chưa, tạo mới.

    Args:
        token: FCM token từ Firebase.
        device_type: Loại thiết bị (mặc định "android").
        current_user: Người dùng hiện tại.
        db: Database session.

    Returns:
        dict: Thông báo cập nhật thành công.
    """
    db_device = db.query(models.UserDevice).filter(models.UserDevice.device_token == token).first()
    if not db_device:
        db_device = models.UserDevice(device_token=token, device_type=device_type, userid=current_user.id)
        db.add(db_device)
    else:
        db_device.userid = current_user.id

    db.commit()
    return {"message": "Device token updated successfully"}


# ══════════════════════════════════════════════════════════════
# ADDRESS ENDPOINTS - Địa chỉ giao hàng
# ══════════════════════════════════════════════════════════════


@app.post("/addresses/", response_model=schemas.Address)
def create_address(address: schemas.AddressCreate, db: Session = Depends(get_db)):
    """
    Tạo địa chỉ giao hàng mới.

    Args:
        address: Thông tin địa chỉ (detail, phone, userid).
        db: Database session.

    Returns:
        schemas.Address: Địa chỉ đã tạo.
    """
    db_address = models.Address(detail=address.detail, phone=address.phone, userid=address.userid)
    db.add(db_address)
    db.commit()
    db.refresh(db_address)
    return db_address


@app.get("/users/{user_id}/addresses/", response_model=List[schemas.Address])
def read_user_addresses(user_id: int, db: Session = Depends(get_db)):
    """
    Lấy danh sách địa chỉ của người dùng.

    Args:
        user_id: ID người dùng.
        db: Database session.

    Returns:
        List[schemas.Address]: Danh sách địa chỉ.
    """
    return db.query(models.Address).filter(models.Address.userid == user_id).all()


@app.put("/addresses/{address_id}/", response_model=schemas.Address)
def update_address(address_id: int, address: schemas.AddressUpdate, db: Session = Depends(get_db)):
    """
    Cập nhật địa chỉ giao hàng.

    Chỉ cập nhật các trường không null.

    Args:
        address_id: ID địa chỉ cần cập nhật.
        address: Thông tin mới (detail, phone - có thể null).
        db: Database session.

    Returns:
        schemas.Address: Địa chỉ đã cập nhật.

    Raises:
        HTTPException: 404 nếu không tìm thấy địa chỉ.
    """
    db_address = db.query(models.Address).filter(models.Address.id == address_id).first()
    if not db_address:
        raise HTTPException(status_code=404, detail="Address not found")

    if address.detail is not None:
        db_address.detail = address.detail
    if address.phone is not None:
        db_address.phone = address.phone

    db.commit()
    db.refresh(db_address)
    return db_address


# ══════════════════════════════════════════════════════════════
# PROMOTIONS ENDPOINTS - Mã giảm giá
# ══════════════════════════════════════════════════════════════


@app.get("/promotions/", response_model=List[schemas.Promotion])
def read_promotions(active_only: bool = True, db: Session = Depends(get_db)):
    """
    Lấy danh sách mã giảm giá.

    Args:
        active_only: True (mặc định) chỉ lấy mã đang hoạt động.
        db: Database session.

    Returns:
        List[schemas.Promotion]: Danh sách mã giảm giá.
    """
    query = db.query(models.Promotion)
    if active_only:
        query = query.filter(models.Promotion.status.ilike("active"))
    return query.all()


# ══════════════════════════════════════════════════════════════
# RESTAURANT ENDPOINTS - Nhà hàng
# ══════════════════════════════════════════════════════════════


@app.get("/restaurants/", response_model=List[schemas.Restaurant])
def read_restaurants(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    """
    Lấy danh sách tất cả nhà hàng.

    Args:
        skip: Số bản ghi bỏ qua (phân trang).
        limit: Số bản ghi tối đa (mặc định 100).
        db: Database session.

    Returns:
        List[schemas.Restaurant]: Danh sách nhà hàng.
    """
    restaurants = db.query(models.Restaurant).offset(skip).limit(limit).all()
    return restaurants


@app.get("/restaurants/search/", response_model=List[schemas.Restaurant])
def search_restaurants_by_name(
    name: str,
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """
    Tìm kiếm nhà hàng theo tên (không phân biệt hoa thường).

    Args:
        name: Từ khóa tìm kiếm.
        skip: Số bản ghi bỏ qua.
        limit: Số bản ghi tối đa.
        db: Database session.

    Returns:
        List[schemas.Restaurant]: Danh sách nhà hàng khớp với từ khóa.
    """
    restaurants = db.query(models.Restaurant).filter(
        models.Restaurant.name.ilike(f"%{name}%")
    ).offset(skip).limit(limit).all()
    return restaurants


@app.get("/restaurants/{restaurant_id}/", response_model=schemas.Restaurant)
def read_restaurant(restaurant_id: int, db: Session = Depends(get_db)):
    """
    Lấy chi tiết nhà hàng theo ID.

    Args:
        restaurant_id: ID nhà hàng.
        db: Database session.

    Returns:
        schemas.Restaurant: Thông tin nhà hàng.

    Raises:
        HTTPException: 404 nếu không tìm thấy nhà hàng.
    """
    restaurant = db.query(models.Restaurant).filter(models.Restaurant.id == restaurant_id).first()
    if not restaurant:
        raise HTTPException(status_code=404, detail="Restaurant not found")
    return restaurant


# ══════════════════════════════════════════════════════════════
# MENU ITEM ENDPOINTS - Món ăn
# ══════════════════════════════════════════════════════════════


def build_menuitem_with_reviews(item: models.MenuItem, db: Session) -> dict:
    """
    Helper function: Xây dựng MenuItem kèm reviews và avg_rating.

    Lấy tất cả đánh giá cho món ăn, tính điểm trung bình,
    và trả về dict đầy đủ thông tin.

    Args:
        item: MenuItem model object.
        db: Database session.

    Returns:
        dict: MenuItem với reviews, avg_rating, restaurant_name.
    """
    restaurant = db.query(models.Restaurant).filter(models.Restaurant.id == item.restaurantid).first()
    restaurant_name = restaurant.name if restaurant else ""

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
    """
    Lấy danh sách tất cả món ăn từ tất cả nhà hàng (kèm reviews).

    Args:
        skip: Số bản ghi bỏ qua.
        limit: Số bản ghi tối đa.
        db: Database session.

    Returns:
        List[schemas.MenuItemWithReviews]: Danh sách món ăn với reviews.
    """
    menu_items = db.query(models.MenuItem).offset(skip).limit(limit).all()

    result = []
    for item in menu_items:
        item_dict = build_menuitem_with_reviews(item, db)
        result.append(item_dict)
    return result


@app.get("/menu-items/search/", response_model=List[schemas.MenuItemWithReviews])
def search_menu_items_by_name(
    name: str,
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """
    Tìm kiếm món ăn theo tên (không phân biệt hoa thường).

    Args:
        name: Từ khóa tìm kiếm.
        skip: Số bản ghi bỏ qua.
        limit: Số bản ghi tối đa.
        db: Database session.

    Returns:
        List[schemas.MenuItemWithReviews]: Danh sách món ăn khớp.
    """
    menu_items = db.query(models.MenuItem).filter(
        models.MenuItem.name.ilike(f"%{name}%")
    ).offset(skip).limit(limit).all()

    result = []
    for item in menu_items:
        item_dict = build_menuitem_with_reviews(item, db)
        result.append(item_dict)
    return result


@app.get("/menu-items/category/search/", response_model=List[schemas.MenuItemWithReviews])
def search_menu_items_by_category_name(
    category_name: str,
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """
    Tìm kiếm món ăn theo tên danh mục (không phân biệt hoa thường).

    Args:
        category_name: Tên danh mục cần tìm.
        skip: Số bản ghi bỏ qua.
        limit: Số bản ghi tối đa.
        db: Database session.

    Returns:
        List[schemas.MenuItemWithReviews]: Danh sách món ăn thuộc danh mục.
    """
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
    """
    Lấy chi tiết một món ăn (kèm reviews).

    Args:
        menu_item_id: ID món ăn.
        db: Database session.

    Returns:
        schemas.MenuItemWithReviews: Chi tiết món ăn với reviews.

    Raises:
        HTTPException: 404 nếu không tìm thấy món ăn.
    """
    item = db.query(models.MenuItem).filter(models.MenuItem.id == menu_item_id).first()
    if not item:
        raise HTTPException(status_code=404, detail="Menu item not found")

    return build_menuitem_with_reviews(item, db)


# ══════════════════════════════════════════════════════════════
# VNPAY RETURN - Callback từ VNPay
# ══════════════════════════════════════════════════════════════


@app.get("/vnpay_return")
async def vnpay_return(request: Request, db: Session = Depends(get_db)):
    """
    Callback từ VNPay sau khi thanh toán.

    Xử lý kết quả thanh toán từ VNPay:
    - vnp_ResponseCode="00": Thành công → cập nhật trạng thái "paid"
    - Khác: Thanh toán thất bại

    Args:
        request: HTTP request chứa query params từ VNPay.
        db: Database session.

    Returns:
        dict: Trạng thái thanh toán (success/failed).
    """
    params = dict(request.query_params)

    response_code = params.get("vnp_ResponseCode")
    order_ref = params.get("vnp_TxnRef")  # Format: DH{order_id}

    # Trích xuất order_id từ reference (VD: "DH123" -> 123)
    try:
        if order_ref and order_ref.startswith("DH"):
            order_id = int(order_ref[2:])
        else:
            order_id = int(order_ref) if order_ref else 0
    except (ValueError, AttributeError):
        order_id = 0

    if response_code == "00":
        # Thanh toán thành công
        if order_id > 0:
            order = db.query(models.Orders).filter(models.Orders.id == order_id).first()
            if order:
                order.status = "paid"

                payment_record = models.Payment(
                    status="success",
                    method="vnpay",
                    orderid=order.id
                )
                db.add(payment_record)
                db.commit()
                db.refresh(order)

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
        # Thanh toán thất bại
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


# ══════════════════════════════════════════════════════════════
# PAYMENT ENDPOINTS - Thanh toán
# ══════════════════════════════════════════════════════════════


@app.get("/create-payment")
async def create_payment(
    order_id: str,
    amount: int,
    request: Request
):
    """
    Tạo URL thanh toán VNPay.

    Args:
        order_id: ID đơn hàng.
        amount: Số tiền (VNĐ).
        request: HTTP request để lấy IP address.

    Returns:
        dict: Chứa payment_url để mở trong WebView.
    """
    ip_address = request.client.host

    payment_url = generate_vnpay_url(
        order_id=str(order_id),
        amount=amount,
        ip_address=ip_address
    )

    return {
        "payment_url": payment_url
    }


# ══════════════════════════════════════════════════════════════
# ORDER ENDPOINTS - Đơn hàng
# ══════════════════════════════════════════════════════════════


@app.post("/orders/", response_model=schemas.Order)
def create_order(order: schemas.OrderCreate, db: Session = Depends(get_db)):
    """
    Tạo đơn hàng mới.

    Tạo đơn hàng và danh sách OrderItem.
    Nếu là COD (status="confirmed"), gửi thông báo ngay.

    Args:
        order: Thông tin đơn hàng (status, totalprice, restaurantid, addressid, userid, order_items).
        db: Database session.

    Returns:
        schemas.Order: Đơn hàng đã tạo.
    """
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

    # Nếu là COD, gửi thông báo ngay
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
    """
    Lấy danh sách đơn hàng của người dùng.

    Args:
        user_id: ID người dùng.
        db: Database session.

    Returns:
        List[schemas.Order]: Danh sách đơn hàng.
    """
    return db.query(models.Orders).filter(models.Orders.userid == user_id).all()


@app.get("/orders/{order_id}/detail", response_model=schemas.OrderDetail)
def read_order_detail(order_id: int, db: Session = Depends(get_db)):
    """
    Lấy chi tiết đơn hàng (kèm nhà hàng, địa chỉ, danh sách món).

    Args:
        order_id: ID đơn hàng.
        db: Database session.

    Returns:
        schemas.OrderDetail: Chi tiết đơn hàng đầy đủ.

    Raises:
        HTTPException: 404 nếu không tìm thấy đơn hàng.
    """
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
def update_order_status(
    order_id: int,
    payload: schemas.OrderStatusUpdate,
    db: Session = Depends(get_db)
):
    """
    Cập nhật trạng thái đơn hàng.

    Tự động gửi push notification khi trạng thái thay đổi:
    - paid/confirmed: "Đơn hàng được đặt thành công!"
    - delivering: "Đơn hàng đang giao"
    - completed: "Đơn hàng đã giao"
    - cancelled: "Đơn hàng bị hủy"

    Args:
        order_id: ID đơn hàng.
        payload: Trạng thái mới.
        db: Database session.

    Returns:
        schemas.Order: Đơn hàng đã cập nhật.

    Raises:
        HTTPException: 404 nếu không tìm thấy đơn hàng.
    """
    order = db.query(models.Orders).filter(models.Orders.id == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")

    old_status = order.status
    order.status = payload.status
    db.commit()
    db.refresh(order)

    # Tạo thông báo khi trạng thái thay đổi
    try:
        notification_title = ""
        notification_body = ""

        if payload.status in ["paid", "confirmed"] and old_status not in ["paid", "confirmed"]:
            notification_title = "Đơn hàng được đặt thành công! ✅"
            total_formatted = f"{order.totalprice:,.0f}".replace(",", ".")
            notification_body = f"Đơn hàng #{order.id} đã được đặt thành công. Tổng tiền: {total_formatted}đ. Đang chờ quán xác nhận."
        elif payload.status == "delivering":
            notification_title = "Đơn hàng đang giao 🚚"
            notification_body = f"Đơn hàng #{order.id} đang trên đường giao đến bạn"
        elif payload.status == "completed":
            notification_title = "Đơn hàng đã giao 🎉"
            notification_body = f"Đơn hàng #{order.id} đã giao thành công. Cảm ơn đã đặt hàng!"
        elif payload.status == "cancelled":
            notification_title = "Đơn hàng bị hủy ❌"
            notification_body = f"Đơn hàng #{order.id} đã bị hủy"

        if notification_title:
            notify_user(
                db=db,
                user_id=order.userid,
                title=notification_title,
                body=notification_body,
                order_id=order.id
            )
    except Exception as e:
        print(f"Lỗi khi tạo thông báo trạng thái: {str(e)}")

    return order


# ══════════════════════════════════════════════════════════════
# REVIEW ENDPOINTS - Đánh giá
# ══════════════════════════════════════════════════════════════


@app.post("/reviews/", response_model=schemas.Review)
def create_review(review: schemas.ReviewCreate, db: Session = Depends(get_db)):
    """
    Tạo đánh giá cho đơn hàng已完成.

    Validation:
    - Đơn hàng phải tồn tại
    - Đơn hàng phải thuộc về người đánh giá
    - Đơn hàng phải có ít nhất 1 món

    Sau khi tạo, tính lại avg_rating cho nhà hàng.

    Args:
        review: Thông tin đánh giá (rating, comment, orderid, userid).
        db: Database session.

    Returns:
        schemas.Review: Đánh giá đã tạo.

    Raises:
        HTTPException: 404 nếu đơn hàng không tồn tại.
        HTTPException: 403 nếu đơn hàng không thuộc về user.
        HTTPException: 400 nếu đơn hàng không có món.
    """
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


# ══════════════════════════════════════════════════════════════
# PROFILE ENDPOINTS - Hồ sơ & Tích điểm
# ══════════════════════════════════════════════════════════════


@app.get("/users/{user_id}/profile-summary", response_model=schemas.UserProfileSummary)
def read_user_profile_summary(user_id: int, db: Session = Depends(get_db)):
    """
    Lấy tổng quan hồ sơ người dùng (điểm tích lũy).

    Cách tính điểm:
    - points = total_spent // 10000
    - delivered_orders = số đơn có status "delivered", "completed", "done"
    - total_spent = tổng tất cả orders.totalprice

    Args:
        user_id: ID người dùng.
        db: Database session.

    Returns:
        schemas.UserProfileSummary: user_id, user_name, points, delivered_orders, total_spent.

    Raises:
        HTTPException: 404 nếu không tìm thấy người dùng.
    """
    user = db.query(models.User).filter(models.User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    orders = db.query(models.Orders).filter(models.Orders.userid == user_id).all()
    delivered_orders = [o for o in orders if (o.status or "").lower() in {"delivered", "completed", "done"}]
    total_spent = sum(o.totalprice or 0 for o in orders)

    points = total_spent // 10000

    return {
        "user_id": user.id,
        "user_name": user.name,
        "points": points,
        "delivered_orders": len(delivered_orders),
        "total_spent": total_spent
    }


# ══════════════════════════════════════════════════════════════
# NOTIFICATION ENDPOINTS - Thông báo
# ══════════════════════════════════════════════════════════════


@app.get("/users/{user_id}/notifications/", response_model=List[schemas.Notification])
def read_user_notifications(user_id: int, db: Session = Depends(get_db)):
    """
    Lấy danh sách thông báo của người dùng (sắp xếp mới nhất trước).

    Args:
        user_id: ID người dùng.
        db: Database session.

    Returns:
        List[schemas.Notification]: Danh sách thông báo.
    """
    return db.query(models.Notification).filter(
        models.Notification.userid == user_id
    ).order_by(models.Notification.createdat.desc()).all()


@app.post("/notifications/", response_model=schemas.Notification)
def create_notification(notification: schemas.NotificationCreate, db: Session = Depends(get_db)):
    """
    Tạo thông báo mới và gửi push notification.

    Sử dụng notify_user() để vừa lưu vào DB vừa gửi FCM push.

    Args:
        notification: Thông tin thông báo (title, type, content, userid, orderid).
        db: Database session.

    Returns:
        schemas.Notification: Thông báo đã tạo.
    """
    notify_user(
        db=db,
        user_id=notification.userid,
        title=notification.title,
        body=notification.content,
        order_id=notification.orderid
    )

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
    """
    Đánh dấu thông báo đã đọc.

    Args:
        notification_id: ID thông báo.
        db: Database session.

    Returns:
        dict: Thông báo đánh dấu thành công.

    Raises:
        HTTPException: 404 nếu không tìm thấy thông báo.
    """
    db_notification = db.query(models.Notification).filter(
        models.Notification.id == notification_id
    ).first()
    if not db_notification:
        raise HTTPException(status_code=404, detail="Notification not found")

    db_notification.isread = True
    db.commit()
    return {"message": "Notification marked as read"}


# ══════════════════════════════════════════════════════════════
# Khởi chạy server
# ══════════════════════════════════════════════════════════════

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)



