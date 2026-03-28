FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY --chown=1000:1000 . /app
RUN ./gradlew build -x test

FROM eclipse-temurin:25-jdk
COPY --from=builder /app/build/libs/*jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
