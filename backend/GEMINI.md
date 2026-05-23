# GEMINI Rules — Python Web Development

You are an expert Python web developer. Follow these rules strictly for all code generation, review, and suggestions.

---

## 1. Stack & Framework

- **Backend**: FastAPI (preferred) or Flask
- **Templating**: Jinja2 for server-rendered HTML
- **ORM**: SQLAlchemy (async with FastAPI, sync with Flask)
- **Database migrations**: Alembic
- **Validation**: Pydantic v2 (FastAPI) or WTForms (Flask)
- **Auth**: JWT via `python-jose` + Google OAuth2 via `authlib` (primary login method)
- **Testing**: `pytest` + `httpx` (async) or `Flask test client`
- **Package manager**: `uv` (preferred) or `pip` with `requirements.txt`

---

## 2. Layer Architecture

The project follows a **4-layer architecture**. Each layer has a single responsibility and may only depend on the layer directly below it.

```
┌─────────────────────────────────────────┐
│           Presentation Layer            │  routers/, templates/, schemas/
│   (HTTP in, HTTP out — no logic here)   │
├─────────────────────────────────────────┤
│           Application Layer             │  services/
│  (Use-case orchestration, no DB calls)  │
├─────────────────────────────────────────┤
│             Domain Layer                │  domain/
│  (Core business rules, pure Python)     │
├─────────────────────────────────────────┤
│         Infrastructure Layer            │  models/, repositories/, db/
│  (DB, ORM, external APIs, I/O)          │
└─────────────────────────────────────────┘
```

### Dependency Rule
> **Upper layers depend on lower layers. Lower layers NEVER import from upper layers.**

| Layer | Can import from | Cannot import from |
|---|---|---|
| Presentation | Application, Domain | Infrastructure directly |
| Application | Domain, Infrastructure (via interfaces) | Presentation |
| Domain | Nothing (pure Python only) | Any other layer |
| Infrastructure | Domain | Application, Presentation |

---

## 3. Project Structure

```
project/
├── app/
│   ├── __init__.py
│   ├── main.py                  # App entry point & middleware setup
│   ├── config.py                # Settings via pydantic-settings
│   ├── dependencies.py          # Shared FastAPI Depends()
│   ├── exceptions.py            # Custom exception classes
│   │
│   ├── domain/                  # ★ Domain Layer (pure Python, no frameworks)
│   │   ├── __init__.py
│   │   ├── user/
│   │   │   ├── __init__.py
│   │   │   ├── entity.py        # User dataclass/entity
│   │   │   ├── value_objects.py # Email, Password, Role (immutable)
│   │   │   ├── repository.py    # IUserRepository (abstract interface)
│   │   │   └── exceptions.py    # UserNotFoundError, DuplicateEmailError
│   │   └── order/
│   │       ├── entity.py
│   │       ├── value_objects.py
│   │       └── repository.py
│   │
│   ├── services/                # Application Layer (use-case orchestration)
│   │   ├── __init__.py
│   │   ├── user_service.py      # create_user(), get_user(), delete_user()
│   │   └── order_service.py
│   │
│   ├── repositories/            # Infrastructure Layer (SQLAlchemy implementations)
│   │   ├── __init__.py
│   │   ├── user_repository.py   # Implements IUserRepository
│   │   └── order_repository.py
│   │
│   ├── models/                  # Infrastructure Layer (SQLAlchemy ORM models)
│   │   ├── __init__.py
│   │   ├── base.py
│   │   ├── user.py
│   │   └── order.py
│   │
│   ├── schemas/                 # Presentation Layer (Pydantic request/response)
│   │   ├── __init__.py
│   │   ├── user.py
│   │   └── order.py
│   │
│   ├── routers/                 # Presentation Layer (FastAPI route handlers)
│   │   ├── __init__.py
│   │   ├── users.py
│   │   └── orders.py
│   │
│   ├── templates/               # Jinja2 HTML templates
│   │   ├── base.html
│   │   └── components/
│   └── static/
│       ├── css/
│       └── js/
│
├── tests/
│   ├── conftest.py
│   ├── unit/                    # Test domain & services in isolation
│   │   ├── test_user_entity.py
│   │   └── test_user_service.py
│   └── integration/             # Test routers + real DB
│       └── test_user_router.py
├── alembic/
├── .env
├── .env.example
├── pyproject.toml
└── README.md
```

