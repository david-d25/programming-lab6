package ru.david.room.json;

/**
 * Инкапсулирует json-строку
 */
public class JSONString extends JSONEntity {
    private String content;

    {
        type = JSONType.STRING;
    }

    /**
     * Создаёт пустую json-строку
     */
    public JSONString() {
        content = "";
    }

    /**
     * Создаёт json-строку с указанным содержимым
     * @param content содержимое
     */
    public JSONString(String content) {
        this.content = content;
    }

    /**
     * @return содержимое в виде строки
     */
    public String getContent() {
        return content;
    }

    /**
     * Устанавливает содержимое json-строки
     * @param content содержимое в виде строки
     */
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return '"' + content + '"';
    }

    @Override
    protected String toFormattedString(int tabSize, int depth) {
        return getPaddingString(tabSize, depth) + toString();
    }
}
