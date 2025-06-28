# 🍕 Food Delivery Microservices

Modern food delivery platform built with Spring Boot microservices architecture.

## 🏗️ Architecture

- **User Service** (Port 8081) - Authentication, user management
- **Product Service** (Port 8083) - Stores, products, categories  
- **Frontend Service** (Port 8082) - Web UI
- **Eureka Server** (Port 8761) - Service discovery

## 🚀 Tech Stack

- **Backend**: Spring Boot 3.5, Java 17
- **Database**: PostgreSQL
- **Security**: JWT Authentication
- **Image Storage**: Cloudinary
- **Maps**: Mapbox API
- **Service Discovery**: Netflix Eureka
- **Build Tool**: Maven

## 📦 Services

### User Service
- User registration and authentication
- JWT token management
- User profiles

### Product Service  
- Store management
- Product catalog
- Image upload with Cloudinary
- Address geocoding with Mapbox

## 🛠️ Setup

1. Clone repository
2. Set up environment variables
3. Start PostgreSQL
4. Run Eureka Server
5. Start microservices

## 🔧 Environment Variables

```env
DB_PASSWORD=your_password
JWT_SECRET=your_secret_key
CLOUDINARY_CLOUD_NAME=your_cloud
CLOUDINARY_API_KEY=your_key
CLOUDINARY_API_SECRET=your_secret
MAPBOX_ACCESS_TOKEN=your_token
