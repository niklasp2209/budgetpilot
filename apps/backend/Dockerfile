FROM eclipse-temurin:25-jdk AS builder

WORKDIR /workspace
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY apps/backend ./apps/backend
RUN mkdir -p ./apps/frontend

RUN chmod +x ./gradlew
RUN ./gradlew :apps:backend:build -x test || ./gradlew :apps:backend:build -x test

FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app
COPY --from=builder /workspace/apps/backend/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
