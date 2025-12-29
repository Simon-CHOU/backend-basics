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
*   **问题描述**: 修改了 `ResultPage.tsx` 以添加 "Approved By" 字段 and 移除冗余标题，但在浏览器中验证时，界面未发生变化。
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

### 5. 镜像体积优化与 GraalVM 适配

*   **问题标题**: 前后端 Docker 镜像体积过大，构建效率低
*   **问题描述**: 后端镜像包含完整 JRE (300MB+)，前端镜像包含构建缓存，导致拉取和部署缓慢。
*   **问题原因**: 
    1.  后端未使用 Native Image 技术，且构建层未分离依赖下载。
    2.  前端 Dockerfile 未利用分层缓存机制，且包含 Source Maps。
    3.  缺少 `.dockerignore` 文件，导致构建上下文过大。
*   **解决方案**:
    1.  **后端 GraalVM 适配**:
        *   引入 `native-maven-plugin` 插件。
        *   使用 `ghcr.io/graalvm/native-image-community:21` 进行 Native Image 编译。
        *   使用 `debian:bookworm-slim` 作为极简运行时底座。
        *   分离 Maven 依赖下载层，利用 Docker 缓存。
    2.  **前端构建优化**:
        *   **禁止 Source Maps**: 在 `vite.config.ts` 中设置 `sourcemap: false`，从 86MB 降至 21.5MB。
        *   **精简镜像**: 切换至 `nginx:alpine-slim`。
        *   分离 `package.json` 拷贝与 `npm ci` 步骤。
    3.  **构建上下文优化**:
        *   新增 `.dockerignore` 文件，排除 `node_modules`, `target`, `.git` 等无关文件。

### 6. 统一镜像构建失败 (Docker Ignore 冲突)

*   **问题标题**: 执行 `docker build -f Dockerfile.unified` 时提示 `frontend: not found`
*   **问题描述**: 在使用统一 Dockerfile 构建前后端一体化镜像时，构建进程无法找到 `frontend/` 目录。
*   **问题原因**: 
    1.  **配置冲突**: 之前的 `.dockerignore` 文件中为了优化单体构建，排除了 `frontend` 目录。
    2.  **构建上下文受限**: 当从根目录构建统一镜像时，被忽略的目录对 Docker 引擎不可见。
*   **解决方案**:
    1.  **修正配置**: 从根目录的 `.dockerignore` 中移除 `frontend` 排除项。
    2.  **PowerShell 命令适配**: 在 Windows PowerShell 环境下，使用 `$env:DOCKER_USERNAME = "..."` 正确设置环境变量，并使用 `"$($env:DOCKER_USERNAME)/..."` 进行变量插值构建。

## 2025-12-29

### 7. Railway 部署统一镜像失败 (start.sh No such file or directory)

*   **问题标题**: Railway 部署 Docker Image 后容器启动失败，提示 `/app/start.sh` 不存在
*   **问题描述**: 将 `Dockerfile.unified` 构建的镜像推送到 Docker Hub 并在 Railway 以 Docker Image 方式部署时，容器启动阶段反复崩溃，日志显示 `exec container process (missing dynamic library?) '/app/start.sh': No such file or directory`。
*   **问题原因**:
    1.  **脚本行尾格式不兼容**: 在 Windows 环境下（Git 自动转换 CRLF）导致 `start.sh` 以 CRLF 结尾，Linux 容器执行脚本时 Shebang 解析失败，表现为无法执行 `/app/start.sh`。
    2.  **Nginx 配置路径不正确**: `nginx.conf` 为完整主配置（包含 `events/http` 块），复制到 `conf.d/default.conf` 会导致 Nginx 启动不稳定或行为异常。
    3.  **不必要的依赖增加构建波动**: `nginx-extras` 依赖较多，构建时偶发拉取失败（如 apt 502），影响可重复构建与部署稳定性。
*   **解决方案**:
    1.  **统一镜像启动修复**: 在 `Dockerfile.unified` 中对 `/app/start.sh` 执行 `sed -i 's/\\r$//'` 强制转换为 LF，并使用 `CMD [\"/bin/sh\", \"/app/start.sh\"]` 启动，避免 Shebang 行尾问题。
    2.  **Nginx 配置修正**: 将 `nginx.conf` 复制到 `/etc/nginx/nginx.conf`，与其配置结构匹配。
    3.  **降低构建依赖**: 将运行时依赖从 `nginx-extras` 调整为 `nginx`，减少依赖面，提升构建稳定性。
    4.  **本地验证**: 本地构建并运行统一镜像后，`/health` 返回 `200`，前后端链路可正常启动并对外服务。
