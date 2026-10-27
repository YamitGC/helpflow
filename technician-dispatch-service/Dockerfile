# Etapa 1: Build con Maven y JDK 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar POMs para optimizar caché de capas de dependencias
COPY pom.xml ./
COPY technician-dispatch-service/pom.xml ./technician-dispatch-service/
COPY technician-dispatch-service/src ./technician-dispatch-service/src

RUN mvn clean package -pl technician-dispatch-service -DskipTests

# Etapa 2: Runtime liviano con JRE 21 slim
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios root por seguridad empresarial
RUN addgroup -S helpflow && adduser -S helpflow -G helpflow
USER helpflow

COPY --from=build /app/technician-dispatch-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]

