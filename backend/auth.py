"""
Module xác thực JWT và quản lý mật khẩu.

Cung cấp các chức năng:
- Hash/verify mật khẩu bằng bcrypt
- Tạo và giải mã JWT access token
- Lấy người dùng hiện tại từ token

Sử dụng:
- python-jose cho JWT
- passlib cho bcrypt hashing
"""

import os
from datetime import datetime, timedelta
from typing import Optional
from jose import JWTError, jwt
from passlib.context import CryptContext
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session
from dotenv import load_dotenv

import models, database

load_dotenv()

# Cấu hình từ biến môi trường
SECRET_KEY = os.getenv("SECRET_KEY")
ALGORITHM = os.getenv("ALGORITHM", "HS256")
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", 30))

# Context hash mật khẩu
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# OAuth2 scheme cho FastAPI
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """
    Kiểm tra mật khẩu có khớp với hash không.

    Args:
        plain_password: Mật khẩu dạng plain text.
        hashed_password: Mật khẩu đã hash (bcrypt).

    Returns:
        True nếu mật khẩu khớp, False nếu không.
    """
    return pwd_context.verify(plain_password, hashed_password)


def get_password_hash(password: str) -> str:
    """
    Hash mật khẩu bằng bcrypt.

    Args:
        password: Mật khẩu dạng plain text.

    Returns:
        str: Mật khẩu đã hash.
    """
    return pwd_context.hash(password)


def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    """
    Tạo JWT access token.

    Args:
        data: Dữ liệu payload (thường là {"sub": email}).
        expires_delta: Thời gian hết hạn (mặc định 15 phút).

    Returns:
        str: JWT token string.
    """
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=15)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt


async def get_current_user(
    token: str = Depends(oauth2_scheme),
    db: Session = Depends(database.get_db)
) -> models.User:
    """
    Lấy người dùng hiện tại từ JWT token.

    Giải mã token, lấy email từ payload, tìm user trong database.
    Raise 401 nếu token không hợp lệ hoặc user không tồn tại.

    Args:
        token: JWT token từ Authorization header.
        db: Database session.

    Returns:
        models.User: Người dùng hiện tại.

    Raises:
        HTTPException: 401 nếu token không hợp lệ.
    """
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        email: str = payload.get("sub")
        if email is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    user = db.query(models.User).filter(models.User.email == email).first()
    if user is None:
        raise credentials_exception
    return user
