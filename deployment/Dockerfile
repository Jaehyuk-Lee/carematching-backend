FROM ubuntu/jre:21-24.04_stable

WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
