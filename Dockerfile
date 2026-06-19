# --- Build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# System tools required by the pipeline: ffmpeg, python, yt-dlp.
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg python3 python3-pip curl \
    && curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp \
        -o /usr/local/bin/yt-dlp \
    && chmod a+rx /usr/local/bin/yt-dlp \
    && rm -rf /var/lib/apt/lists/*

# Python AI modules + their dependencies.
COPY python ./python
RUN pip3 install --no-cache-dir --break-system-packages -r python/requirements.txt

COPY --from=build /app/target/*.jar app.jar

ENV PYTHON_DIR=/app/python
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
