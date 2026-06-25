FROM eclipse-temurin:21-jre-alpine AS base
RUN adduser -D waypoint
USER waypoint
WORKDIR /app

FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM base AS final
COPY --chown=waypoint:waypoint --from=build /build/target/*.jar app.jar
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/v1/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
