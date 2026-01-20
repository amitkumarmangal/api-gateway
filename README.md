# API Gateway Service

## 📌 Overview
This project is a **Spring Cloud API Gateway** that acts as a single entry point for all backend microservices.  
It provides:

- 🔐 JWT-based authentication & authorization
- 👮 Fixed USER-based access control (ADMIN / USER)
- 📊 Distributed tracing using Zipkin
- ❤️ Health and metrics via Spring Boot Actuator
- 🔁 Load balancing with Eureka

---

## 🏗 Architecture

```

Client
│
▼
API Gateway
│
├── JWT Authentication Filter
├── Role-based Authorization
├── Zipkin Tracing
└── Actuator Metrics
│
▼
Downstream Microservices

```

---

## ⚙️ Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Cloud Gateway
- Spring Security
- JWT (JSON Web Token)
- Micrometer Tracing
- Zipkin
- Eureka Client
- Actuator

---

## 🔐 Security Model

### JWT Authentication
- All incoming requests are validated using **JWT**
- Token is expected in the `Authorization` header

```

Authorization: Bearer <JWT_TOKEN>

````

### Roles
| Role | Access |
|----|----|
| `ADMIN` | Full access to all services |
| `USER` | Limited access to user-level APIs |

---

## 🌐 Route Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: customer-management
          uri: lb://customer-management
          predicates:
            - Path=/customers/**
          filters:
            - JwtAuthenticationFilter
````
---

## 📊 Distributed Tracing (Zipkin)

### Features

* Automatic Trace & Span creation
* Trace propagation from Gateway → Services
* TraceId and SpanId included in logs

### Configuration

```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0

  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

### Logging Pattern

```yaml
logging:
  pattern:
    level: "%5p [api-gateway,%X{traceId},%X{spanId}]"
```

---

## 🚀 Running Zipkin Locally

```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

Access UI:

```
http://localhost:9411
```

---

## ❤️ Actuator Endpoints

### Enabled Endpoints

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### Useful URLs

| Endpoint   | URL                    |
| ---------- | ---------------------- |
| Health     | `/actuator/health`     |
| Metrics    | `/actuator/metrics`    |
| Prometheus | `/actuator/prometheus` |

---

## 🧪 Sample Request Flow

1. Client sends request with JWT
2. API Gateway validates token
3. Trace is created
4. Request forwarded to service
5. Logs contain same TraceId across services

---

## 🛡 JWT Token Claims Example

```json
admin / user
```

---

## 📦 Running the Application

```bash
mvn spring-boot:run
```

Ensure:

* Eureka Server is running
* Zipkin is running
* Downstream services are registered

---

## ✅ Best Practices

* Use `sampling.probability < 1.0` in production
* Rotate JWT signing keys periodically
* Secure actuator endpoints in production
* Avoid logging sensitive token data

---

## 📄 License

This project is for learning and internal use.

---