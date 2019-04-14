#!/usr/bin/env bash
[[ -f "jars/ServerApp.jar" ]] &&
(
  echo "Запуск сервера...";
  java -Xmx4g -XX:OnOutOfMemoryError="kill -9 %p; sh run-server.sh" -jar jars/ServerApp.jar config/server-config.json
) ||
  echo "Сначала соберите проект: выполните build.sh";