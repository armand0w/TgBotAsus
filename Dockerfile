
FROM maven:3-eclipse-temurin-21 AS builder
WORKDIR /opt/builder

RUN git clone -b develop https://github.com/armand0w/DBSQLite
RUN git clone -b develop https://github.com/armand0w/tgBotApi

RUN mvn -f DBSQLite/pom.xml install -DskipTests -e -B
RUN mvn -f tgBotApi/pom.xml install -DskipTests -e -B

COPY ./pom.xml ./TgBotAsus/pom.xml
COPY ./src ./TgBotAsus/src
RUN mvn -f ./TgBotAsus/pom.xml clean compile package -DskipTests


FROM azul/zulu-openjdk-alpine:21-jre-headless-latest
LABEL author="Armando Castillo"

RUN apk add --no-cache sudo
ARG USERNAME=tgbot
RUN adduser --gecos "$USERNAME" \
    --disabled-password \
    --shell /bin/sh \
    --uid 1000 \
    ${USERNAME} && \
    echo "$USERNAME:1234" | chpasswd && \
    echo "$USERNAME ALL=(ALL) ALL" > /etc/sudoers.d/"$USERNAME" && chmod 0440 /etc/sudoers.d/"$USERNAME" \
    && addgroup ${USERNAME} wheel \
    && addgroup ${USERNAME} ${USERNAME}

WORKDIR /opt/app
RUN chown -R ${USERNAME}:${USERNAME} /opt/app

USER ${USERNAME}

COPY --from=builder /opt/builder/TgBotAsus/target/TgBotAsus.jar TgBotAsus.jar

ENTRYPOINT ["java"]
CMD ["-jar", "TgBotAsus.jar"]

# docker build . -t armand0w/tgbotasus
