
# docker buildx build -f src/test/resources/tgBotAsus/Dockerfile . --no-cache --platform linux/amd64,linux/arm64 --tag armand0w/devicesbot:beta --push
# docker buildx build -f src/test/resources/tgBotAsus/Dockerfile . --no-cache --platform linux/arm64 --tag armand0w/devicesbot:beta --push
FROM azul/zulu-openjdk-alpine:21-jre-headless-latest

WORKDIR /opt/app
RUN mkdir logs

COPY target/TgBotAsus.jar TgBotAsus.jar

ENTRYPOINT ["java"]
CMD ["-jar", "TgBotAsus.jar"]
