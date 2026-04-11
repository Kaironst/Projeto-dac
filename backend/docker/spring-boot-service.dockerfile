FROM gradle:9-jdk25-alpine AS build

WORKDIR /app/

ARG PROJECT_DIR

COPY . .

RUN ./shared/gradlew publishToMavenLocal

WORKDIR /app/${PROJECT_DIR}

RUN chmod +x gradlew
RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:25-alpine

RUN addgroup spring && adduser spring -D spring -G spring

USER spring:spring

ARG PROJECT_DIR

COPY --from=build /app/${PROJECT_DIR}/build/libs/*SNAPSHOT.jar app.jar
COPY --from=build /home/gradle/.m2 /home/spring/.m2

ENTRYPOINT ["java","-jar","/app.jar"]
