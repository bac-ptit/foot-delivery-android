"""
Module: auth.py

Xác thực và phân quyền người dùng.
Cung cấp JWT token, mã hóa mật khẩu bcrypt, và dependency lấy user hiện tại.
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

# Khóa bí mật để ký JWT token, lấy từ biến môi trường
SECRET_KEY = os.getenv("SECRET_KEY")
# Thuật toán mã hóa JWT, mặc định HS256
ALGORITHM = os.getenv("ALGORITHM", "HS256")
# Thời gian hết hạn token (phút), mặc định 30 phút
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", 30))

# Context mã hóa mật khẩu bằng bcrypt
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
# Scheme trích xuất Bearer token từ header Authorization
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

def verify_password(plain_password, hashed_password):
    """Xác thực mật khẩu plaintext với mật khẩu đã hash bằng bcrypt."""
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password):
    """Hash mật khẩu plaintext bằng bcrypt."""
    return pwd_context.hash(password)

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None):
    """Tạo JWT access token với payload và thời gian hết hạn."""
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=15)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

async def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(database.get_db)):
    """Dependency: giải mã JWT token và trả về user hiện tại. Raise 401 nếu token không hợp lệ."""
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
