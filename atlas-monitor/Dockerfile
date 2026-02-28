FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/atlas-monitor-1.0.0.jar app.jar
EXPOSE 9400
ENTRYPOINT ["java", "-jar", "app.jar"]
