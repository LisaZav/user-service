FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn dependency:go-offline -B

RUN mvn package

FROM openjdk:17-alpine
WORKDIR /app

COPY --from=builder /app/target/user-service-1.0.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]