FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/protocol-benchmark-*.jar app.jar

EXPOSE 8080 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
