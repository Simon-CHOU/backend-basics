# 用户会话管理系统

一个基于Spring Boot + React + Redis的用户会话管理系统，演示完整的用户认证、会话管理和权限控制流程。

## 📋 项目概述

本项目是一个面试级别的用户会话管理系统，展示了现代Web应用中用户认证和会话管理的最佳实践。系统采用前后端分离架构，后端使用Spring Boot提供RESTful API，前端使用React + TypeScript构建现代化用户界面，Redis作为会话存储。

### 🎯 核心功能

- ✅ 用户登录/注销
- ✅ 基于Redis的会话管理
- ✅ Token认证机制
- ✅ 用户信息获取
- ✅ 权限验证
- ✅ 会话过期处理
- ✅ 健康检查
- ✅ Docker容器化部署

## 🏗️ 系统架构

```
┌─────────────────┐    HTTP/REST    ┌─────────────────┐    Redis Protocol    ┌─────────────────┐
│                 │ ──────────────► │                 │ ──────────────────► │                 │
│   React前端     │                 │  Spring Boot    │                     │      Redis      │
│  (Port: 3000)   │ ◄────────────── │   后端服务      │ ◄────────────────── │   (Port: 6379)  │
│                 │    JSON响应     │  (Port: 8080)   │    会话数据         │                 │
└─────────────────┘                 └─────────────────┘                     └─────────────────┘
        │                                   │                                         │
        │                                   │                                         │
        ▼                                   ▼                                         ▼
┌─────────────────┐                 ┌─────────────────┐                     ┌─────────────────┐
│   用户界面      │                 │   业务逻辑      │                     │   会话存储      │
│ - 登录表单      │                 │ - 用户认证      │                     │ - 会话数据      │
│ - 用户信息展示  │                 │ - 会话管理      │                     │ - 过期控制      │
│ - 权限控制      │                 │ - 权限验证      │                     │ - 键值存储      │
└─────────────────┘                 └─────────────────┘                     └─────────────────┘
```

### 🔧 技术栈

**后端技术栈：**
- Java 21
- Spring Boot 3.2.x
- Spring Data Redis
- Spring Web
- Lombok
- Jackson
- Maven

**前端技术栈：**
- React 18
- TypeScript
- Vite
- Ant Design
- Axios
- React Router
- js-cookie

**基础设施：**
- Redis 7
- Docker & Docker Compose
- Nginx

## 📁 项目结构

```
lab029/
├── README.md                          # 项目文档
├── docker-compose.yml                 # Docker编排文件
├── docker/                            # Docker配置
│   └── redis/
│       └── redis.conf                 # Redis配置文件
├── user-session-be/                   # 后端项目
│   ├── Dockerfile                     # 后端Docker文件
│   ├── pom.xml                        # Maven配置
│   └── src/main/
│       ├── java/com/interview/usersession/
│       │   ├── UserSessionApplication.java    # 启动类
│       │   ├── config/
│       │   │   └── WebConfig.java             # Web配置
│       │   ├── controller/
│       │   │   ├── AuthController.java        # 认证控制器
│       │   │   ├── UserController.java        # 用户控制器
│       │   │   └── HealthController.java      # 健康检查控制器
│       │   ├── interceptor/
│       │   │   └── AccessInterceptor.java     # 访问拦截器
│       │   ├── model/
│       │   │   ├── LoginRequest.java          # 登录请求模型
│       │   │   ├── LoginResponse.java         # 登录响应模型
│       │   │   └── UserInfo.java              # 用户信息模型
│       │   ├── service/
│       │   │   ├── AuthService.java           # 认证服务
│       │   │   └── SessionService.java        # 会话服务
│       │   └── util/
│       │       └── UserUtil.java              # 用户工具类
│       └── resources/
│           ├── application.yml                # 默认配置
│           └── application-docker.yml         # Docker环境配置
└── user-session-fe/                   # 前端项目
    ├── Dockerfile                     # 前端Docker文件
    ├── nginx.conf                     # Nginx配置
    ├── package.json                   # NPM配置
    ├── vite.config.ts                 # Vite配置
    ├── tsconfig.json                  # TypeScript配置
    ├── index.html                     # HTML模板
    └── src/
        ├── App.tsx                    # 主应用组件
        ├── main.tsx                   # 应用入口
        ├── index.css                  # 全局样式
        ├── components/
        │   ├── LoginForm.tsx          # 登录表单组件
        │   └── UserProfile.tsx        # 用户信息组件
        ├── pages/
        │   ├── LoginPage.tsx          # 登录页面
        │   └── HomePage.tsx           # 首页
        ├── services/
        │   ├── api.ts                 # API基础配置
        │   └── userService.ts         # 用户服务
        ├── types/
        │   └── user.ts                # 类型定义
        └── utils/
            └── auth.ts                # 认证工具函数
```

