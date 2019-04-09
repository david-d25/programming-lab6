package ru.david.room.json;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Инкапсулирует json-массив
 */
public class JSONArray extends JSONEntity {
    private ArrayList<JSONEntity> items;

    {
        type = JSONType.ARRAY;
    }

    /**
     * Создаёт json-массив с указанными элементами
     * @param items элементы создаваемого массива
     */
    public JSONArray(JSONEntity... items) {
        this.items = new ArrayList<>(Arrays.asList(items));
    }

    /**
     * Создаёт json-массив с указанными элементами и автоматически запаковывает в элементы типа {@link JSONEntity}
     * Если тип передаваемого элемента не поддерживается, добавляет вместо него {@link JSONString} с содержимым,
     * соответствующим строковому представлению переданного элемента. Используется в случае, когда нужно создать
     * массив из разных типов элементов
     * @param items элементы создаваемого массива
     */
    public JSONArray(Object... items) {
        this.items = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof JSONEntity)
                this.items.add((JSONEntity)item);
            else if (item instanceof Number)
                this.items.add(new JSONNumber((Double)item));
            else if (item instanceof String)
                this.items.add(new JSONString((String)item));
            else if (item == null)
                this.items.add(null);
            else
                this.items.add(new JSONString(item.toString()));
        }
    }

    /**
     * Создаёт пустой json-массив
     */
    public JSONArray() {
        this.items = new ArrayList<>();
    }

    /**
     * Возвращает элемент массива по указанному индексу
     * @param index индекс нужного элемента
     * @return элемент массива по этому индексу
     */
    public JSONEntity getItem(int index) {
        if (index < 0 || index >= items.size())
            return null;
        else
            return items.get(index);
    }

    /**
     * Удаляет элемент массива по указанному индексу
     * @param index индекс элемента, который нужно удалить
     */
    public void removeItem(int index) {
        if (index >= 0 && index < items.size())
            items.remove(index);
    }

    /**
     * @return размер массива
     */
    public int size() {
        return items.size();
    }

    /**
     * Добавляет в массив элемент
     * @param entity элемент, который надо добавить
     */
    public void addItem(JSONEntity entity) {
        items.add(entity);
    }

    /**
     * @return Строковое представление массива в формате [elem1, elem2, elem3, ..., elemN]
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i < items.size(); i++) {
            builder.append(items.get(i));
            if (i + 1 < items.size())
                builder.append(", ");
        }
        builder.append(']');
        return builder.toString();
    }

    @Override
    public String toFormattedString(int tabSize, int depth) {
        StringBuilder builder = new StringBuilder();
        String padding = getPaddingString(tabSize, depth);

        builder.append(padding).append("[\n");
        for (int i = 0; i < items.size(); i++) {
            builder.append(items.get(i).toFormattedString(tabSize, depth+1));
            if (i + 1 < items.size())
                builder.append(",\n");
        }
        builder.append('\n').append(padding).append(']');
        return builder.toString();
    }
}
