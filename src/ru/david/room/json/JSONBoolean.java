package ru.david.room.json;

/**
 * Инкапсулирует json-булев тип
 */
public class JSONBoolean extends JSONEntity{
    private boolean value;

    {
        type = JSONType.BOOLEAN;
    }

    /**
     * Создаёт json-булев тип с указанным значением
     * @param value содержимое
     */
    JSONBoolean(boolean value) {
        this.value = value;
    }

    /**
     * Создаёт json-булев по строковому представлению. Булев тип равен true только если переданная строка равна "true"
     * @param value строковое представление булевого типа
     */
    JSONBoolean(String value) {
        this.value = value.equals("true");
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    @Override
    protected String toFormattedString(int tabSize, int depth) {
        return getPaddingString(tabSize, depth) + toString();
    }
}