---

## 4. Domain Layer

The domain layer contains **pure business logic only** — no FastAPI, no SQLAlchemy, no I/O of any kind.

### Entities
An entity has an identity (`id`) and encapsulates business rules as methods.

```python
# app/domain/user/entity.py
from dataclasses import dataclass, field
from app.domain.user.value_objects import Email, HashedPassword, UserRole
from app.domain.user.exceptions import InvalidRoleError

@dataclass
class UserEntity:
    id: int | None
    name: str
    email: Email
    password: HashedPassword
    role: UserRole = UserRole.CUSTOMER

    def is_admin(self) -> bool:
        return self.role == UserRole.ADMIN

    def promote_to_admin(self) -> None:
        """Promote this user to admin role."""
        self.role = UserRole.ADMIN

    def change_name(self, new_name: str) -> None:
        if not new_name.strip():
            raise ValueError("Name cannot be empty.")
        self.name = new_name.strip()
```

### Value Objects
Value objects are **immutable** and have no identity — equality is based on value, not ID.

```python
# app/domain/user/value_objects.py
from dataclasses import dataclass
from enum import StrEnum
import re

@dataclass(frozen=True)
class Email:
    value: str

    def __post_init__(self) -> None:
        if not re.match(r"^[\w.+-]+@[\w-]+\.[a-z]{2,}$", self.value):
            raise ValueError(f"Invalid email address: {self.value}")

    def __str__(self) -> str:
        return self.value


@dataclass(frozen=True)
class HashedPassword:
    value: str  # already bcrypt-hashed — never store plaintext

    def __str__(self) -> str:
        return self.value


class UserRole(StrEnum):
    CUSTOMER = "customer"
    ADMIN    = "admin"
    SHIPPER  = "shipper"
```

### Repository Interfaces
Define **abstract interfaces** in the domain layer so the application layer can depend on them without touching SQLAlchemy.

```python
# app/domain/user/repository.py
from abc import ABC, abstractmethod
from app.domain.user.entity import UserEntity

class IUserRepository(ABC):

    @abstractmethod
    async def get_by_id(self, user_id: int) -> UserEntity | None: ...

    @abstractmethod
    async def get_by_email(self, email: str) -> UserEntity | None: ...

    @abstractmethod
    async def save(self, user: UserEntity) -> UserEntity: ...

    @abstractmethod
    async def delete(self, user_id: int) -> None: ...
```

### Domain Exceptions
Raise domain-specific exceptions — never `HTTPException` inside the domain layer.

```python
# app/domain/user/exceptions.py
class UserDomainError(Exception):
    """Base class for all user domain errors."""

class UserNotFoundError(UserDomainError):
    def __init__(self, user_id: int):
        super().__init__(f"User with id={user_id} does not exist.")

class DuplicateEmailError(UserDomainError):
    def __init__(self, email: str):
        super().__init__(f"Email '{email}' is already registered.")

class InvalidRoleError(UserDomainError):
    pass
```

### Rules for the Domain Layer
- ✅ Plain Python `dataclass` or `class` only
- ✅ All business rules and invariants live here as methods
- ✅ Only raises domain exceptions (never `HTTPException`)
- ❌ No imports from `fastapi`, `sqlalchemy`, `pydantic`, or any infrastructure
- ❌ No `async` database calls
- ❌ No I/O of any kind (no file reads, no HTTP calls)

---

## 5. Application Layer (Services)

Services orchestrate use cases. They call the domain entity's methods and persist changes via the repository interface.

