# Code-Driven Workflow Engine

一个基于Spring Boot的工作流引擎项目，使用代码驱动的方式定义和管理工作流程。

## 项目结构

```
code-driven-workflow-engine/
├── app/                          # Spring Boot应用主模块
│   ├── src/main/java/com/example/dec/
│   │   ├── engine/              # 工作流引擎核心组件
│   │   ├── domain/              # 领域模型
│   │   ├── service/             # 服务层
│   │   ├── task/                # 任务引擎实现
│   │   └── constant/            # 常量定义
│   ├── src/main/resources/
│   │   ├── application.yml      # 默认配置（PostgreSQL）
│   │   ├── application-dev.yml  # 开发环境配置（H2数据库）
│   │   └── schema.sql           # 数据库初始化脚本
│   └── pom.xml                  # Maven配置
├── docker/
│   └── compose.yml              # PostgreSQL数据库配置
└── README.md                    # 项目说明
```

## 快速开始

### 方式一：开发环境（推荐）

使用H2内存数据库，无需额外配置即可快速启动：

```bash
# 进入应用目录
cd app

# 使用开发环境配置启动应用
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 方式二：生产环境

使用PostgreSQL数据库：

```bash
# 1. 启动PostgreSQL数据库
cd docker
docker-compose up -d

# 2. 等待数据库启动完成（约30秒）

# 3. 启动应用
cd ../app
mvn spring-boot:run
```

## 应用访问

- **应用地址**: http://localhost:8080
- **H2数据库控制台**: http://localhost:8080/h2-console（仅限开发环境）

## 数据库访问

### 开发环境（H2）

- **URL**: `jdbc:h2:mem:testdb`
- **用户名**: `sa`
- **密码**: （留空）
- **访问方式**: 浏览器访问 http://localhost:8080/h2-console

### 生产环境（PostgreSQL）

- **URL**: `jdbc:postgresql://localhost:5432/dec`
- **用户名**: `dec`
- **密码**: `dec`
- **端口**: 5432

## 应用管理

### 启动应用

```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 生产环境
mvn spring-boot:run
```

### 停止应用

在运行应用的终端中按 `Ctrl + C` 停止应用。

### 重新启动

如果需要重新启动应用：

```bash
# 1. 停止应用（Ctrl + C）
# 2. 重新执行启动命令
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 重新编译

如果修改了代码，建议重新编译：

```bash
# 清理并重新编译
mvn clean compile spring-boot:run -Dspring-boot.run.profiles=dev
```

## 数据库管理

### H2数据库（开发环境）

1. 访问 http://localhost:8080/h2-console
2. 输入连接信息：
   - JDBC URL: `jdbc:h2:mem:testdb`
   - User Name: `sa`
   - Password: （留空）
3. 点击 "Connect" 按钮连接

### PostgreSQL数据库（生产环境）

```bash
# 查看数据库状态
docker-compose ps

# 连接数据库
docker-compose exec db psql -U dec -d dec

# 停止数据库
docker-compose down

# 重启数据库
docker-compose restart
```

## 技术栈

- **Java**: 21
- **Spring Boot**: 3.3.4
- **数据库**: H2（开发）/ PostgreSQL（生产）
- **构建工具**: Maven
- **容器**: Docker（用于PostgreSQL）

## 开发说明

- 修改代码后应用会自动重启（Spring Boot DevTools）
- H2数据库中的数据在应用重启后会丢失
- 生产环境的PostgreSQL数据会持久化保存
- 应用启动时会自动执行 `schema.sql` 初始化数据库结构

## 故障排除

### 端口冲突

如果8080端口被占用，可以修改 `application.yml` 或 `application-dev.yml` 中的端口配置：

```yaml
server:
  port: 8081  # 修改为其他端口
```

### 数据库连接问题

1. 确保数据库服务正在运行
2. 检查连接配置是否正确
3. 确认数据库用户权限

### 编译错误

如果遇到编译错误，尝试清理并重新编译：

```bash
mvn clean compile
```

## 日志查看

应用运行时的日志会直接显示在终端中，包含：
- 应用启动信息
- 数据库连接状态
- HTTP请求记录
- 错误信息（如有）