FROM internal.example.com/hub.docker.com/gradle:8.14.4-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

FROM internal.example.com/hub.docker.com/eclipse-temurin:21-jdk
WORKDIR /app
ENV TZ=Asia/Tashkent
COPY --from=builder /app/build/libs/*.jar app.jar

##EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]