```python
# app/services/user_service.py
from app.domain.user.entity import UserEntity
from app.domain.user.value_objects import Email, HashedPassword, UserRole
from app.domain.user.repository import IUserRepository
from app.domain.user.exceptions import DuplicateEmailError, UserNotFoundError
from app.schemas.user import UserCreateSchema
from passlib.context import CryptContext

pwd_ctx = CryptContext(schemes=["bcrypt"])

class UserService:
    def __init__(self, repo: IUserRepository) -> None:
        self._repo = repo

    async def create(self, data: UserCreateSchema) -> UserEntity:
        """Register a new user. Raises DuplicateEmailError if email taken."""
        existing = await self._repo.get_by_email(data.email)
        if existing:
            raise DuplicateEmailError(data.email)

        user = UserEntity(
            id=None,
            name=data.name,
            email=Email(data.email),
            password=HashedPassword(pwd_ctx.hash(data.password)),
            role=UserRole.CUSTOMER,
        )
        return await self._repo.save(user)

    async def get_by_id(self, user_id: int) -> UserEntity:
        user = await self._repo.get_by_id(user_id)
        if not user:
            raise UserNotFoundError(user_id)
        return user

    async def promote_to_admin(self, user_id: int) -> UserEntity:
        user = await self.get_by_id(user_id)
        user.promote_to_admin()          # ← business rule lives on entity
        return await self._repo.save(user)
```

### Rules for the Application Layer
- ✅ One service class per feature/aggregate
- ✅ Receives and returns **domain entities or Pydantic schemas** — never ORM models
- ✅ Calls domain entity methods for business logic — never duplicates rules here
- ✅ Depends on `IRepository` interfaces (not concrete SQLAlchemy classes)
- ❌ No `HTTPException` — only domain exceptions (translated in the router)
- ❌ No direct SQL or ORM queries

---

## 6. Infrastructure Layer (Repositories)

Concrete implementations of domain repository interfaces using SQLAlchemy. Maps between ORM models and domain entities.

```python
# app/repositories/user_repository.py
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.domain.user.entity import UserEntity
from app.domain.user.repository import IUserRepository
from app.domain.user.value_objects import Email, HashedPassword, UserRole
from app.models.user import UserORM  # SQLAlchemy model

class SQLUserRepository(IUserRepository):
    def __init__(self, db: AsyncSession) -> None:
        self._db = db

    async def get_by_id(self, user_id: int) -> UserEntity | None:
        row = await self._db.get(UserORM, user_id)
        return self._to_entity(row) if row else None

    async def get_by_email(self, email: str) -> UserEntity | None:
        result = await self._db.execute(
            select(UserORM).where(UserORM.email == email)
        )
        row = result.scalar_one_or_none()
        return self._to_entity(row) if row else None

    async def save(self, user: UserEntity) -> UserEntity:
        if user.id is None:
            row = self._to_orm(user)
            self._db.add(row)
        else:
            row = await self._db.get(UserORM, user.id)
            self._apply_entity(row, user)
        await self._db.commit()
        await self._db.refresh(row)
        return self._to_entity(row)

    async def delete(self, user_id: int) -> None:
        row = await self._db.get(UserORM, user_id)
        if row:
            await self._db.delete(row)
            await self._db.commit()

    # ── Mapping helpers ──────────────────────────────────────────────────

    @staticmethod
    def _to_entity(row: UserORM) -> UserEntity:
        return UserEntity(
            id=row.id,
            name=row.name,
            email=Email(row.email),
            password=HashedPassword(row.password),
            role=UserRole(row.role),
        )

    @staticmethod
    def _to_orm(entity: UserEntity) -> UserORM:
        return UserORM(
            name=entity.name,
            email=str(entity.email),
            password=str(entity.password),
            role=entity.role.value,
        )

    @staticmethod
    def _apply_entity(row: UserORM, entity: UserEntity) -> None:
        row.name     = entity.name
        row.email    = str(entity.email)
        row.password = str(entity.password)
        row.role     = entity.role.value
```

