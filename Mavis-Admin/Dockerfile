FROM eclipse-temurin:21-jdk

ARG JAR_FILE=build/libs/Mavis-Admin.jar
COPY ${JAR_FILE} app.jar

ARG PROFILE=dev
ENV SPRING_PROFILES_ACTIVE=${PROFILE}

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app.jar"]
