package ru.david.room.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JSONParser {

    /**
     * Создаёт объектную структуру json по указанному строковому представлению json,
     * предварительно удаляет все пробелы, табуляции и переносы строк.
     *
     * @param json строка, по которой создастся структура
     * @return {@link JSONEntity}, содержащий объектную структуру
     * @throws JSONParseException Когда что-то идёт не так. Используйте сообщения исключения,
     * чтобы понять, в чём проблема.
     */
    public static JSONEntity parse(String json) throws JSONParseException {
        json = json.replaceAll("\\s", "");

        return process(json);
    }

    private static JSONEntity process(String source) throws JSONParseException {
        if (isNull(source))
            return null;

        else if (isBoolean(source))
            return new JSONBoolean(source);

        else if (isString(source))
            return new JSONString(source.substring(1, source.length() - 1));

        else if (isNumber(source))
            return new JSONNumber(Double.parseDouble(source));

        else if (isArray(source)) {
            String trimmed = source.substring(1, source.length()-1);
            ArrayList<Integer> commas = getValidCommas(trimmed);

            if (commas.size() == 0) {
                if (trimmed.length() == 0)
                    return new JSONArray();
                else
                    return new JSONArray(parse(trimmed));
            }

            JSONArray result = new JSONArray();
            for (int a = 0; a < commas.size()+1; a++) {
                if (a == 0)
                    result.addItem(parse(trimmed.substring(0, commas.get(a))));
                else if (a == commas.size())
                    result.addItem(parse(trimmed.substring(commas.get(a-1)+1)));
                else
                    result.addItem(parse(trimmed.substring(commas.get(a-1)+1, commas.get(a))));
            }
            return result;

        } else if (isObject(source)) {
            String trimmed = source.substring(1, source.length()-1);
            ArrayList<Integer> commas = getValidCommas(trimmed);

            if (commas.size() == 0) {
                if (trimmed.length() == 0)
                    return new JSONObject();
                else
                    return new JSONObject(makeObjectEntry(trimmed));
            }

            JSONObject result = new JSONObject();
            for (int a = 0; a < commas.size()+1; a++) {
                if (a == 0)
                    result.putItem(makeObjectEntry(trimmed.substring(0, commas.get(a))));
                else if (a == commas.size())
                    result.putItem(makeObjectEntry(trimmed.substring(commas.get(a-1)+1)));
                else
                    result.putItem(makeObjectEntry(trimmed.substring(commas.get(a-1)+1, commas.get(a))));
            }
            return result;

        } else
            throw new JSONParseException("Не удалось обработать эту часть json: " + source);
    }

    private static Map.Entry<String, JSONEntity> makeObjectEntry(String source) throws JSONParseException{
        int colonIndex = source.indexOf(':');

        if (colonIndex == -1)
            throw new JSONParseException("В json-объекте между ключом и значением должно быть двоеточие: " + source);

        String key = source.substring(0, colonIndex);

        if (key.length() < 3)
            throw new JSONParseException("В этой месте пустой ключ: " + source);

        key = key.substring(1, key.length()-1);
        String value = source.substring(colonIndex+1);

        if (value.length() == 0)
            throw new JSONParseException("В этом месте не указано значение: " + source);

        return new HashMap.SimpleEntry<>(key, parse(value));
    }

    private static ArrayList<Integer> getValidCommas(String source) throws JSONParseException {
        ArrayList<Integer> result = new ArrayList<>();
        int cursor = 0;
        int arrayDepth = 0;
        int objectDepth = 0;
        boolean inString = false;
        while (cursor < source.length()) {
            if (source.charAt(cursor) == ',' && !inString && arrayDepth == 0 && objectDepth == 0)
                result.add(cursor);
            else if (source.charAt(cursor) == '"')
                inString = !inString;
            else if (source.charAt(cursor) == '{' && !inString)
                objectDepth++;
            else if (source.charAt(cursor) == '}' && !inString)
                objectDepth--;
            else if (source.charAt(cursor) == '[' && !inString)
                arrayDepth++;
            else if (source.charAt(cursor) == ']' && !inString)
                arrayDepth--;

            if (arrayDepth < 0 || objectDepth < 0) {
                StringBuilder builder = new StringBuilder();
                builder.append("Не удалось обработать json в этом месте:\n");
                builder.append(source).append('\n');
                for (int a = 0; a < cursor; a++)
                    builder.append(' ');
                throw new JSONParseException(builder.toString());
            }

            cursor++;
        }
        return result;
    }

    private static boolean isObject(String source) {
        return source.length() > 1 && source.startsWith("{") && source.endsWith("}");
    }

    private static boolean isArray(String source) {
        return source.length() > 1 && source.startsWith("[") && source.endsWith("]");
    }

    private static boolean isNumber(String source) {
        try {
            Double.parseDouble(source);
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    private static boolean isString(String source) {
        return source.length() > 1 && source.startsWith("\"") && source.endsWith("\"");
    }

    private static boolean isBoolean(String source) {
        return source.equals("true") || source.equals("false");
    }

    private static boolean isNull(String source) {
        return source.equals("null");
    }
}