
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

WORKDIR /opt/app
RUN mkdir logs

COPY --from=builder /opt/builder/TgBotAsus/target/TgBotAsus.jar TgBotAsus.jar

ENTRYPOINT ["java"]
CMD ["-jar", "TgBotAsus.jar"]

# docker build . -t armand0w/tgbotasus
