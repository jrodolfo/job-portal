# Stage 1: Build the application
FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install wget to fetch the OpenTelemetry Java agent
RUN apk add --no-cache wget

# OpenTelemetry Java Agent
ARG OTEL_JAVA_AGENT_VERSION=2.11.0
RUN mkdir -p /otel \
    && wget -O /otel/opentelemetry-javaagent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Enable auto-instrumentation by default
ENV JAVA_TOOL_OPTIONS="-javaagent:/otel/opentelemetry-javaagent.jar"

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
