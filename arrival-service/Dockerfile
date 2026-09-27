FROM amazoncorretto:21

WORKDIR /app
COPY target/arrival-service-1.0.0.jar app.jar

USER 10001:10001
ENV PORT=8081
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
