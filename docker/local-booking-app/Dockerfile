FROM gradle:8.14.3-jdk21@sha256:21bd311ed01360c189b8870c6b6e988199ff10f72d445d02fb39d3cff9da91d7 AS build

WORKDIR /workspace
COPY --chown=gradle:gradle settings.gradle.kts build.gradle.kts gradle.properties ./
COPY --chown=gradle:gradle gradle ./gradle
COPY --chown=gradle:gradle local-app ./local-app
RUN gradle :local-app:installDist --no-daemon

FROM eclipse-temurin:21-jre-jammy@sha256:d63bd8d9b171999cbed8576f2c76e874dd4856791a358536e5c4d407e77edc13

RUN useradd --create-home --uid 10001 bookwright
WORKDIR /app
COPY --from=build --chown=bookwright:bookwright /workspace/local-app/build/install/local-app ./
USER bookwright
EXPOSE 8080
ENTRYPOINT ["/app/bin/local-app"]
