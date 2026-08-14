# alexandria-backend/Dockerfile

# ---------- Estágio 1: Build ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copia primeiro só o pom.xml para aproveitar cache de dependências
# (só refaz o download se o pom.xml mudar, não a cada mudança no código)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Agora copia o código-fonte e builda
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Estágio 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Usuário não-root por segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia apenas o JAR gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]