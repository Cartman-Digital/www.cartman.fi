FROM openjdk:17

RUN mkdir /app
WORKDIR /app
COPY target/generator.jar /app

ENTRYPOINT ["java", "-jar", "/app/generator.jar", "-webserver"]
