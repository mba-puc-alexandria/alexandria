# alexandria-backend/Dockerfile

# ---------- Estágio 1: Build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o projeto e baixa somente as dependências necessárias ao empacotamento.
# `dependency:go-offline` tenta resolver dependências opcionais de plugins e
# torna a primeira build desnecessariamente lenta.
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Estágio 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Usuário não-root por segurança
RUN addgroup -S spring \
    && adduser -S spring -G spring \
    && mkdir /app/logs \
    && chown -R spring:spring /app
USER spring:spring

# Copia apenas o JAR gerado no estágio anterior
COPY --from=build /app/target/alexandria-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
