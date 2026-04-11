FROM gradle:9-jdk25-alpine AS build

WORKDIR /app/

ARG PROJECT_DIR

COPY . .

WORKDIR /app/shared
RUN chmod +x ./gradlew
RUN ./gradlew publishToMavenLocal

WORKDIR /app/${PROJECT_DIR}
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

FROM eclipse-temurin:25-alpine

RUN addgroup spring && adduser spring -D spring -G spring

USER spring:spring

ARG PROJECT_DIR

COPY --from=build /app/${PROJECT_DIR}/build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
