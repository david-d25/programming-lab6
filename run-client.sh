#!/usr/bin/env bash
shopt -s expand_aliases

if [[ $1 = "18" ]]; then
  alias java=java18
fi

if [[ -f "jars/ClientApp.jar" ]]; then
  echo "Запуск клиента...";
  java -Xmx2g -XX:OnOutOfMemoryError="kill -9 %p; sh run-client.sh" -jar jars/ClientApp.jar config/client-config.json
else
  echo "Сначала соберите проект: выполните build.sh";
fi

if [[ $1 = "18" ]]; then
  unalias java
fi