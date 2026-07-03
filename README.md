# Geospatial Application

This is a Spring Boot application for geospatial data processing.

## Development Setup

For local development without Docker:

1. Ensure you have PostgreSQL with PostGIS extension installed
2. Configure the database connection in `application-local.yml`
3. Run the application with the `local` profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Building the Application

To build the application without Docker:

```bash
./mvnw clean package
```

The built JAR file will be in the `target` directory.

## External Datasource Support

External datasource support is documented in
[`docs/external-datasource-support.md`](docs/external-datasource-support_.md).
It covers the current implementation, datasource endpoints, connector extension
points, and guidance for adding API sources, database sources

## Caching Configuration

The application supports two caching implementations:

1. **JVM Cache** (default): In-memory caching using Ehcache
2. **Redis Cache**: Distributed caching using Redis

### Switching Between Cache Types

The cache type is controlled by the `cache.type` property:

- For JVM caching: `cache.type=jvm`
- For Redis caching: `cache.type=redis`

This can be set in the application properties or as an environment variable.

### Redis Configuration

When using Redis caching, the following properties can be configured:

```yaml
cache:
  type: reddis
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    ssl: ${REDIS_SSL:false}
```

In production, Redis is used by default with the configuration in `application-prod.yml`.

### Cache Configuration

Caches are configured in the application properties:

```yaml
cache:
  caches:
    - name: cacheName
      keyType: java.lang.String
      valueType: fully.qualified.ClassName
      ttl: 60  # Time to live in minutes
```

The same cache configuration works for both JVM and Redis caching.
