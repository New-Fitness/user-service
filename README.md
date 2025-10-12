Core Service 🚀  
**Central User Management & Authentication System**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/)
[![WebFlux](https://img.shields.io/badge/WebFlux-Reactive-blue)](https://spring.io/reactive)
[![Security](https://img.shields.io/badge/Security-JWT-orange)](https://jwt.io/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue)](https://postgresql.org/)

---

## 📋 Overview
**Core Service** — This is a central authentication and user management system, providing a single source of truth for user data across the entire platform.
The service manages user profiles, supports JWT authentication, and interacts with other microservices (nutrition, training, gateway, etc.).

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

- Java **21+**
    
- PostgreSQL **16+**
    
- Gradle

---
