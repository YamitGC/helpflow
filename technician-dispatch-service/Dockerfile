FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S helpflow && adduser -S helpflow -G helpflow
USER helpflow

COPY technician-dispatch-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
