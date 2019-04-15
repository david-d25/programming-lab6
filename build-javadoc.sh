#!/usr/bin/env bash
shopt -s expand_aliases

echo "Generating javadoc...";

if [[ $1 = "18" ]]; then
  alias javadoc=javadoc18
fi

javadoc -d javadoc -private -encoding UTF-8 -sourcepath src/ ru.david.room ru.david.room.server ru.david.room.json src/ru/david/room/*.java

if [[ $1 = "18" ]]; then
  unalias javadoc
fi