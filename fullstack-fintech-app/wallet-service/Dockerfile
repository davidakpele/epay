FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B 2>/dev/null || true

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    apk add --no-cache maven && \
    mvn clean package -DskipTests -B -q && \
    java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /build/target/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/application/ ./

RUN apk add --no-cache curl

USER appuser

EXPOSE 8035

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
    CMD curl -f http://localhost:8035/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/tmp/heapdump.hprof", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "org.springframework.boot.loader.launch.JarLauncher"]