#!/usr/bin/env bash
echo "Generating javadoc...";
javadoc -d javadoc -private -encoding UTF-8 -sourcepath src/ ru.david.room ru.david.room.server ru.david.room.json src/ru/david/room/*.java