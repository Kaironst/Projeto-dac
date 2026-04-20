FROM gradle:9-jdk25-alpine AS build

#project dir é a pasta a ser acessada e é passada pela dockerfile
ARG PROJECT_DIR

WORKDIR /app/
COPY ./shared ./shared

#publica a dependência /shared no repositório maven local para tornar instalável
WORKDIR /app/shared
RUN --mount=type=cache,target=/home/gradle/.gradle \
  gradle publishToMavenLocal --no-daemon

WORKDIR /app/
COPY ./${PROJECT_DIR} ./${PROJECT_DIR}

WORKDIR /app/${PROJECT_DIR}
#rodando sem testes para o build não falhar caso o banco ou rabbitmq ainda não estejam rodando
RUN --mount=type=cache,target=/home/gradle/.gradle \
  gradle build -x test --no-daemon

FROM eclipse-temurin:25-alpine

RUN addgroup spring && adduser spring -D spring -G spring

USER spring:spring

ARG PROJECT_DIR

COPY --from=build /app/${PROJECT_DIR}/build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
