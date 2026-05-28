"""
Module: database.py

Cấu hình kết nối SQLAlchemy và session cho cơ sở dữ liệu PostgreSQL.
Cung cấp engine, SessionLocal và hàm get_db() dependency cho FastAPI.
"""

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
import os
from dotenv import load_dotenv

load_dotenv()

# URL kết nối database, ưu tiên biến môi trường, mặc định là PostgreSQL local
SQLALCHEMY_DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://postgres:password@localhost/food_delivery")

# SQLAlchemy engine quản lý kết nối pool tới database
engine = create_engine(SQLALCHEMY_DATABASE_URL)

# Session factory tạo các session kết nối database
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Base class để khai báo các ORM model kế thừa
Base = declarative_base()

def get_db():
    """FastAPI dependency: tạo session database cho mỗi request, tự động đóng sau khi hoàn tất."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
