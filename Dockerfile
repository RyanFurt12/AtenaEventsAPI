# ─── Stage 1: build com Maven ────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copia o pom.xml primeiro (melhor cache de layers)
COPY pom.xml ./

# Baixa as dependências (fica em cache se o pom.xml não mudar)
RUN mvn dependency:go-offline -B

# Copia o código-fonte
COPY src ./src

# Build do JAR (sem testes para acelerar o build)
RUN mvn package -DskipTests -B

# ─── Stage 2: imagem leve para rodar ─────────────────────────────────────────
FROM eclipse-temurin:17-jre AS runner

WORKDIR /app

# Copia o JAR gerado
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
