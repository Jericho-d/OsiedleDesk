# -------------- Multi-stage Dockerfile --------------
FROM eclipse-temurin:25-jdk as builder
WORKDIR /app
COPY --chown=1000:1000 . /app
RUN ./gradlew build -x test

FROM eclipse-temurin:25-jdk
ARG JAR_FILE=build/libs/administrative-tool-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/build/libs/*jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
