# Builds OrderService.application (and its OrderService.module dependency)
# from source and packages a runnable image.
#
# Build context must be app/ (the reactor root), e.g.:
#   docker build -f app/OrderService.application/docker/Dockerfile \
#                --build-arg DESIGN_TIME_ENV=dev \
#                -t order-service:dev-1 app/
#
# DESIGN_TIME_ENV selects which app/OrderService.application/configuration/{env}.substvar
# gets baked into the jar (see the Maven profiles in OrderService.application/pom.xml).
# This is the "based on branch, use the respective env property file" step, resolved by
# the Jenkinsfile before it reaches here (see deploy/ci-cd/Jenkinsfile).

FROM maven:3.9.9-eclipse-temurin-11 AS builder
ARG DESIGN_TIME_ENV=dev
WORKDIR /build
COPY pom.xml .
COPY OrderService.module OrderService.module
COPY OrderService.application OrderService.application
RUN mvn -B -f pom.xml -pl OrderService.application -am clean package \
        -P${DESIGN_TIME_ENV} -DskipTests

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=builder /build/OrderService.application/target/order-service-app.jar app.jar
COPY --from=builder /build/OrderService.application/target/lib ./lib
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
