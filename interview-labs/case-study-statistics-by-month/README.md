# Case Study Statistics By Month

一个使用 Spring Boot 和 PostgreSQL 的示例服务。以下为最简运行方式与常用命令。

## 先决条件
- 已安装 `Docker` 与 `Docker Compose`
- 如需本地运行源码：`JDK 21`

## 使用 Docker Compose（推荐）
- 构建并启动：`docker compose up -d --build`
- 访问应用：`http://localhost:8080`
- 查看应用日志：`docker compose logs -f app`
- 停止并清理：`docker compose down`

说明：Compose 会启动 `postgres:16-alpine` 数据库与应用容器，应用默认使用环境变量连接数据库（服务名 `db`）。

## 本地运行（连接本机或容器中的 PostgreSQL）
1) 启动测试数据库（容器方式）：
```
docker run -d --name csbm-postgres \
  -e POSTGRES_DB=case_study_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:16-alpine
```

2) 运行应用（二选一）：
- 使用 Maven Wrapper 启动：
  - Linux/Mac：`./mvnw spring-boot:run`
  - Windows：`mvnw.cmd spring-boot:run`

- 或打包运行：
```
./mvnw -DskipTests package
java -jar target/case-study-statistics-by-month-0.0.1-SNAPSHOT.jar
```

3) 默认数据库连接（可在 `application.properties` 中查看并通过环境变量覆盖）：
- URL：`jdbc:postgresql://localhost:5432/case_study_db`
- 用户名：`postgres`
- 密码：`postgres`
- 支持的覆盖变量：`SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`

## 构建应用镜像并连接外部数据库
```
docker build -t csbm-app .
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/case_study_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres csbm-app
```

## 额外说明
- 数据库初始化暂缓，当前已禁用自动建表与迁移（`spring.jpa.hibernate.ddl-auto=none`、`spring.sql.init.mode=never`、`spring.flyway.enabled=false`）。
- 如需变更数据库连接或账号，请修改 `docker-compose.yml` 中的 `POSTGRES_*` 或在运行应用容器时传入 `SPRING_DATASOURCE_*` 环境变量。