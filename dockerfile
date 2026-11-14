# ============================================================
# Stage 1 - Build
# ============================================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Capa de dependencias: solo se invalida si cambia el pom.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

RUN mvn -B clean package -DskipTests

# ============================================================
# Stage 2 - Runtime
# ============================================================
FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="soportehub"

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    imagemagick \
    ghostscript \
    wget && \
    find /etc/ImageMagick* -name policy.xml -exec \
    sed -i 's/rights="none" pattern="PDF"/rights="read|write" pattern="PDF"/g' {} \; && \
    rm -rf /var/lib/apt/lists/*

ENV PORT=8081

WORKDIR /app

RUN groupadd --system soportehub && \
    useradd --system --gid soportehub soportehub

COPY --from=builder --chown=soportehub:soportehub /build/target/*.jar /app/app.jar

RUN mkdir -p /app/uploads /opt/soportehub/uploads /var/log/soportehub && \
    chown -R soportehub:soportehub /app /opt/soportehub /var/log/soportehub

USER soportehub

EXPOSE 8081

HEALTHCHECK --interval=30s \
    --timeout=10s \
    --start-period=90s \
    --retries=3 \
    CMD wget --no-verbose --tries=1 -O /dev/null http://localhost:${PORT:-8081}/api/v1/public/health || exit 1

ENTRYPOINT ["sh","-c","exec java \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -Djava.security.egd=file:/dev/./urandom \
    -jar /app/app.jar"]