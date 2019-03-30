#!/usr/bin/env bash
rm App.jar 2> /dev/null;
rm -r build 2> /dev/null;
mkdir build;
echo "Compiling...";
javac18 -sourcepath src -d build src/ru/david/room/Main.java -encoding UTF-8 &&
(
  echo "Creating jar file...";
  jar cfm App.jar src/MANIFEST.MF -C build .
)