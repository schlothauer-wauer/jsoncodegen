FROM openjdk:8-jre-alpine

ADD release /opt/jsonCodeGen

RUN ln -s /opt/jsonCodeGen/jsonCodeGen.sh /usr/bin/jsonCodeGen
ENV JSONCODEGEN_HOME=/opt/jsonCodeGen
ENTRYPOINT ["jsonCodeGen"]
CMD []