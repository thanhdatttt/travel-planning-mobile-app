# 📍 Travel Planner Location App

A mobile application that helps users plan trips by discovering locations, creating itineraries, reviewing places, and sharing travel plans.

## 🚀 Project Overview

Travel Planner Location App allows users to:

- Search and explore travel destinations
- Create and manage trip plans
- Add custom locations
- Review and rate places
- Share itineraries
- Get AI-powered suggestions
- View places on interactive maps

This application focuses on trip planning and location management.  
It does NOT include social networking, payment systems, or ticket booking features.

---

# 🏗️ System Architecture

Android App (Java)  
        │  
        │ REST API (HTTPS)  
        ▼  
Node.js + Express (TypeScript)  
        │  
        ▼  
Prisma ORM  
        │  
        ▼  
PostgreSQL Database  

---

# 📱 Mobile Application (Android)

## Technology Stack

- Java
- Android Studio
- MVVM Architecture
- Retrofit (API calls)
- Gson (JSON parsing)
- Google Maps SDK
- Material Design Components

## Main Features

1. Authentication (Login / Register)
2. User Profile Management
3. Location Search & Discovery
4. Trip Planning
5. Reviews & Ratings
6. Map Integration
7. AI Travel Suggestions
8. User-added Locations

---

# 🌐 Backend API

## Technology Stack

- Node.js
- Express.js
- TypeScript
- Prisma ORM
- PostgreSQL
- JWT Authentication
- bcrypt (password hashing)

## Folder Structure

### Server
```
server/
│
├── src/
│ ├── controllers/
│ ├── routes/
│ ├── middleware/
│ ├── services/
│ ├── prisma/
│ ├── utils/
│ └── index.ts
│
├── prisma/
│ └── schema.prisma
│
├── .env
├── package.json
└── tsconfig.json
```


# 🔐 Authentication

- JWT-based authentication
- Password hashing using bcrypt
- Protected routes via middleware
- Optional role-based authorization

---

# 🤖 AI Features

The application integrates AI to:

- Suggest destinations based on user preferences
- Recommend optimized travel schedules
- Generate trip summaries
- Provide smart search suggestions

Possible implementation:
- OpenAI API
- Custom recommendation engine

---

# 🔎 Core Features

## User Management
- Register
- Login
- Edit profile
- Change password

## Location Management
- Search locations
- Filter by category
- View location details
- Add new location
- Edit/delete owned location

## Reviews & Ratings
- Rate locations (1–5 stars)
- Write reviews
- View average rating

## Trip Planning
- Create trip
- Add multiple locations
- Reorder destinations
- Set schedule
- Make trip public/private

## Map Integration
- Display locations on map
- Show routes between destinations
- View nearby places

## Sharing
- Share trip via link
- Export trip summary

---

# ⚙️ Installation Guide

## Backend Setup

```
cd backend
npm install
```

Create `.env` file:

```
DATABASE_URL="postgresql://user:password@localhost:5432/travel_app"
JWT_SECRET_KEY="your_secret_key"
PORT=5000
```

Run server:
```
npm run dev
```

---

## Android Setup

1. Open project in Android Studio
2. Configure API base URL
3. Add Google Maps API key
4. Run on emulator or physical device

---

# 🧪 API Testing

Recommended tools:

- Postman
- Thunder Client
- Swagger (optional integration)

---

# 📦 Deployment

## Backend Hosting Options
- Render
- Railway
- Fly.io
- VPS (Docker recommended)

## Database Hosting Options
- NeonDB

---

# 🛡️ Security Considerations

- Use HTTPS
- Secure JWT secret
- Validate request inputs
- Sanitize user-generated content
- Configure CORS properly
- Apply rate limiting

---

# 📈 Future Improvements

- Offline mode (local caching)
- Advanced recommendation engine
- Admin dashboard
- Location moderation system
- Image upload with cloud storage
- Push notifications
- Multi-language support

---

# 📄 License

This project is licensed under the MIT License.

---