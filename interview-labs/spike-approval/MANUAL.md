# Approval Service Manual

This service demonstrates the Chain of Responsibility pattern using Spring Boot.

## Prerequisites

- JDK 21
- Maven

## Running the Application

```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8089`.

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
