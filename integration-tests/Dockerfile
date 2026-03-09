FROM node:24 AS frontend-build
WORKDIR /app

COPY endpoint-insights-ui/package*.json ./
RUN npm ci
COPY endpoint-insights-ui/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-25 AS backend-build
WORKDIR /app

COPY endpoint-insights-api/pom.xml .
RUN mvn dependency:go-offline -B

COPY endpoint-insights-api/src ./src
COPY --from=frontend-build /app/dist/endpoint-insights-ui/browser/ ./src/main/resources/static/
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre-noble AS runtime
WORKDIR /app

LABEL org.opencontainers.image.source=https://github.com/git-stuff-done/EndpointInsights
LABEL org.opencontainers.image.description="EndpointInsights"

RUN groupadd -r endpointinsights && useradd -r -g endpointinsights endpointinsights

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

RUN curl -L https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz \
    | tar -xz -C /opt && \
    ln -s /opt/apache-jmeter-5.6.3 /opt/jmeter

ENV JMETER_HOME=/opt/jmeter
ENV PATH="${JMETER_HOME}/bin:${PATH}"

COPY --from=backend-build /app/target/*.jar app.jar

RUN chown -R endpointinsights:endpointinsights /app /opt/apache-jmeter-5.6.3

USER endpointinsights

EXPOSE 8080

HEALTHCHECK --interval=5s --timeout=3s --start-period=30s --retries=5 \
  CMD curl -f http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]