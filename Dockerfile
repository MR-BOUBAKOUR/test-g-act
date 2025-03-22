# Dockerfile.dev
# Image optimisée pour la production/développement

FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copie du fichier POM pour télécharger les dépendances
COPY pom.xml .
COPY src ./src

# Installation de Maven et 
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Création du jar sans tests
RUN mvn package -DskipTests

# Utilisation du JRE pour une image finale plus légère
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copie du jar compilé depuis la phase de build
COPY --from=build /app/target/*.jar app.jar

# Exposition du port et commande d'exécution
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
