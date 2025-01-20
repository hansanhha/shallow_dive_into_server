FROM gradle:8.6.0-jdk21 AS builder

WORKDIR "/project"
COPY . .
RUN gradle build

FROM openjdk:21-slim

WORKDIR /app
COPY --from=builder /project/api/build/libs/api.jar .

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/api.jar"]