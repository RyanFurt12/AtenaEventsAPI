# ─── Stage 1: build com Maven ────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY src ./src

RUN mvn package -DskipTests -B

# ─── Stage 2: imagem leve para rodar ─────────────────────────────────────────
FROM eclipse-temurin:17-jre AS runner

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
