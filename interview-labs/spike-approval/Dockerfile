# Stage 1: Build Native Image
FROM ghcr.io/graalvm/native-image-community:21 AS build
WORKDIR /app

# Install Maven
RUN microdnf install -y maven

# Copy configuration first for caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -Pnative native:compile -DskipTests

# Stage 2: Runtime
# Using a glibc-based small image since the build environment is glibc
FROM debian:bookworm-slim
WORKDIR /app

# Copy the native binary
COPY --from=build /app/target/spike-approval /app/spike-approval

EXPOSE 8089
ENTRYPOINT ["/app/spike-approval"]
