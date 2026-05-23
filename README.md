# Food Delivery (Android + FastAPI Backend)

An end-to-end **Food Delivery** demo project that includes:

- **Android client app** (Gradle Kotlin DSL) inside `app/`
- **FastAPI backend API** inside `backend/`
- **PostgreSQL** database (via Docker Compose)
- Extra services integrated in the backend such as **Firebase (FCM)** notifications, **image upload**, a **chatbot service**, and **VNPay** payment URL generation.

> Repo: `bac-ptit/foot-delivery-android`

---

## Table of contents

- [Project structure](#project-structure)
- [Tech stack](#tech-stack)
- [Quick start](#quick-start)
  - [1) Backend (recommended: Docker Compose)](#1-backend-recommended-docker-compose)
  - [2) Android app](#2-android-app)
- [Environment & configuration](#environment--configuration)
- [API overview](#api-overview)
- [Search tips](#search-tips)
- [Troubleshooting](#troubleshooting)

---

## Project structure

```
.
├── app/                  # Android Studio project (client)
│   ├── app/              # Android application module
│   └── ...
├── backend/              # FastAPI backend
│   ├── main.py           # FastAPI app + routes
│   ├── docker-compose.yml
│   ├── requirements.txt
│   ├── main.sql          # DB init
│   ├── seed_data.sql     # Seed data
│   └── ...
└── README.md
```

---

## Tech stack

### Android (client)

- Kotlin / Android SDK (**compileSdk 34**, **minSdk 24**, **Java 17**)  
- Retrofit + Gson (API requests)
- Glide / Picasso (images)
- Firebase BoM + Firebase Messaging + Analytics (push notifications)

### Backend

- FastAPI + Uvicorn
- SQLAlchemy
- PostgreSQL (Docker)
- Firebase Admin (server-side)
- Auth: OAuth2 Password flow + JWT (`python-jose`, `passlib/bcrypt`)
- Optional: Google GenAI integration (see `google-genai` in `requirements.txt`)

---

## Quick start

### 1) Backend (recommended: Docker Compose)

From the repository root:

```bash
cd backend
docker compose up --build
```

This will start:

- PostgreSQL on `localhost:5432`
- FastAPI on `http://localhost:8000`

The database is initialized automatically using:

- `backend/main.sql`
- `backend/seed_data.sql`

Test:

```bash
curl http://localhost:8000/
```

You should get a welcome message.

### 2) Android app

1. Open Android Studio
2. Open the project **inside `app/`** (this is the Android Studio root)
3. Sync Gradle
4. Run the app on emulator/device

> The Android app uses Retrofit to call the backend. Update the API base URL in the Android source code (often a `BASE_URL` constant) to point to your machine IP.
>
> For emulator:
> - Android Emulator usually uses `http://10.0.2.2:8000` to access your host machine

---

## Environment & configuration

### Backend environment variables

`backend/docker-compose.yml` already provides some defaults:

- `DATABASE_URL`: `postgresql://postgres:password@db:5432/food_delivery`
- `SECRET_KEY`: configured in compose (change for production)
- `ALGORITHM`: `HS256`
- `ACCESS_TOKEN_EXPIRE_MINUTES`: `30`

If you run backend without Docker, export these variables yourself.

### Firebase

- Android: Firebase is configured via `app/app/google-services.json`
- Backend: uses `firebase-admin` (check `backend/firebase_utils.py` and notification related code)

**Important:** Avoid committing real production credentials.

---

## API overview

The backend is a FastAPI app (`backend/main.py`). Some notable endpoints:

- **Auth**
  - `POST /token` (OAuth2 password flow → JWT)
- **Users**
  - `POST /users/`
  - `GET /users/me/`
- **Restaurants**
  - `GET /restaurants/`
  - `GET /restaurants/search/?name=...`
- **Menu items**
  - `GET /menu-items/`
  - `GET /menu-items/search/?name=...`
  - `GET /menu-items/category/search/?category_name=...`
- **Orders**
  - `POST /orders/`
  - `GET /orders/{order_id}/detail`
  - `PUT /orders/{order_id}/status`
- **Chat**
  - `POST /chat/`
- **Notifications**
  - `GET /users/{user_id}/notifications/`
  - `PUT /notifications/{notification_id}/read`
- **Payments (VNPay)**
  - `GET /create-payment?order_id=...&amount=...`
  - `GET /vnpay_return`

---

## Search tips

If you want to quickly find where things are implemented:

- Search backend routes in: `backend/main.py`
- Search DB models in: `backend/models.py`
- Search request/response schemas in: `backend/schemas.py`
- Search auth/JWT code in: `backend/auth.py`
- Search push notifications in: `backend/notification_service.py`

---

## Troubleshooting

### Android app cannot call backend

- If using Android Emulator, use `10.0.2.2` instead of `localhost`
- Make sure FastAPI is running and port `8000` is open
- If calling from a real device, use your computer's LAN IP (e.g. `192.168.x.x`)

### Docker database issues

- Check containers:
  ```bash
  docker ps
  ```
- Reset DB volume if needed (will delete data):
  ```bash
  docker compose down -v
  docker compose up --build
  ```

---

## License

Add a license if you plan to share publicly.
