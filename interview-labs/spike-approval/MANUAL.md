# Approval Service Manual

This service demonstrates the Chain of Responsibility pattern using Spring Boot (Backend) and React (Frontend).

## Prerequisites

- JDK 21
- Maven
- Node.js & npm (for local frontend dev)
- Docker Desktop (for containerized deployment)

## Running the Application

### Option 1: Development Mode (Local)

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

Deploy the entire stack (Backend + Frontend) using Docker Compose. This is the recommended way to verify the "MVP" delivery.

**Steps:**

1. **Fix Docker Mirror (China Region only)**:
   If you are in China and have issues pulling images, ensure your `~/.docker/daemon.json` is configured with valid mirrors (see project root `daemon-config-fix.json`).

2. **Build and Run**:
   ```bash
   docker-compose up --build
   ```

3. **Access Application**:
   - **Frontend (User Interface)**: [http://localhost](http://localhost)
   - **Backend API**: [http://localhost:8089/actuator/health](http://localhost:8089/actuator/health) (Health Check)

4. **Stop Application**:
   ```bash
   docker-compose down
   ```

### Option 3: Serverless / Cloud Deployment

The application is cloud-ready.

**Backend Configuration:**
- **Port**: Supports `PORT` environment variable (defaults to 8089).
- **Health Check**: `/actuator/health` endpoint available.

**Frontend Configuration:**
- **Backend URL**: Supports `BACKEND_URL` environment variable.
  - Default: `http://backend:8089` (Docker Compose internal DNS)
  - For Cloud: Set `BACKEND_URL` to your public backend API URL (e.g., `https://api.myapp.com`).

### Option 4: Manual Build & Push (PowerShell)

If you need to build and push the unified Docker image manually in a Windows PowerShell environment:

1. **Set Environment Variable**:
   ```powershell
   $env:DOCKER_USERNAME = "your_username"
   ```

2. **Build Unified Image**:
   ```powershell
   docker build -f Dockerfile.unified -t "$($env:DOCKER_USERNAME)/spike-approval:latest" .
   ```

3. **Push to Registry**:
   ```powershell
   docker push "$($env:DOCKER_USERNAME)/spike-approval:latest"
   ```

---

## API Usage

### Endpoint

`POST /api/approval/submit`

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
curl -X POST http://localhost:8089/api/approval/submit \
  -H "Content-Type: application/json" \
  -d '{"amount": 500, "purpose": "Team Lunch"}'
```

**Response:**

```json
{
  "success": true,
  "approvalId": "uuid-string...",
  "status": "approved",
  "message": "Approved by Team Leader"
}
```

#### 2. Department Manager Approval (1000 <= Amount < 5000)

**Request:**

```bash
curl -X POST http://localhost:8089/api/approval/submit \
  -H "Content-Type: application/json" \
  -d '{"amount": 2500, "purpose": "New Laptops"}'
```

**Response:**

```json
{
  "success": true,
  "approvalId": "uuid-string...",
  "status": "approved",
  "message": "Approved by Department Manager"
}
```

#### 3. CEO Approval (Amount >= 5000)

**Request:**

```bash
curl -X POST http://localhost:8089/api/approval/submit \
  -H "Content-Type: application/json" \
  -d '{"amount": 10000, "purpose": "New Office"}'
```

**Response:**

```json
{
  "success": true,
  "approvalId": "uuid-string...",
  "status": "approved",
  "message": "Approved by CEO"
}
```
