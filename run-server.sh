#!/usr/bin/env bash
shopt -s expand_aliases

if [[ $1 = "18" ]]; then
  alias java=java18
fi

[[ -f "jars/ServerApp.jar" ]] &&
(
  echo "Запуск сервера...";
  java -Xmx4g -XX:OnOutOfMemoryError="kill -9 %p; sh run-server.sh" -jar jars/ServerApp.jar config/server-config.json
) ||
  echo "Сначала соберите проект: выполните build.sh";

if [[ $1 = "18" ]]; then
  unalias java
fi