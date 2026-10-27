# Etapa 1: Build con Maven y JDK 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar POMs y código
COPY pom.xml ./
COPY support-ticket-service/pom.xml ./support-ticket-service/
COPY support-ticket-service/src ./support-ticket-service/src

RUN mvn clean package -pl support-ticket-service -DskipTests

# Etapa 2: Runtime liviano con JRE 21 slim
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios root
RUN addgroup -S helpflow && adduser -S helpflow -G helpflow
USER helpflow

COPY --from=build /app/support-ticket-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
