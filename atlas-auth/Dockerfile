FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/atlas-auth-1.0.0.jar app.jar
EXPOSE 9100
ENTRYPOINT ["java", "-jar", "app.jar"]
