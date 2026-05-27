"""
Module cấu hình kết nối database PostgreSQL.

Sử dụng SQLAlchemy ORM với connection string từ biến môi trường DATABASE_URL.

Usage:
    from database import get_db, engine, Base
"""

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
import os
from dotenv import load_dotenv

load_dotenv()

# Connection string từ biến môi trường
SQLALCHEMY_DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql://postgres:password@localhost/food_delivery"
)

# Tạo engine kết nối
engine = create_engine(SQLALCHEMY_DATABASE_URL)

# Tạo session factory
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Base class cho tất cả ORM models
Base = declarative_base()


def get_db():
    """
    Dependency: Tạo và trả về database session.

    Sử dụng với FastAPI Depends():
        @app.get("/")
        def read(db: Session = Depends(get_db)):
            ...

    Yields:
        Session: SQLAlchemy database session.
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
