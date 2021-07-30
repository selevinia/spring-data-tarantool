FROM ubuntu:20.04
ARG DEBIAN_FRONTEND=noninteractive
ENV TZ=Europe/Moscow

RUN apt-get update && apt-get install curl -y
RUN curl -L https://tarantool.io/qCLQYP/release/2.9/installer.sh --output installer.sh
RUN chmod +x ./installer.sh
RUN ./installer.sh
RUN apt-get -y install tarantool
RUN apt-get -y install cartridge-cli
RUN apt-get -y install unzip

COPY ./integration-app /opt/integration-app
RUN cartridge build /opt/integration-app

EXPOSE 3301

COPY tarantool-entrypoint.sh /
ENTRYPOINT ["/tarantool-entrypoint.sh"]
