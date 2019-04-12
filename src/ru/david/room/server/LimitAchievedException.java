package ru.david.room.server;

import java.io.IOException;

/**
 * Исключение для {@link LimitedInputStream}. Используется для обозначения
 * ситуаций, когда количество информации, прочитанное из потока,
 * превысило установленный предел.
 */
class LimitAchievedException extends IOException {}
