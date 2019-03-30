package ru.david.room.json;

/**
 * Инкапсулирует json-число
 */
public class JSONNumber extends JSONEntity {
    private double value;

    {
        type = JSONType.NUMBER;
    }

    /**
     * Создаёт json-число по указанному значению
     * @param value значение числа
     */
    JSONNumber(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    /**
     * @return строковое представление без нулей в дробной части, если число целое
     */
    @Override
    public String toString() {
        if (value == (long)value)
            return String.format("%d", (long)value);
        else
            return String.format("%s", value);
    }

    @Override
    protected String toFormattedString(int tabSize, int depth) {
        return getPaddingString(tabSize, depth) + toString();
    }
}