## 🚀 快速开始

### 前置要求

- Docker & Docker Compose
- JDK 21 (如果本地开发)
- Node.js 18+ (如果本地开发)
- Maven 3.8+ (如果本地开发)

### 🐳 Docker部署（推荐）

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd lab029
   ```

2. **启动所有服务**
   ```bash
   docker-compose up -d
   ```

3. **查看服务状态**
   ```bash
   docker-compose ps
   ```

4. **访问应用**
   - 前端应用: http://localhost:3000
   - 后端API: http://localhost:8080/api
   - Redis管理界面: http://localhost:8081 (用户名: admin, 密码: admin123)

5. **停止服务**
   ```bash
   docker-compose down
   ```

### 🛠️ 本地开发

#### 启动Redis
```bash
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

#### 启动后端服务
```bash
cd user-session-be
./mvnw spring-boot:run
```

#### 启动前端服务
```bash
cd user-session-fe
npm install
npm run dev
```

## 🔐 测试账号

系统内置了以下测试账号：

| 用户名 | 密码 | 角色 | 描述 |
|--------|------|------|------|
| admin | admin123 | ADMIN | 管理员账号 |
| user | user123 | USER | 普通用户账号 |
| test | test123 | USER | 测试账号 |

## 📡 API接口文档

### 认证接口

#### 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**响应：**
```json
{
  "token": "uuid-token",
  "user": {
    "userId": "1",
    "username": "admin",
    "displayName": "管理员",
    "email": "admin@example.com",
    "role": "ADMIN",
    "active": true,
    "sessionId": "session-uuid",
    "loginTime": "2024-01-15T10:30:00",
    "lastAccessTime": "2024-01-15T10:30:00",
    "sessionExpiry": "2024-01-15T11:30:00"
  }
}
```

#### 用户注销
```http
POST /api/auth/logout
Authorization: Bearer <token>
```

#### Token验证
```http
GET /api/auth/validate
Authorization: Bearer <token>
```

### 用户接口

#### 获取当前用户信息
```http
GET /api/user/current
Authorization: Bearer <token>
```

#### 获取用户权限
```http
GET /api/user/permissions
Authorization: Bearer <token>
```

#### 获取用户资料
```http
GET /api/user/profile
Authorization: Bearer <token>
```

### 健康检查

#### 基础健康检查
```http
GET /api/health
```

#### 详细健康检查
```http
GET /api/health/detailed
```

## 🔧 配置说明

### 后端配置

主要配置文件：`user-session-be/src/main/resources/application.yml`

```yaml
# 会话配置
app:
  session:
    expire-time: 3600  # 会话过期时间（秒）
    redis-key-prefix: "user:session:"  # Redis键前缀
  
  # 排除路径（不需要认证）
  exclude-paths:
    - "/api/auth/login"
    - "/api/auth/logout"
    - "/api/health"
```

### 前端配置

环境变量文件：`user-session-fe/.env`

```env
# API基础URL
VITE_API_BASE_URL=http://localhost:8080/api

# 应用配置
VITE_APP_TITLE=用户会话管理系统
VITE_APP_VERSION=1.0.0
```

## 🏛️ 架构设计

