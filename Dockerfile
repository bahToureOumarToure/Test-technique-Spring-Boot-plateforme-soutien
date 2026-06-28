# ============================================================
#  Dockerfile multi-stage
#  Étape 1 (build)  : compile le projet et produit le .jar avec Maven
#  Étape 2 (runtime): image légère ne contenant QUE le .jar + un JRE
# ============================================================

# ---------- Étape 1 : build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# On copie d'abord le pom seul pour mettre en cache le téléchargement
# des dépendances (tant que le pom ne change pas, ce layer est réutilisé).
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Puis le code source, et on construit le jar (tests ignorés ici :
# ils se lancent séparément via `mvn test`, sur H2).
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Étape 2 : runtime ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# On récupère le jar construit à l'étape précédente.
COPY --from=build /app/target/plateforme-soutien-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