### Rules for the Infrastructure Layer
- ✅ Every repository implements a domain interface (`IUserRepository`)
- ✅ Always map ORM rows → domain entities before returning
- ✅ Always map domain entities → ORM rows before persisting
- ❌ Never return raw SQLAlchemy `Row` or ORM objects to upper layers
- ❌ Never contain business logic — only queries and mapping

---

### General
- Follow **PEP 8** strictly
- Max line length: **88 characters** (Black formatter default)
- Use **Black** for formatting, **Ruff** for linting
- Use **type hints** on all functions and class attributes — no exceptions
- Prefer `pathlib.Path` over `os.path`
- Prefer f-strings over `.format()` or `%`

### Naming Conventions
| Kind | Convention | Example |
|---|---|---|
| Variables & functions | `snake_case` | `get_user_by_id` |
| Classes | `PascalCase` | `UserRepository` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_RETRY_COUNT` |
| Private attributes | `_leading_underscore` | `_cache` |
| Files/modules | `snake_case` | `user_service.py` |

### Functions & Classes
```python
# Good
async def get_user(user_id: int, db: AsyncSession) -> UserSchema:
    """Fetch a single user by ID. Raises 404 if not found."""
    user = await db.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return UserSchema.model_validate(user)

# Bad — no type hints, no docstring
async def get_user(user_id, db):
    user = await db.get(User, user_id)
    return user
```

### Imports
- Group imports: stdlib → third-party → local, separated by blank lines
- Use absolute imports within the project
- Never use wildcard imports (`from module import *`)

```python
# Good
import os
from pathlib import Path

from fastapi import Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.user import User
from app.schemas.user import UserSchema
```

---

## 7. Presentation Layer (Routers + Schemas)

### Routers
- One router per feature module
- Keep route handlers thin — only HTTP plumbing + exception translation
- **Translate domain exceptions → HTTPException here**, not in services

```python
# routers/users.py
from fastapi import APIRouter, Depends, HTTPException
from app.schemas.user import UserSchema, UserCreateSchema
from app.services.user_service import UserService
from app.domain.user.exceptions import UserNotFoundError, DuplicateEmailError
from app.dependencies import get_user_service

router = APIRouter(prefix="/users", tags=["users"])

@router.get("/{user_id}", response_model=UserSchema)
async def get_user(
    user_id: int,
    service: UserService = Depends(get_user_service),
) -> UserSchema:
    try:
        user = await service.get_by_id(user_id)
        return UserSchema.model_validate(user.__dict__)
    except UserNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))

@router.post("/", response_model=UserSchema, status_code=201)
async def create_user(
    data: UserCreateSchema,
    service: UserService = Depends(get_user_service),
) -> UserSchema:
    try:
        user = await service.create(data)
        return UserSchema.model_validate(user.__dict__)
    except DuplicateEmailError as e:
        raise HTTPException(status_code=409, detail=str(e))
```

### Schemas (Pydantic v2)
- Separate schemas for Create / Update / Response — never reuse the same class
- Schemas belong to the **Presentation layer** — they are HTTP contracts, not domain objects

```python
# schemas/user.py
from pydantic import BaseModel, EmailStr

class UserCreateSchema(BaseModel):
    name: str
    email: EmailStr
    password: str

class UserUpdateSchema(BaseModel):
    name: str | None = None
    email: EmailStr | None = None

class UserSchema(BaseModel):
    id: int
    name: str
    email: str
    role: str
```

### Dependency Injection
- Wire services and repositories together in `dependencies.py`
- Never instantiate anything directly inside route handlers

```python
# dependencies.py
from sqlalchemy.ext.asyncio import AsyncSession
from app.db import AsyncSessionLocal
from app.repositories.user_repository import SQLUserRepository
from app.services.user_service import UserService

async def get_db() -> AsyncGenerator[AsyncSession, None]:
    async with AsyncSessionLocal() as session:
        yield session

async def get_user_service(db: AsyncSession = Depends(get_db)) -> UserService:
    return UserService(repo=SQLUserRepository(db))
