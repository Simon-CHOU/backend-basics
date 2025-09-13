# User Stories - 全局用户信息获取系统

## Story 1: 用户登录认证
**ID**: US-001  
**Title**: 用户通过用户名密码登录系统  
**Description**: 作为一个用户，我希望能够通过用户名和密码登录系统，以便访问需要认证的功能。

### 验收标准 (Acceptance Criteria)
- **AC1.1**: 用户在登录页面输入正确的用户名和密码后，系统返回登录成功响应
- **AC1.2**: 登录成功后，系统生成UUID格式的session token
- **AC1.3**: 用户信息以JSON格式存储在Redis中，key为"session:{token}"
- **AC1.4**: Redis中的会话数据设置合理的TTL（如2小时）
- **AC1.5**: 登录成功后，前端页面跳转到首页
- **AC1.6**: 用户输入错误的用户名或密码时，系统返回明确的错误信息
- **AC1.7**: 登录接口支持CORS，允许前端跨域访问

---

## Story 2: 会话拦截器实现
**ID**: US-002  
**Title**: 实现AccessInterceptor拦截所有需要认证的请求  
**Description**: 作为系统架构师，我需要实现一个拦截器来自动处理用户认证和上下文设置，以便业务代码能够透明地访问用户信息。

### 验收标准 (Acceptance Criteria)
- **AC2.1**: AccessInterceptor能够拦截所有标记为需要认证的Controller方法
- **AC2.2**: 拦截器从HTTP请求Header中提取"Cookie"字段的token值
- **AC2.3**: 使用token从Redis中查询用户会话信息
- **AC2.4**: 成功获取用户信息后，将其反序列化为UserInfo对象
- **AC2.5**: 将UserInfo对象存储到ThreadLocal或TransmittableThreadLocal中
- **AC2.6**: 请求处理完成后，清理ThreadLocal中的用户信息，防止内存泄漏
- **AC2.7**: 当token无效或过期时，返回401 Unauthorized响应
- **AC2.8**: 拦截器处理过程中的异常能够被正确捕获和处理

---

## Story 3: 全局用户信息工具类
**ID**: US-003  
**Title**: 实现UserUtil.getUserInfo()静态方法  
**Description**: 作为开发者，我希望能够在任何业务逻辑中通过静态方法获取当前登录用户信息，而无需传递用户参数。

### 验收标准 (Acceptance Criteria)
- **AC3.1**: UserUtil.getUserInfo()方法能够返回当前线程的用户信息
- **AC3.2**: 方法返回的UserInfo对象包含userId、username、roles等基本信息
- **AC3.3**: 在未登录状态下调用该方法时，返回null或抛出明确的异常
- **AC3.4**: 方法支持在Controller、Service、Repository等各层调用
- **AC3.5**: 在异步处理场景下（如@Async方法），能够正确传递用户上下文
- **AC3.6**: 提供getUserId()、getUsername()等便捷方法
- **AC3.7**: 工具类方法具有良好的性能，单次请求内多次调用不会重复查询Redis

---

## Story 4: 业务接口用户信息展示
**ID**: US-004  
**Title**: 创建展示当前用户信息的业务接口  
**Description**: 作为用户，我希望能够查看当前登录用户的详细信息，以验证全局用户信息获取功能的正确性。

### 验收标准 (Acceptance Criteria)
- **AC4.1**: 提供GET /api/user/profile接口返回当前用户信息
- **AC4.2**: 接口通过UserUtil.getUserInfo()获取用户信息，不接收任何用户参数
- **AC4.3**: 返回的用户信息包括userId、username、loginTime、lastAccessTime等
- **AC4.4**: 接口需要认证，未登录用户访问时返回401状态码
- **AC4.5**: 接口响应格式为标准的JSON格式
- **AC4.6**: 提供GET /api/user/permissions接口返回用户权限信息
- **AC4.7**: 所有业务接口都能正确获取用户上下文，无需显式传递用户参数

---

