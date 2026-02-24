# 📬 Notification Service - Spring Boot Microservice

This is the **Notification Service** for the **Open Source Mobile Banking Microservices** project. It handles **all system notifications** such as user authentication events, deposit/withdrawal updates, and maintenance alerts. Built with **Spring Boot** and integrated with **RabbitMQ** for messaging, it also supports **Docker**, **Kubernetes**, and **CI/CD with Jenkins**.

---

## 📦 Features

- ✅ Send email notifications on system events
- ✅ Connects to RabbitMQ for message handling
- ✅ Listens to queues from:
  - `authentication-service`
  - `deposit-service`
  - `withdrawal-service`
  - `maintenance-service`
- ✅ Built and deployed via Jenkins pipelines
- ✅ Dockerized and Kubernetes-ready
- ✅ Logs monitoring via ELK stack

---

## 🛠️ Tech Stack

| Technology   | Description                                  |
|--------------|----------------------------------------------|
| Spring Boot  | Java backend framework                       |
| RabbitMQ     | Message broker for asynchronous communication|
| Docker       | Containerization                             |
| Jenkins      | CI/CD automation                             |
| Kubernetes   | Container orchestration                      |
| ELK Stack    | Logging and monitoring                       |

---

## 🧾 RabbitMQ Configuration

- **Exchange:** `notification.exchange`
- **Queues:**
  - `auth.notifications.queue`
  - `deposit.notifications.queue`
  - `withdrawal.notifications.queue`
  - `maintenance.notifications.queue`
- **Bindings:** Each service is bound to `notification.exchange` with appropriate routing keys.

---

## 🚀 Getting Started

### 1. Clone the Repo

```bash
git clone https://github.com/davidakpele/bank-system/tree/notification-service.git
cd notification-service
```
## Build with Maven

``` ./mvnw clean install ```
## Run with Docker

### Dockerfile
```
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/notification-service.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml
```
version: '3.8'
services:
  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"

  notification-service:
    build: .
    ports:
      - "8082:8082"
    environment:
      - SPRING_RABBITMQ_HOST=rabbitmq
    depends_on:
      - rabbitmq

```
### RUN

```docker-compose up --build```

## 📦  Jenkins CI/CD
 - This project includes a Jenkinsfile for automated build, test, Docker image creation, and Kubernetes deployment. Key stages include:

🧼 - Cleaning workspace

🛠️ - Building with Maven

✅ - Testing

🐳 - Docker image build & push

☸️ - Kubernetes deployment

📊 - ELK monitoring deployment


📮 Sending a Notification
- Messages from other services are published to RabbitMQ using defined routing keys. Notification service consumes the messages and sends emails to users.

## 🧪 Running Tests
```./mvnw test```

## 🧑‍💻  Contributing
 - Contributions are welcome! Please fork the repository and submit a pull request.

## 📄 License


 ## 🌐Part of the Project
 - Mobile Banking API (Microservice Application) using Java, Golang, Python, and Rust.

> Let me know if you'd like this converted into a GitHub-ready format or need individual service README templates as well.
