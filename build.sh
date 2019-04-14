#!/usr/bin/env bash
rm -r jars 2> /dev/null;
rm -r build 2> /dev/null;

mkdir build;
mkdir jars;

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