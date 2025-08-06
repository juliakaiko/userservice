# UserService  
A microservice for managing users and their cards, built on Spring Boot 3.3.4 using PostgreSQL, Redis, Liquibase, and OpenAPI (Swagger).

## 🚀 Key Features  
- User and card management (CRUD operations).
- Data caching with Redis.
- Automated database updates via Liquibase.
- API documentation via Swagger UI.
- Docker support (build and deployment). 

## 🧩 Architecture
This project follows a layered architecture:

- **Controller Layer**: Handles HTTP requests and maps to services.
- **Service Layer**: Contains business logic.
- **Repository Layer**: Interfaces with PostgreSQL using Spring Data JPA.
- **DTO + Mapper Layer**: Used for clean data transfer and separation from entities.
- **Caching Layer**: Redis integration for caching entity data.
- **Global Exception Handling**: Uniform response structure for all errors.

## ⚙️ Technologies
- Java 21
- Spring Boot 3.3.4 (Web, Data JPA, Validation, Redis)
- PostgreSQL (primary database)
- Redis for caching
- Liquibase (migrations)
- MapStruct (DTO mapping)
- Lombok (reducing boilerplate code)
- SpringDoc OpenAPI (API documentation)
- Docker for containerization

## 🧪 Testing
- Unit Testing: JUnit 5 + Mockito for mocking dependencies. 
- Containerized Integration Testing: Testcontainers for running PostgreSQL and Redis in Docker containers during integration tests.

## 📩 Contacts
**Author:** Yuliya Kaiko
**Email:** yuliya.kaiko@innowise.com

