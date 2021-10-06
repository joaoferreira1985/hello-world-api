FROM openjdk:16.0.1-jdk-slim-buster


WORKDIR /app

COPY target/*-fat.jar /app/vertx.jar
COPY src/main/conf/my-application-conf-environment.json /app/config.conf


CMD [ "java","-jar", "/app/vertx.jar","-conf","/app/config.conf" ]
