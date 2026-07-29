# --- Build stage ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

RUN apt-get update && apt-get install -y maven

# Копируем весь проект (чтобы Maven видел все модули и зависимости)
COPY . .

# Собираем ewm-service вместе с зависимостями
RUN mvn -pl ewm-service -am clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Копируем jar из target ewm-service
COPY --from=build /app/ewm-service/target/ewm-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
