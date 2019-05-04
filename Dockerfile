FROM mcr.microsoft.com/java/jdk:11u3-zulu-alpine as build

RUN ["jlink", \
     "--compress=2", \
     "--strip-debug", \
     "--no-header-files", \
     "--no-man-pages", \
     "--module-path", "/usr/lib/jvm/zulu-11-azure-jdk_11.31.11-11.0.3-linux_musl_x64/jmods", \
     "--add-modules", "java.base,java.logging,java.desktop,java.naming,java.prefs,java.xml,java.scripting,java.sql", \
     "--output", "/custom_jre"]

FROM alpine:latest
COPY --from=build /custom_jre /opt/jre/

RUN ln -s /opt/jre/bin/java /usr/bin/

ADD build/release /opt/jsonCodeGen/

ENTRYPOINT ["/opt/jsonCodeGen/jsonCodeGen.sh"]

CMD ["--help"]