#!/usr/bin/env bash
shopt -s expand_aliases

if [[ $1 = "18" ]]; then
  alias java=java18
fi

if [[ -f "jars/ServerApp.jar" ]]; then
  echo "Запуск сервера...";
  java -Xmx2g -XX:OnOutOfMemoryError="kill -9 %p; sh run-server.sh" -jar jars/ServerApp.jar config/server-config.json
else
  echo "Сначала соберите проект: выполните build.sh";
fi

if [[ $1 = "18" ]]; then
  unalias java
fi