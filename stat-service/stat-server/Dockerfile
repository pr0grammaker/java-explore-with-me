# Используем официальный образ JDK для сборки
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Копируем pom.xml и исходники
COPY . .

# Собираем jar через Maven
RUN ./mvnw clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre

WORKDIR /app

# Копируем jar из предыдущего этапа
COPY --from=build /app/target/stat-server-0.0.1-SNAPSHOT.jar app.jar

# Указываем порт, который слушает приложение
EXPOSE 9090

# Запускаем Spring Boot приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