## Story 5: 前端登录页面
**ID**: US-005  
**Title**: 创建极简的React登录页面  
**Description**: 作为用户，我需要一个简洁的登录界面来输入凭据并访问系统。

### 验收标准 (Acceptance Criteria)
- **AC5.1**: 登录页面包含用户名和密码输入框
- **AC5.2**: 页面具有现代化的UI设计，响应式布局
- **AC5.3**: 登录表单具有基本的客户端验证（非空验证）
- **AC5.4**: 登录成功后，将token存储在浏览器Cookie中
- **AC5.5**: 登录失败时，显示友好的错误提示信息
- **AC5.6**: 页面支持Enter键提交表单
- **AC5.7**: 使用TypeScript确保类型安全
- **AC5.8**: 集成axios或fetch进行HTTP请求

---

## Story 6: 前端首页和用户信息展示
**ID**: US-006  
**Title**: 创建首页展示用户信息  
**Description**: 作为已登录用户，我希望在首页看到我的用户信息，以确认登录状态和验证后端用户上下文功能。

### 验收标准 (Acceptance Criteria)
- **AC6.1**: 首页能够调用后端API获取当前用户信息
- **AC6.2**: 页面展示用户名、登录时间等基本信息
- **AC6.3**: 提供注销功能，清除本地token和后端会话
- **AC6.4**: 未登录用户访问首页时，自动重定向到登录页面
- **AC6.5**: 页面具有良好的加载状态和错误处理
- **AC6.6**: 支持刷新页面后保持登录状态
- **AC6.7**: 提供简单的导航菜单或用户操作区域

---

## Story 7: Docker环境配置
**ID**: US-007  
**Title**: 配置Docker Compose本地开发环境  
**Description**: 作为开发者，我需要一个简单的命令来启动所有依赖服务，以便快速搭建开发和测试环境。

### 验收标准 (Acceptance Criteria)
- **AC7.1**: Docker Compose文件包含Redis服务配置
- **AC7.2**: Redis配置合理的端口映射和数据持久化
- **AC7.3**: 提供开发环境和生产环境的不同配置
- **AC7.4**: 支持一键启动所有依赖服务
- **AC7.5**: 包含健康检查配置，确保服务正常启动
- **AC7.6**: 提供清理和重置环境的便捷命令
- **AC7.7**: 文档说明如何使用Docker Compose进行开发

---

## Story 8: 系统集成测试
**ID**: US-008  
**Title**: 端到端功能验证  
**Description**: 作为QA工程师，我需要验证整个用户会话管理流程的正确性，确保所有组件协同工作。

### 验收标准 (Acceptance Criteria)
- **AC8.1**: 完整的登录-访问-注销流程能够正常工作
- **AC8.2**: 多个并发用户登录时，用户信息不会混淆
- **AC8.3**: 会话过期后，用户需要重新登录
- **AC8.4**: 系统能够处理Redis连接异常等边界情况
- **AC8.5**: 前后端集成无CORS或其他跨域问题
- **AC8.6**: 系统性能满足基本要求（登录响应时间<500ms）
- **AC8.7**: 所有API接口都有适当的错误处理和日志记录
- **AC8.8**: 提供基本的单元测试和集成测试用例

---

## 技术约束和非功能性需求

### 技术栈要求
- **后端**: Spring Boot 3.x, Java 21, Redis, Maven
- **前端**: React 18+, TypeScript, Vite, Axios
- **部署**: Docker Compose
- **测试**: JUnit 5, Jest/React Testing Library

### 性能要求
- 登录接口响应时间 < 500ms
- 用户信息获取响应时间 < 200ms
- 支持至少100个并发用户

### 安全要求
- 密码不能明文存储
- Session token具有合理的过期时间
- 所有API接口都有适当的认证检查
- 防止常见的Web安全漏洞（XSS、CSRF等）

### 可维护性要求
- 代码遵循Clean Code原则
- 关键功能有单元测试覆盖
- 提供完整的API文档
- 代码具有良好的注释和文档