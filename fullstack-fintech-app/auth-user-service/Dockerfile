FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

RUN mkdir -p /var/www/uploads/images && \
    chmod -R 755 /var/www/uploads/images

VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]