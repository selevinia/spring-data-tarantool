FROM tarantool/tarantool:2.11.2-ubuntu20.04

RUN apt-get update
RUN apt-get -y install cartridge-cli
RUN apt-get -y install unzip
RUN apt-get -y install build-essential cmake make gcc git

COPY ./integration-app /opt/integration-app
RUN cartridge build /opt/integration-app

EXPOSE 3301

COPY tarantool-entrypoint.sh /
ENTRYPOINT ["/tarantool-entrypoint.sh"]
