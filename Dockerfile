FROM eclipse-temurin:26-jre

# Install only the runtime libraries required by the application
RUN apt-get update \
    && apt-get install -y --no-install-recommends liblmdb0 liblz4-1 \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root user
RUN groupadd --system app \
    && useradd --system --gid app --create-home --home-dir /opt/app app

WORKDIR /opt/app
COPY --chown=app:app target/eelaa-*.jar app.jar

USER app

EXPOSE 7178
ENTRYPOINT ["java"]
CMD ["-XX:+ExitOnOutOfMemoryError", "-Xms512m", "-Xmx8g", "-XX:+UseCompactObjectHeaders", "-jar","app.jar"]
