FROM ubuntu:latest
LABEL author="LoopOfPixels"

ENTRYPOINT ["top", "-b"]

# 1. ETAPA DE CONSTRUCCIÓN (BUILD STAGE)
# Usamos una imagen base de Java con Maven para construir el proyecto.
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Establecer el directorio de trabajo dentro del contenedor.
WORKDIR /app

# Copiar el archivo pom.xml para descargar dependencias primero.
COPY pom.xml .

# Descargar dependencias (solo si pom.xml ha cambiado).
RUN mvn dependency:go-offline

# Copiar todo el código fuente.
COPY src ./src

# Empaquetar la aplicación en un JAR ejecutable.
RUN mvn clean package -DskipTests

# ----------------------------------------------------

# 2. ETAPA DE EJECUCIÓN (RUNNING STAGE)
# Usamos una imagen base más ligera (solo Java Runtime) para la ejecución final.
FROM eclipse-temurin:21-jre-alpine

# Argumento para el nombre del archivo JAR (se toma de la etapa de construcción).
ARG JAR_FILE=/app/target/*.jar

# Copiar el JAR compilado desde la etapa 'build'.
COPY --from=build ${JAR_FILE} app.jar

# Exponer el puerto por defecto de Spring Boot (8080).
EXPOSE 8080

# Comando para ejecutar la aplicación JAR cuando se inicie el contenedor.
ENTRYPOINT ["java", "-jar", "/app.jar"]