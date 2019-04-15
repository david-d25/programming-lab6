#!/usr/bin/env bash
shopt -s expand_aliases

if [[ $1 = "18" ]]; then
  alias java=java18
fi

[[ -f "jars/ClientApp.jar" ]] &&
(
  echo "Запуск клиента...";
  java -Xmx4g -XX:OnOutOfMemoryError="kill -9 %p; sh run-client.sh" -jar jars/ClientApp.jar config/client-config.json
) ||
  echo "Сначала соберите проект: выполните build.sh";

if [[ $1 = "18" ]]; then
  unalias java
fi