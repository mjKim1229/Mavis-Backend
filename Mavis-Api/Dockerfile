FROM eclipse-temurin:21-jdk

ARG JAR_FILE=build/libs/Mavis-Api.jar

COPY ${JAR_FILE} app.jar
ARG PROFILE=dev
ENV PROFILE=${PROFILE}

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${PROFILE}","/app.jar"]
