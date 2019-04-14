#!/usr/bin/env bash
[[ -f "jars/ClientApp.jar" ]] &&
(
  echo "Запуск клиента...";
  java -Xmx4g -XX:OnOutOfMemoryError="kill -9 %p; sh run-client.sh" -jar jars/ClientApp.jar config/client-config.json
) ||
  echo "Сначала соберите проект: выполните build.sh";