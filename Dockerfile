# syntax=docker/dockerfile:1
# Imagen de producción del backend: compila con Maven (JDK 25) y ejecuta en un JRE 25 mínimo (Alpine), con usuario
# sin privilegios, capas separadas (dependencias / aplicación) para builds rápidos y sin herramientas de compilación.

FROM maven:3.10.0-eclipse-temurin-25-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests \
 && java -Djarmode=tools -jar target/fundigsac-backend.jar extract --layers --launcher --destination target/extracted

FROM eclipse-temurin:25-jre-alpine
LABEL org.opencontainers.image.title="fundigsac-backend" \
      org.opencontainers.image.description="API de FUNDIGSAC (monolito modular, Spring Boot 4)" \
      org.opencontainers.image.source="https://github.com/FundigSac/Backend"
RUN addgroup -S -g 10001 fundigsac && adduser -S -D -H -u 10001 -G fundigsac fundigsac
WORKDIR /app
COPY --from=build /workspace/target/extracted/dependencies/ ./
COPY --from=build /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/target/extracted/application/ ./
USER 10001:10001
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseCompactObjectHeaders -XX:+ExitOnOutOfMemoryError -Djava.io.tmpdir=/tmp -Dfile.encoding=UTF-8"
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=3s --start-period=90s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1:8081/actuator/health/readiness || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
