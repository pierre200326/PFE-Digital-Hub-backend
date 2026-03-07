# Dockerfile pour Spring Boot
FROM eclipse-temurin:21-jdk-alpine

# Définir le dossier de travail
WORKDIR /app

# Copier le projet Maven wrapper + pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Télécharger les dépendances Maven
RUN ./mvnw dependency:go-offline -B

# Copier le code source
COPY src ./src

# Builder le jar
RUN ./mvnw clean package -DskipTests

# Exposer le port de l'application
EXPOSE 8080

# Lancer l'application
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]