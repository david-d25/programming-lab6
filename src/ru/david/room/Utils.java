package ru.david.room;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    /**
     * Возвращает красивую полосу прогресса
     * @param progress прогресс от 0 до 1
     * @return строка с красивой полосой прогресса
     */
    public static String progressBar(float progress) {
        return  "[" + Stream.generate(() ->  "=").limit((int)(25*progress)).collect(Collectors.joining()) + ">" +
                Stream.generate(() -> " ").limit((int)(25*(1 - progress))).collect(Collectors.joining()) + "]" +
                " " + Math.round(1000f * progress)/10f + "%\r";
    }

    /**
     * Возвращает количество информации в читабельном виде.
     * Например, при входных данных 134217728, возвращает "128 МиБ"
     * @param bytes Количество информации в байтах
     * @return Читабельное представление информации
     */
    public static String optimalInfoUnit(long bytes) {
        String[] units = {"байт", "КиБ", "МиБ", "ГиБ", "ТиБ"};
        long result = bytes;
        int divided = 0;
        while (result > 1024) {
            result /= 1024;
            divided++;
        }
        if (divided >= units.length)
            divided = units.length-1;
        return result + " " + units[divided];
    }
}
