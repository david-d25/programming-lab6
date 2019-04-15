#!/usr/bin/env bash
shopt -s expand_aliases

sh clear.sh;

mkdir build;
mkdir jars;

if [[ $1 -eq "18" ]]; then
  alias javac=javac18;
fi

(
  echo "Компиляция клиента...";
  javac -sourcepath src -d build src/ru/david/room/Client.java -encoding UTF-8
) && (
  echo "Сборка клиентского jar...";
  jar cfm jars/ClientApp.jar metadata/CLIENT_MANIFEST.MF -C build .
) && (
  echo "Компиляция сервера...";
  javac -sourcepath src -d build src/ru/david/room/server/Server.java -encoding UTF-8
) && (
  echo "Сборка серверного jar...";
  jar cfm jars/ServerApp.jar metadata/SERVER_MANIFEST.MF -C build .
) && (
  echo "Готово!";
  echo "Чтобы запустить клиентское приложение, выполните run-client.sh";
  echo "Чтобы запустить серверное приложение, выполните run-server.sh";
)

if [[ $1 = "18" ]]; then
  unalias javac;
fi