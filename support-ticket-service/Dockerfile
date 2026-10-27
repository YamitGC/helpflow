FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S helpflow && adduser -S helpflow -G helpflow
USER helpflow

COPY support-ticket-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
