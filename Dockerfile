# --- Étape 1 : Build de l'application ---
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copie des fichiers de dépendances pour optimiser le cache Docker
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline

# Copie du code source et compilation du JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- Étape 2 : Image d'exécution légère ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Création d'un utilisateur non-root pour la sécurité (UID/GID explicite comme dans FastAPI)
RUN addgroup -S spring -g 1000 && adduser -S spring -u 1000 -G spring
USER spring:spring

# Copie du JAR compilé
COPY --from=build /app/target/*.jar app.jar

# Lancement compatible avec le port dynamique de Vercel
ENTRYPOINT ["sh", "-c", "java -Xmx384m -Dserver.address=0.0.0.0 -Dserver.port=${PORT:-8080} -jar app.jar"]