### 会话管理流程

```
用户登录 ──► 验证凭据 ──► 创建会话 ──► 生成Token ──► 存储Redis ──► 返回Token
    │                                                      │
    └──────────────────── 登录成功 ◄─────────────────────────┘

用户请求 ──► 提取Token ──► 验证Token ──► 查询会话 ──► 更新访问时间 ──► 处理请求
    │                                      │
    └──────── Token无效/过期 ◄──────────────┘
```

### 权限控制

系统采用基于角色的访问控制（RBAC）：

1. **拦截器验证**：`AccessInterceptor`拦截所有请求
2. **Token提取**：从Header、Cookie或参数中提取token
3. **会话验证**：通过Redis验证会话有效性
4. **用户上下文**：将用户信息存储到ThreadLocal
5. **权限检查**：根据用户角色进行权限验证

### 数据模型

#### 用户信息模型
```java
public class UserInfo {
    private String userId;          // 用户ID
    private String username;        // 用户名
    private String displayName;     // 显示名称
    private String email;           // 邮箱
    private String role;            // 角色
    private boolean active;         // 是否活跃
    private String sessionId;       // 会话ID
    private LocalDateTime loginTime;        // 登录时间
    private LocalDateTime lastAccessTime;   // 最后访问时间
    private LocalDateTime sessionExpiry;    // 会话过期时间
}
```

#### 会话存储结构
```
Redis Key: user:session:{token}
Redis Value: {
  "userId": "1",
  "username": "admin",
  "sessionId": "session-uuid",
  "loginTime": "2024-01-15T10:30:00",
  "lastAccessTime": "2024-01-15T10:30:00"
}
TTL: 3600秒
```

## 🔍 监控和日志

### 健康检查

系统提供多层次的健康检查：

1. **基础检查**：`/api/health` - 返回简单的健康状态
2. **详细检查**：`/api/health/detailed` - 包含Redis连接、JVM信息等
3. **Docker健康检查**：容器级别的健康监控

### 日志配置

- **控制台日志**：开发环境实时查看
- **文件日志**：生产环境持久化存储
- **日志轮转**：防止日志文件过大
- **分级日志**：不同组件使用不同日志级别

## 🚨 故障排除

### 常见问题

1. **Redis连接失败**
   ```bash
   # 检查Redis服务状态
   docker-compose ps redis
   
   # 查看Redis日志
   docker-compose logs redis
   ```

2. **后端服务启动失败**
   ```bash
   # 查看后端日志
   docker-compose logs backend
   
   # 检查端口占用
   netstat -an | findstr :8080
   ```

3. **前端无法访问后端**
   - 检查CORS配置
   - 验证API_BASE_URL设置
   - 确认网络连通性

4. **会话过期问题**
   - 检查Redis TTL设置
   - 验证时间同步
   - 查看会话配置

### 调试模式

启用调试模式：
```bash
# 后端调试
SPRING_PROFILES_ACTIVE=dev docker-compose up backend

# 前端调试
VITE_DEBUG=true npm run dev
```

## 🔒 安全考虑

1. **Token安全**：使用UUID生成随机token
2. **会话过期**：自动过期机制防止会话劫持
3. **CORS配置**：限制跨域访问
4. **输入验证**：前后端双重验证
5. **错误处理**：避免敏感信息泄露
6. **HTTPS**：生产环境使用HTTPS

## 📈 性能优化

1. **Redis连接池**：复用连接减少开销
2. **静态资源缓存**：Nginx缓存静态文件
3. **Gzip压缩**：减少传输数据量
4. **懒加载**：按需加载组件
5. **内存优化**：合理设置JVM参数

## 🤝 贡献指南

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开Pull Request

## 📄 许可证

本项目仅用于学习和面试演示目的。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 项目Issues: [GitHub Issues]()
- 邮箱: developer@example.com

---

**注意**：本项目是一个演示项目，主要用于展示用户会话管理的技术实现。在生产环境中使用时，请根据实际需求进行安全加固和性能优化。