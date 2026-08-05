FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

RUN apt-get update && apt-get install -y maven

# Копируем весь проект (чтобы были все модули)
COPY . .

# Собираем stat-server вместе с зависимостями
RUN mvn -pl stat-service/stat-server -am clean package -DskipTests -Dcheckstyle.skip

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/stat-service/stat-server/target/stat-server-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 9090

ENTRYPOINT ["java","-jar","app.jar","--spring.config.name=application-docker"]