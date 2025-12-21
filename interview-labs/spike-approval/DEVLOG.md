# Development Log

## 2025-12-22

### 1. Docker 镜像拉取失败

*   **问题标题**: `docker-compose up --build` 失败，提示 `failed to resolve source metadata`
*   **问题描述**: 在本地开发环境（Windows 11 + Docker Desktop）中构建项目时，基础镜像（node:18-alpine, eclipse-temurin:21-jre-alpine 等）无法拉取，导致构建过程中断。
*   **问题原因**: 国内网络环境连接 Docker Hub 官方镜像源不稳定，且部分公开镜像源（如网易源）已失效或不可达。
*   **解决方案**:
    1.  创建 `daemon-config-fix.json`，提供了一组可用的国内镜像加速源（DaoCloud, Huecker 等）。
    2.  手动更新用户目录下的 `.docker/daemon.json` 配置。
    3.  重启 Docker Desktop 使配置生效。
    4.  手动执行 `docker pull` 预先拉取基础镜像，绕过 compose 的超时限制。

### 2. 前端界面更新未生效 (缓存问题)

*   **问题标题**: 前端代码修改后，浏览器显示仍为旧版本
*   **问题描述**: 修改了 `ResultPage.tsx` 以添加 "Approved By" 字段和移除冗余标题，但在浏览器中验证时，界面未发生变化。
*   **问题原因**:
    1.  **Docker 构建缓存**: Docker 复用了之前的构建层，未包含最新的代码变更。
    2.  **浏览器/Nginx 缓存**: Nginx 默认可能缓存了 `index.html`，导致浏览器加载旧的入口文件。
*   **解决方案**:
    1.  **强制构建**: 使用 `docker-compose build --no-cache` 强制重新编译前端和后端代码。
    2.  **禁用缓存**: 修改 `nginx.conf`，为 `index.html` 添加 `Cache-Control "no-store, no-cache, must-revalidate"` 响应头，确保浏览器每次请求最新版本。

### 3. Serverless 部署适配

*   **问题标题**: 应用无法动态适配云平台端口和域名
*   **问题描述**: 原始应用配置了硬编码的端口（8089）和后端地址，无法满足 Serverless 平台动态注入环境变量的要求。
*   **问题原因**: Spring Boot 和 Nginx 配置缺乏对环境变量的支持。
*   **解决方案**:
    1.  **后端**: 修改 `application.properties` 为 `server.port=${PORT:8089}`。
    2.  **前端**: 修改 `nginx.conf` 和 `Dockerfile`，使用 Nginx 模板功能支持动态注入 `BACKEND_URL` 环境变量。
    3.  **健康检查**: 引入 `spring-boot-starter-actuator` 提供 `/actuator/health` 端点。

### 4. 审批日志与界面增强

*   **问题标题**: 缺乏审计日志和详细审批信息
*   **问题描述**: 后端未记录审批动作日志，前端结果页面仅显示 "Approved"，未显示具体审批人及规则提示。
*   **问题原因**: 代码实现仅满足了 MVP 的最小功能闭环，未包含可观测性和用户体验细节。
*   **解决方案**:
    1.  **后端**: 在 Controller 和 Handler 中添加 SLF4J 日志记录；API 响应增加 `approver` 字段。
    2.  **前端**: 
        *   在结果表格中增加 "Approved By" 行。
        *   集成 Ant Design 的 `Tooltip` 组件，悬停显示 `< 1000: Team Leader` 等审批规则。
        *   优化 UI，移除 Result 组件默认的副标题，避免信息重复。
