# --- Build stage ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

RUN apt-get update && apt-get install -y maven

# Копируем весь проект (чтобы Maven видел все модули и зависимости)
COPY . .

# Собираем ewm-service вместе с зависимостями
RUN mvn -pl ewm-service/ewn-service-admin -am clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Копируем jar из target ewm-service
COPY --from=build /app/ewm-service/ewn-service-admin/target/ewn-service-admin-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar","--spring.config.name=application-docker"]
