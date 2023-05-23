FROM openjdk:17

RUN mkdir /app
WORKDIR /app
COPY target/generator-standalone.jar /app

ENTRYPOINT ["java", "-jar", "/app/generator-standalone.jar"]
