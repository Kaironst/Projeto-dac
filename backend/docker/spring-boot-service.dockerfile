FROM eclipse-temurin:25-alpine
RUN addgroup spring && adduser spring -D spring -G spring
USER spring:spring
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
