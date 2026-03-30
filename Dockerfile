# ==============================================
# Backend Dockerfile — Spring Boot + Java 21
# ==============================================

# Etapa 1: Build con Maven
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar descriptores de dependencias primero para cachear la capa
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar fuentes y construir el JAR (sin tests)
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# Etapa 2: Imagen de producción — solo JRE
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar el JAR construido
COPY --from=builder /app/target/*.jar app.jar

# Puerto expuesto por la aplicación
EXPOSE 8080

# Punto de entrada
ENTRYPOINT ["java", "-jar", "app.jar"]
