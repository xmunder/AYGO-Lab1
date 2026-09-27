FROM amazoncorretto:21

WORKDIR /app
COPY target/arrival-api-gateway-1.0.0.jar app.jar

USER 10001:10001
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
