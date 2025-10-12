Core Service 🚀  
**Central User Management & Authentication System**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/)
[![WebFlux](https://img.shields.io/badge/WebFlux-Reactive-blue)](https://spring.io/reactive)
[![Security](https://img.shields.io/badge/Security-JWT-orange)](https://jwt.io/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue)](https://postgresql.org/)

---

## 📋 Overview
**Core Service** — это центральная система аутентификации и управления пользователями, обеспечивающая единый источник достоверных данных о пользователях во всей платформе.  
Сервис управляет профилями пользователей, поддерживает авторизацию через JWT и взаимодействует с другими микросервисами (питание, тренировки, гейтвей и т.д.).

---

## 🏗️ Architecture

```mermaid
graph TB
    A[User] --> B[Gateway]
    B --> C[Core Service]
    C --> D[PostgreSQL]
    C --> E[Meal Service]
    C --> F[Workout Service]
    
    style C fill:#e1f5fe,stroke:#0288d1,stroke-width:2px
```

---

## 🛠️ Technologies

|Stack|Description|
|---|---|
|**Spring Boot WebFlux**|Reactive web framework|
|**Spring Security**|Authentication & Authorization|
|**JWT**|Token-based security|
|**PostgreSQL**|Primary database|
|**common-lib**|Shared DTOs between services|

---

## 🔒 Security

- JWT token-based authentication
    
- Reactive `Spring Security` configuration
    
- Role-based access control
    
- Secure inter-service communication
    

---

## 🚀 Getting Started

### Prerequisites

- Java **17+**
    
- PostgreSQL **12+**
    
- Maven or Gradle
    

### Run locally

`# Clone repository git clone https://github.com/your-org/core-service.git cd core-service  # Configure database in application.yml spring.datasource.url=jdbc:postgresql://localhost:5432/core_db spring.datasource.username=your_username spring.datasource.password=your_password  # Start application ./mvnw spring-boot:run`

---

## 📚 API Endpoints (examples)

|Method|Endpoint|Description|
|---|---|---|
|`POST`|`/api/auth/register`|Register new user|
|`POST`|`/api/auth/login`|Authenticate and get JWT|
|`GET`|`/api/users/profile`|Get user profile|
|`PUT`|`/api/users/goals`|Update user goals|

---

## 🧩 Future Improvements

- Add **OAuth2 / OpenID Connect** support
    
- Implement **audit logging**
    
- Integrate **Prometheus + Grafana** for metrics
    
- Add **Testcontainers** for integration testing
