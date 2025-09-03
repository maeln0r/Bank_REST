FROM eclipse-temurin:21-jdk-alpine as build

WORKDIR /app

COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
RUN ./gradlew build || return 0

COPY . .

RUN ./gradlew build -x test

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/app/app.jar"]