```

---

## 8. SQLAlchemy ORM Models (Infrastructure)

- Named `*ORM` suffix to distinguish from domain entities (e.g. `UserORM` vs `UserEntity`)
- Use SQLAlchemy 2.x style: `Mapped[]` + `mapped_column()`
- Never add business logic here — ORM models are pure data containers

```python
# models/user.py
from sqlalchemy import String
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, relationship

class Base(DeclarativeBase):
    pass

class UserORM(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    email: Mapped[str] = mapped_column(String(255), unique=True, nullable=False)
    password: Mapped[str] = mapped_column(String(255), nullable=False)
    role: Mapped[str] = mapped_column(String(50), nullable=False, default="customer")

    orders: Mapped[list["OrderORM"]] = relationship(back_populates="user")
```

---

## 9. Jinja2 Templates

### Structure
- Always extend a `base.html`
- Use `{% block %}` for content areas: `head`, `content`, `scripts`
- Store reusable UI pieces in `templates/components/`

```html
{# base.html #}
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>{% block title %}App{% endblock %}</title>
    {% block head %}{% endblock %}
</head>
<body>
    {% include "components/navbar.html" %}
    <main>{% block content %}{% endblock %}</main>
    {% block scripts %}{% endblock %}
</body>
</html>
```

```html
{# pages/users.html #}
{% extends "base.html" %}

{% block title %}Users{% endblock %}

{% block content %}
<h1>Users</h1>
{% for user in users %}
    {% include "components/user_card.html" %}
{% endfor %}
{% endblock %}
```

### Rules
- Always escape user content — never use `| safe` on user-supplied data
- Use `url_for()` for all static file and route references
- Pass only what the template needs — no raw ORM objects if Pydantic schemas exist

---

## 10. Error Handling

- Use custom exception classes inheriting from a base `AppException`
- Register global exception handlers in `main.py`
- Never expose raw stack traces to the client

```python
# exceptions.py
class AppException(Exception):
    def __init__(self, message: str, status_code: int = 400):
        self.message = message
        self.status_code = status_code

class NotFoundException(AppException):
    def __init__(self, resource: str):
        super().__init__(f"{resource} not found", status_code=404)

# main.py
@app.exception_handler(AppException)
async def app_exception_handler(request: Request, exc: AppException):
    return JSONResponse(
        status_code=exc.status_code,
        content={"detail": exc.message},
    )
```

---

## 11. Environment & Config

- Use `pydantic-settings` for all configuration — never hardcode secrets
- Load from `.env` file in development
- Provide `.env.example` with all keys (no values)

```python
# config.py
from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    database_url: str
    secret_key: str
    debug: bool = False
    allowed_origins: list[str] = ["http://localhost:3000"]

    model_config = SettingsConfigDict(env_file=".env")

settings = Settings()
```

---

## 12. Google OAuth2 Authentication

Use `authlib` for the OAuth2 flow and `python-jose` for issuing internal JWTs after login.

### Flow Overview

```
User clicks "Login with Google"
        │
        ▼
GET /auth/google/login
  → redirect to Google consent screen
        │
        ▼
Google redirects to GET /auth/google/callback?code=...
  → exchange code for Google token
  → fetch user info from Google
  → upsert user in DB (create if new, fetch if existing)
  → issue internal JWT
  → redirect to frontend with JWT
```

### Install Dependencies

```
uv add authlib httpx python-jose[cryptography] passlib[bcrypt]
```

### Config

```python
# config.py
class Settings(BaseSettings):
    google_client_id: str
    google_client_secret: str
    google_redirect_uri: str          # e.g. http://localhost:8000/auth/google/callback
    jwt_secret_key: str
    jwt_algorithm: str = "HS256"
    jwt_expire_minutes: int = 60 * 24 # 1 day
    ...
```

```ini
# .env.example
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
GOOGLE_REDIRECT_URI=http://localhost:8000/auth/google/callback
JWT_SECRET_KEY=
```

### Domain — Value Object & Entity update

```python
# domain/user/value_objects.py
from enum import StrEnum

class AuthProvider(StrEnum):
    LOCAL  = "local"
    GOOGLE = "google"
```

```python
# domain/user/entity.py
@dataclass
class UserEntity:
    id: int | None
    name: str
    email: Email
    password: HashedPassword | None   # None for OAuth-only accounts
    role: UserRole = UserRole.CUSTOMER
    provider: AuthProvider = AuthProvider.LOCAL
    provider_id: str | None = None    # Google's "sub" field

    def is_oauth_account(self) -> bool:
        return self.provider != AuthProvider.LOCAL
```

### Domain — Repository Interface update

```python
# domain/user/repository.py
class IUserRepository(ABC):
    ...
    @abstractmethod
    async def get_by_provider(
        self, provider: str, provider_id: str
    ) -> UserEntity | None: ...
```

### Infrastructure — ORM Model update

```python
# models/user.py
class UserORM(Base):
    __tablename__ = "users"

    id: Mapped[int]          = mapped_column(primary_key=True)
    name: Mapped[str]        = mapped_column(String(255), nullable=False)
    email: Mapped[str]       = mapped_column(String(255), unique=True, nullable=False)
    password: Mapped[str | None] = mapped_column(String(255), nullable=True)
    role: Mapped[str]        = mapped_column(String(50), default="customer")
    provider: Mapped[str]    = mapped_column(String(50), default="local")
    provider_id: Mapped[str | None] = mapped_column(String(255), nullable=True)
```

### Application Layer — OAuth Service

```python
# services/oauth_service.py
import httpx
from authlib.integrations.starlette_client import OAuth
from app.config import settings
from app.domain.user.entity import UserEntity
from app.domain.user.value_objects import Email, AuthProvider, UserRole
from app.domain.user.repository import IUserRepository

oauth = OAuth()
oauth.register(
    name="google",
    client_id=settings.google_client_id,
    client_secret=settings.google_client_secret,
    server_metadata_url="https://accounts.google.com/.well-known/openid-configuration",
    client_kwargs={"scope": "openid email profile"},
)

class OAuthService:
    def __init__(self, repo: IUserRepository) -> None:
        self._repo = repo

    async def get_or_create_google_user(self, google_user_info: dict) -> UserEntity:
        """Upsert a user from Google profile data."""
        provider_id: str = google_user_info["sub"]
        email: str       = google_user_info["email"]
        name: str        = google_user_info.get("name", email.split("@")[0])

        # 1. Try find by Google provider_id
        user = await self._repo.get_by_provider("google", provider_id)
        if user:
            return user

        # 2. Try find by email (user may have registered locally before)
        user = await self._repo.get_by_email(email)
        if user:
            # Link Google to existing account
            user.provider    = AuthProvider.GOOGLE
            user.provider_id = provider_id
            return await self._repo.save(user)

        # 3. Create brand new user
        new_user = UserEntity(
            id=None,
            name=name,
            email=Email(email),
            password=None,              # no password for OAuth accounts
            role=UserRole.CUSTOMER,
            provider=AuthProvider.GOOGLE,
            provider_id=provider_id,
        )
        return await self._repo.save(new_user)
```

### Infrastructure — JWT Token Utility

```python
# app/auth/jwt.py
from datetime import datetime, timedelta, timezone
from jose import JWTError, jwt
from app.config import settings

def create_access_token(user_id: int, role: str) -> str:
    expire = datetime.now(timezone.utc) + timedelta(
        minutes=settings.jwt_expire_minutes
    )
    payload = {"sub": str(user_id), "role": role, "exp": expire}
    return jwt.encode(payload, settings.jwt_secret_key, algorithm=settings.jwt_algorithm)

def decode_access_token(token: str) -> dict:
    """Returns payload dict. Raises JWTError if invalid or expired."""
    return jwt.decode(token, settings.jwt_secret_key, algorithms=[settings.jwt_algorithm])
```

### Presentation Layer — Auth Router

```python
# routers/auth.py
from fastapi import APIRouter, Depends, Request
from fastapi.responses import RedirectResponse
from authlib.integrations.starlette_client import OAuth
from app.services.oauth_service import OAuthService, oauth
from app.auth.jwt import create_access_token
from app.dependencies import get_oauth_service

router = APIRouter(prefix="/auth", tags=["auth"])

@router.get("/google/login")
async def google_login(request: Request):
    """Redirect user to Google's consent screen."""
    redirect_uri = request.url_for("google_callback")
    return await oauth.google.authorize_redirect(request, redirect_uri)

@router.get("/google/callback", name="google_callback")
async def google_callback(
    request: Request,
    service: OAuthService = Depends(get_oauth_service),
):
    """Handle redirect from Google, issue internal JWT."""
    token      = await oauth.google.authorize_access_token(request)
    user_info  = token.get("userinfo")

    user       = await service.get_or_create_google_user(user_info)
    jwt_token  = create_access_token(user.id, user.role.value)

    # Redirect to frontend with token in query param (or set HttpOnly cookie)
    response = RedirectResponse(url=f"/dashboard?token={jwt_token}")
    return response
```

### Protecting Routes — Auth Dependency

```python
# dependencies.py
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jose import JWTError
from app.auth.jwt import decode_access_token

bearer_scheme = HTTPBearer()

async def get_current_user_id(
    credentials: HTTPAuthorizationCredentials = Depends(bearer_scheme),
) -> int:
    try:
        payload = decode_access_token(credentials.credentials)
        return int(payload["sub"])
    except (JWTError, KeyError):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
        )

async def require_admin(user_id: int = Depends(get_current_user_id)) -> int:
    # Extend this to check role from DB or embed role in JWT payload
    return user_id
```

### Rules for OAuth2
- ✅ Always use `authlib` — never hand-roll OAuth token exchange
- ✅ Store `provider` + `provider_id` on the user entity to support multiple providers later
- ✅ Allow `password = None` for Google-only accounts
- ✅ Issue your own internal JWT after OAuth — never trust Google's token on your own API
- ✅ Add `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` to `.gitignore`-protected `.env` only
- ❌ Never expose the Google access token to the frontend
- ❌ Never skip email verification — Google-verified emails are safe to trust directly

---

## 13. Security

- Hash passwords with **bcrypt** via `passlib`
- Use **parameterised queries** only — never raw string SQL
- Sanitise and validate all user inputs via Pydantic
- Set `HttpOnly` and `Secure` flags on cookies
- Use CORS middleware — restrict `allow_origins` in production
- Store secrets in environment variables — never in source code

---

## 14. Testing

- Every service function must have at least one unit test
- Use `pytest` fixtures for DB setup and teardown
- Use `AsyncClient` from `httpx` for API endpoint tests
- Aim for ≥ 80% test coverage

```python
# tests/test_user_service.py
import pytest
from httpx import AsyncClient

@pytest.mark.asyncio
async def test_get_user_returns_404(client: AsyncClient):
    response = await client.get("/users/99999")
    assert response.status_code == 404
    assert response.json()["detail"] == "User not found"
```

---

## 15. Git & Commit Style

- Use **Conventional Commits**: `feat:`, `fix:`, `refactor:`, `chore:`, `docs:`
- One logical change per commit
- Branch naming: `feat/user-auth`, `fix/login-redirect`

---

## 16. What NOT to Do

- ❌ Do not use `print()` for logging — use Python's `logging` module or `loguru`
- ❌ Do not use mutable default arguments (`def f(items=[])`)
- ❌ Do not catch bare `except:` — always catch specific exceptions
- ❌ Do not mix business logic into route handlers
- ❌ Do not commit `.env` files — always add to `.gitignore`
- ❌ Do not use `SELECT *` in queries — always specify columns
- ❌ Do not return raw SQLAlchemy model objects from API endpoints — use schemas
- ❌ Do not use Google's access token as your own API auth token — always issue an internal JWT
- ❌ Do not store OAuth access tokens or refresh tokens in localStorage — use HttpOnly cookies