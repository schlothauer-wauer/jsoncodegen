# FROM openjdk:8-jre-alpine
FROM azul/zulu-openjdk-alpine:17.0.4.1

ADD release /opt/jsonCodeGen

RUN ln -s /opt/jsonCodeGen/jsonCodeGen.sh /usr/bin/jsonCodeGen
ENV JSONCODEGEN_HOME=/opt/jsonCodeGen
ENTRYPOINT ["jsonCodeGen"]
CMD []