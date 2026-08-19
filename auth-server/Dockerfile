FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl auth-server -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/auth-server/target/auth-server-*.jar app.jar
RUN mkdir -p /app/keys && chown -R 10001:10001 /app
USER 10001
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
