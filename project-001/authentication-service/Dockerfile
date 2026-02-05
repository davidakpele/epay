FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
RUN mkdir -p /app/uploads
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]