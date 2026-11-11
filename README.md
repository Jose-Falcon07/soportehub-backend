# SoporteHub · Fase 2

Spring Boot 3.5 + Java 21 + Thymeleaf + PostgreSQL + Redis. Reparto por persona en `REPARTO.md`.

## Cómo ejecutarlo
1. `cp .env.example .env` y cambia las contraseñas y el JWT_SECRET (mínimo 32 caracteres).
2. `export $(grep -v '^#' .env | xargs) && docker compose up -d`
3. `set -a; source .env; set +a`
4. `mvn spring-boot:run -Dspring-boot.run.profiles=demo`
5. Abre http://localhost:8080 . Usuarios de demostración (contraseña `Demo1234!`):
   superadmin@soportehub.test · admin@, supervisor@, agente@, solicitante@ con `istmo.test` o `santaana.test`.
   Sin RESEND_API_KEY los correos no salen: el enlace de invitación aparece en la consola.
6. Pruebas: `mvn test` (la de aislamiento necesita Docker).

## Estado
Este código se escribió sin poder compilarlo ni ejecutarlo. Antes de nada: `mvn -q compile`, corrijan lo que salga,
y verifiquen la versión de Spring Boot del pom.
