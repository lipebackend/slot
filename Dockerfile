# syntax=docker/dockerfile:1

FROM node:22-alpine AS frontend-build
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /src
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
COPY backend/src src
RUN rm -rf src/main/resources/static/*
COPY --from=frontend-build /app/dist/slot-frontend/browser/ src/main/resources/static/
RUN chmod +x mvnw && ./mvnw -B -DskipTests package
RUN jlink \
    --add-modules java.base,java.logging,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.sql,jdk.unsupported,java.xml,jdk.crypto.ec \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=zip-6 \
    --output /custom-jre

FROM alpine:3.20
WORKDIR /app
RUN addgroup -S slot && adduser -S slot -G slot
COPY --from=backend-build /custom-jre /opt/jre
COPY --from=backend-build /src/target/slot-0.0.1-SNAPSHOT.jar /app/app.jar
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:MaxRAMPercentage=70.0 -XX:TieredStopAtLevel=1 -Xss256k"
USER slot
EXPOSE 8080
ENTRYPOINT ["/opt/jre/bin/java", "-jar", "/app/app.jar"]
