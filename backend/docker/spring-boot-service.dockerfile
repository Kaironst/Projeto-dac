FROM gradle:9-jdk25-alpine AS build

WORKDIR /app/

#project dir é a pasta a ser acessada e é passada pela dockerfile
ARG PROJECT_DIR

COPY ./${PROJECT_DIR} ./${PROJECT_DIR}
COPY ./shared ./shared

#publica a dependência /shared no repositório maven local para tornar instalável
WORKDIR /app/shared
RUN gradle publishToMavenLocal --no-daemon

WORKDIR /app/${PROJECT_DIR}
#rodando sem testes para o build não falhar caso o banco ou rabbitmq ainda não estejam rodando
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:25-alpine

RUN addgroup spring && adduser spring -D spring -G spring

USER spring:spring

ARG PROJECT_DIR

COPY --from=build /app/${PROJECT_DIR}/build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
