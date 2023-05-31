FROM eclipse-temurin:20-jre-alpine

WORKDIR /app
COPY target/generator.jar /app/generator.jar

ENTRYPOINT ["java", "-jar", "/app/generator.jar", "-webserver"]
