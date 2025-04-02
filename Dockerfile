# Build stage
FROM maven:3-eclipse-temurin-24 AS builder
WORKDIR /opt/builder

RUN git clone -b develop https://github.com/armand0w/DBSQLite && \
    git clone -b develop https://github.com/armand0w/tgBotApi && \
    mvn -f DBSQLite/pom.xml install -DskipTests -e -B && \
    mvn -f tgBotApi/pom.xml install -DskipTests -e -B

COPY pom.xml ./TgBotAsus/
COPY src ./TgBotAsus/src
RUN mvn -f ./TgBotAsus/pom.xml clean package -DskipTests -e -B


# Runtime stage
FROM azul/zulu-openjdk-alpine:24-jre-headless-latest
LABEL maintainer="Armando Castillo" \
      version="0.5.2" \
      description="Telegram Bot ASUS"

ENV APP_USER=tgbot \
    APP_UID=1000 \
    APP_HOME=/opt/app \
    JAVA_OPTS="-Xms64m -Xmx192m"

RUN addgroup -g $APP_UID $APP_USER && \
    adduser -D -u $APP_UID -G $APP_USER $APP_USER && \
    mkdir -p $APP_HOME && chown -R ${APP_USER}:${APP_USER} $APP_HOME && \
    mkdir -p /home/$APP_USER && \
    mkdir -p /opt/db && chown -R ${APP_USER}:${APP_USER} /opt/db && \
    ln -s /home/$APP_USER/config.json $APP_HOME/config.json

WORKDIR $APP_HOME
USER $APP_USER

COPY --from=builder --chown=$APP_USER:$APP_USER /opt/builder/TgBotAsus/target/TgBotAsus.jar ./

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD ps aux | grep java | grep TgBotAsus.jar || exit 1

ENTRYPOINT ["sh", "-c"]
CMD ["java --enable-native-access=ALL-UNNAMED $JAVA_OPTS -jar TgBotAsus.jar"]
