# Build the executable Spring Boot jar, including frontend and Minecraft resources.
FROM eclipse-temurin:24-jdk AS build
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sh ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src/ src/
COPY mc-resources/resources/ mc-resources/resources/
RUN sh ./mvnw --batch-mode --no-transfer-progress package -DskipTests

# The running container only needs Java and the packaged application.
FROM eclipse-temurin:24-jre AS runtime
WORKDIR /app
COPY --from=build --chown=10001:10001 /build/target/MasterCrafter-1.0-SNAPSHOT.jar ./app.jar

ENV PORT=10000 \
    SERVER_ADDRESS=0.0.0.0

USER 10001:10001
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
