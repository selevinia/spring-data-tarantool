#!/usr/bin/env bash

set -e

bash -c "cd /opt/integration-app && rm -rf /tmp/run"
cartridge start ${INSTANCE_NAME} --name ${CLUSTER_NAME} --script /opt/integration-app/init.lua --cfg /opt/integration-app/instances.yml

# Assume that user wants to run their own process,
# for example a `bash` shell to explore this image
exec "$@"