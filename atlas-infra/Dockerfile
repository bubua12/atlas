FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/atlas-infra-1.0.0.jar app.jar
EXPOSE 9300
ENTRYPOINT ["java", "-jar", "app.jar"]
