FROM eclipse-temurin:17 as jre-build

# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
         --add-modules java.base,java.sql,java.naming,java.management \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime


FROM debian:bullseye-slim
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build /javaruntime $JAVA_HOME

RUN mkdir /app
WORKDIR /app
COPY target/generator.jar /app

ENTRYPOINT ["java", "-jar", "/app/generator.jar", "-webserver"]
