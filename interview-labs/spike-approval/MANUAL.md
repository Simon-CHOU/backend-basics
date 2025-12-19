# Approval Service Manual

This service demonstrates the Chain of Responsibility pattern using Spring Boot.

## Prerequisites

- JDK 21
- Maven

## Running the Application

### Option 1: Development Mode

**Backend:**
```bash
mvn spring-boot:run
```
Starts on `http://localhost:8089`.

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```
Starts on `http://localhost:5173`.

### Option 2: Production Mode (Docker Compose)

Deploy the entire stack (Backend + Frontend) using Docker Compose.

**Prerequisites:**
- Docker Desktop (or Docker Engine + Docker Compose)

**Steps:**

1. Build and run containers:
   ```bash
   docker-compose up --build
   ```

2. Access the application:
   - **Frontend**: `http://localhost` (Port 80)
   - **Backend API**: `http://localhost:8089`

**Stopping the application:**
```bash
docker-compose down
```

## API Usage

### Endpoint

`POST /api/approval`

### Request Body

```json
{
  "amount": <number>,
  "purpose": "<string>"
}
```

### Scenarios

#### 1. Team Leader Approval (Amount < 1000)

**Request:**

```bash
curl -X POST http://localhost:8089/api/approval \
  -H "Content-Type: application/json" \
  -d '{"amount": 500, "purpose": "Team Lunch"}'
```

**Response:**

```json
{
  "approvedBy": "Team Leader",
  "status": "APPROVED"
}
```

#### 2. Department Manager Approval (1000 <= Amount < 5000)

**Request:**

```bash
curl -X POST http://localhost:8089/api/approval \
  -H "Content-Type: application/json" \
  -d '{"amount": 2500, "purpose": "New Laptops"}'
```

**Response:**

```json
{
  "approvedBy": "Department Manager",
  "status": "APPROVED"
}
```

#### 3. CEO Approval (Amount >= 5000)

**Request:**

```bash
curl -X POST http://localhost:8089/api/approval \
  -H "Content-Type: application/json" \
  -d '{"amount": 10000, "purpose": "New Office"}'
```

**Response:**

```json
{
  "approvedBy": "CEO",
  "status": "APPROVED"
}
```
