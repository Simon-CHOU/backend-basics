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

### Option 5: Kubernetes (Production)

For large-scale production deployment, use the provided Kubernetes manifests in the `k8s/` directory.

1.  **Build and Push Images**:
    Build your images and push them to a container registry (e.g., Docker Hub, Tencent Cloud CCR).
    ```powershell
    # Example for Backend
    docker build -t <YOUR_REGISTRY>/approval-backend:latest .
    docker push <YOUR_REGISTRY>/approval-backend:latest

    # Example for Frontend
    docker build -t <YOUR_REGISTRY>/approval-frontend:latest ./frontend
    docker push <YOUR_REGISTRY>/approval-frontend:latest
    ```

2.  **Update Manifests**:
    Edit `k8s/backend.yaml` and `k8s/frontend.yaml` to replace `<YOUR_REGISTRY>` with your actual registry address.

3.  **Apply to Cluster**:
    ```bash
    kubectl apply -f k8s/backend.yaml
    kubectl apply -f k8s/frontend.yaml
    ```

4.  **Verify**:
    ```bash
    kubectl get pods
    kubectl get services approval-frontend
    ```

---

## FAQ

**Q: Can I pull my image from anywhere after `docker push`?**

**A:** It depends on your repository's visibility:
- **Public Repository**: Anyone can pull the image if they know the name (e.g., `docker pull username/image`).
- **Private Repository**: Only authenticated users can pull. In Kubernetes, you must create an `imagePullSecret` and reference it in your Deployment to allow the cluster to pull from a private registry.
- **Cloud Registry (CCR/ACR)**: Usually private by default. When using a cloud-managed K8S (like TKE), the cluster is often pre-configured to pull from its own registry.

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
