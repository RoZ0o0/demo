## Technologies Used

- Java 21
- Spring Boot 3.5.6 (Web, Data JPA, Security)
- PostgreSQL 15 (via Docker Compose)
- Flyway (database migrations)
- MapStruct (DTO mapping)
- Lombok (code generation)
- Springdoc OpenAPI / Swagger (API docs)
- Rest Assured & Testcontainers (testing)
- Maven (build tool)
- Docker + Docker Compose (containerization)

---

## Prerequisites

- Java 21+
- Maven 3.8+
- Docker
- Docker Compose

---

## How to run

### 1. Build the Application
Use Maven to build the application and skip tests:

```
mvn clean package -DskipTests
```

### 2. Start PostgreSQL with Docker Compose

Start the PostgreSQL database using Docker Compose:

```
docker compose up -d
```

### 3. Build the Backend Docker Image

Build a Docker image for the backend application:

```
docker build -t demo-app .
```

### 4. Run the Backend Application

Run the backend app container, connect it to the network, and expose port 8080:

```
docker run -d --name demo-app --network demo-net -p 8080:8080 demo-app
```

---

## Tests

To execute all tests:

```
mvn clean test
```
This will compile the code, run the tests, and show a summary of results

---

## How to stop

To stop and remove the running backend container:

```
docker stop demo-app

docker rm demo-app
```

To stop and remove postgres container and network:

```
docker compose down
```

---

## Access Swagger UI
Once the backend is running, access the API documentation via Swagger at:

```
http://localhost:8080/swagger-ui.html
```

---

## Default Credentials

The default credentials for the application are:

- `Username: admin`

- `Password: secret`

---
