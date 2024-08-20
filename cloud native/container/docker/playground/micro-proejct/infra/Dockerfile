FROM openjdk:21-slim
WORKDIR "/micro-project"
CMD ["./gradlew", "clean", "build"]
ARG JAR_FILE=./api/build/libs/api.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]