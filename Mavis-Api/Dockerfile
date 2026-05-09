FROM eclipse-temurin:21-jdk

ARG JAR_FILE=build/libs/Mavis-Api.jar
COPY ${JAR_FILE} app.jar

ARG PROFILE=dev
ENV SPRING_PROFILES_ACTIVE=${PROFILE}

ENTRYPOINT ["java", "-jar", "/app.jar"]
