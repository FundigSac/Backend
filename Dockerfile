# ==============================================================================
# Dockerfile para FUNDIGSAC Backend (Java 21 LTS + Spring Boot)
# ==============================================================================
FROM backend-backend:latest

USER spring:spring
WORKDIR /app

# Copiar el artefacto JAR compilado y empaquetado
COPY target/backend-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational \
               -XX:+ExitOnOutOfMemoryError \
               -XX:+HeapDumpOnOutOfMemoryError \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.threads.virtual.enabled=true"

# Healthcheck interno del contenedor
HEALTHCHECK --interval=20s --timeout=5s --start-period=25s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
