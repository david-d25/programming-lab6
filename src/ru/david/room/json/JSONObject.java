package ru.david.room.json;

import java.util.HashMap;
import java.util.Map;

/**
 * Реализует json-объект
 * (Сущность, хранящая пары ключ-значение)
 */
public class JSONObject extends JSONEntity {
    private HashMap<String, JSONEntity> items;

    {
        type = JSONType.OBJECT;
    }

    /**
     * Создаёт пустой json-объект
     */
    public JSONObject() {
        this(new HashMap<>());
    }

    /**
     * Создаёт json-объект с указанными значениями
     * @param map карта, значения которой будет содержать json-объект
     */
    public JSONObject(Map<? extends String, ? extends JSONEntity> map) {
        items = new HashMap<>(map);
    }

    /**
     * Создаёт json-объект с указанно парой ключа и значения
     * @param entry пара ключ-значение
     */
    public JSONObject(Map.Entry<String, JSONEntity> entry) {
        items = new HashMap<>();
        items.put(entry.getKey(), entry.getValue());
    }

    /**
     * @return содержимое json-объекта
     */
    public HashMap<String, JSONEntity> getItems() {
        return items;
    }

    /**
     * Устанавливает содержимое json-объекта
     * @param items содержимое
     */
    public void setItems(HashMap<String, JSONEntity> items) {
        this.items = items;
    }

    /**
     * Возвращает элемент json-объекта по указанному ключу. Если элемент не найдет, возвращает null.
     * @param key ключ
     * @return элемент, с которым ассоциируется ключ
     */
    public JSONEntity getItem(String key) {
        return items.get(key);
    }

    /**
     * Добавляет пару ключ-значение в json-объект
     * @param key   ключ элемента
     * @param value значение элемента
     */
    public void putItem(String key, JSONEntity value) {
        items.put(key, value);
    }

    /**
     * Добавляет булев тип и автоматически упаковывает значение в {@link JSONBoolean}
     * @param key   ключ элемента
     * @param value значение элемента
     */
    public void putItem(String key, boolean value) {
        items.put(key, new JSONBoolean(value));
    }

    /***
     * Добавляет число в json-объект и автоматически упаковывает значение в {@link JSONNumber}
     * @param key   ключ элемента
     * @param value добавляемое число
     */
    public void putItem(String key, double value) {
        items.put(key, new JSONNumber(value));
    }

    /**
     * Добавляет указанную пару ключ-значение в json-объект
     * @param entry элемент, который надо добавить
     */
    public void putItem(Map.Entry<String, JSONEntity> entry) {
        items.put(entry.getKey(), entry.getValue());
    }

    /**
     * Удаляет элемент по ключу
     * @param key ключ элемента, который надо удалить
     */
    public void removeItem(String key) {
        items.remove(key);
    }

    /**
     * @return строковое представление массива в виде {key1: elem1, key2: elem2, ..., keyN: elemN}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('{');

        for (Map.Entry<String, JSONEntity> entry : items.entrySet())
            builder.append('"').append(entry.getKey()).append('"').append(": ").append(entry.getValue()).append(", ");

        if (builder.length() > 1)
            builder.delete(builder.length() - 2, builder.length());
        builder.append('}');
        return builder.toString();
    }

    @Override
    protected String toFormattedString(int tabSize, int depth) {
        StringBuilder builder = new StringBuilder();
        String padding = getPaddingString(tabSize, depth);
        String innerPadding = getPaddingString(tabSize, depth+1);
        builder.append(padding).append('{').append('\n');

        for (Map.Entry<String, JSONEntity> entry : items.entrySet()) {
            builder.append(innerPadding).append('"').append(entry.getKey()).append('"').append(": ");
            if (entry.getValue() instanceof JSONArray || entry.getValue() instanceof JSONObject)
                builder.append('\n').append(entry.getValue().toFormattedString(tabSize, depth + 2));
            else
                builder.append(entry.getValue());
            builder.append(",\n");
        }

        if (builder.length() > 1)
            builder.delete(builder.length() - 2, builder.length());
        builder.append('\n').append(padding).append('}');
        return builder.toString();
    }
}
