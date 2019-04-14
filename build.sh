#!/usr/bin/env bash
rm ServerApp.jar 2> /dev/null;
rm ClientApp.jar 2> /dev/null;
rm -r build 2> /dev/null;

mkdir build;

(
  echo "Compiling client...";
  javac -sourcepath src -d build src/ru/david/room/Client.java -encoding UTF-8
) && (
  echo "Creating client jar file...";
  jar cfm ClientApp.jar metadata/CLIENT_MANIFEST.MF -C build .
) && (
  echo "Compiling server...";
  javac -sourcepath src -d build src/ru/david/room/server/Server.java -encoding UTF-8
) && (
  echo "Creating server jar file...";
  jar cfm ServerApp.jar metadata/SERVER_MANIFEST.MF -C build .
)