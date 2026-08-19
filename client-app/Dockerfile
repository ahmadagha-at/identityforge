FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl client-app -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/client-app/target/client-app-*.jar app.jar
USER 10001
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
