# ========= Build stage =========
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# 使用 Maven Wrapper 构建
COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .
RUN chmod +x mvnw
# 预热依赖以提升缓存命中率
RUN ./mvnw -ntp -q -DskipTests dependency:go-offline

# 复制源码并打包
COPY src/ src/
RUN ./mvnw -ntp -DskipTests package

# ========= Runtime stage =========
FROM eclipse-temurin:21-jre
WORKDIR /app

# JVM 与数据源默认配置（可被运行时环境变量覆盖）
ENV JAVA_OPTS="" \
    SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/case_study_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8 \
    SPRING_DATASOURCE_USERNAME=app_user \
    SPRING_DATASOURCE_PASSWORD=app_password \
    SPRING_JPA_HIBERNATE_DDL_AUTO=none \
    SPRING_SQL_INIT_MODE=never \
    SPRING_FLYWAY_ENABLED=false

COPY --from=build /workspace/target/case-study-statistics-by-month-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]