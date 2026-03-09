FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

LABEL org.opencontainers.image.source=https://github.com/git-stuff-done/EndpointInsights
LABEL org.opencontainers.image.description="Backend API for EndpointInsights"

RUN groupadd -r endpointinsights && useradd -r -g endpointinsights endpointinsights

COPY --from=build /app/target/*.jar app.jar

RUN chown -R endpointinsights:endpointinsights /app

USER endpointinsights

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]