package ru.david.room.json;

import java.security.PrivilegedActionException;

/**
 * Абстрация для любой json-сущеости (объект, массив или любое значение)
 * @see JSONArray
 * @see JSONObject
 * @see JSONNumber
 * @see JSONBoolean
 * @see JSONString
 */
public class JSONEntity {
    protected JSONType type;

    /**
     * @return {@link JSONEntity#toFormattedString(int)} с отступом 2
     */
    public final String toFormattedString() {
        return toFormattedString(2);
    }

    /**
     * @param tabSize размер отступа
     * @return Тот же, что {@link JSONEntity#toString()}, но с отступами и переносами для читаемости
     */
    public final String toFormattedString(int tabSize) {
        return toFormattedString(tabSize, 0);
    }

    protected String toFormattedString(int tabSize, int depth) {
        return toString();
    }

    protected String getPaddingString(int tabSize, int depth) {
        StringBuilder padding = new StringBuilder();
        for (int i = 0; i < tabSize*depth; i++)
            padding.append(' ');
        return padding.toString();
    }

    public JSONType getType() {
        return type;
    }

    public String getTypeName() {
        return getType().getName();
    }

    public boolean isObject() {
        return type == JSONType.OBJECT;
    }

    public boolean isArray() {
        return type == JSONType.ARRAY;
    }

    public boolean isBoolean() {
        return type == JSONType.BOOLEAN;
    }

    public boolean isNumber() {
        return type == JSONType.NUMBER;
    }

    public boolean isString() {
        return type == JSONType.STRING;
    }

    public int toInt(Exception onError) throws Exception {
        return (int)toNumber(onError).getValue();
    }

    public JSONObject toObject() throws Exception {
        return toObject(new IllegalStateException("Невозможно создать объект из сущности с типом " + getTypeName()));
    }

    public JSONObject toObject(Exception onError) throws Exception {
        if (isObject())
            return (JSONObject)this;
        throw onError;
    }

    public JSONObject toObject(String onError) throws Exception {
        return toObject(new IllegalStateException(onError));
    }

    public JSONArray toArray() throws Exception {
        return toArray(new IllegalStateException("Невозможно создать массив из сущности с типом " + getTypeName()));
    }

    public JSONArray toArray(Exception onError) throws Exception {
        if (isArray())
            return (JSONArray)this;
        throw onError;
    }

    public JSONArray toArray(String onError) throws Exception {
        return toArray(new IllegalStateException(onError));
    }

    public JSONBoolean toBoolean() throws Exception {
        return toBoolean(new IllegalStateException("Невозможно создать булев тип из сущности с типом " + getTypeName()));
    }
    public JSONBoolean toBoolean(Exception onError) throws Exception {
        if (isBoolean())
            return (JSONBoolean) this;
        throw onError;
    }

    public JSONBoolean toBoolean(String onError) throws Exception {
        return toBoolean(new IllegalStateException(onError));
    }

    public JSONNumber toNumber() throws Exception {
        return toNumber(new IllegalStateException("Невозможно создать число из сущености с типом " + getTypeName()));
    }
    public JSONNumber toNumber(Exception onError) throws Exception {
        if (isNumber())
            return (JSONNumber) this;
        throw onError;
    }

    public JSONNumber toNumber(String onError) throws Exception {
        return toNumber(new IllegalStateException(onError));
    }

    public JSONString toString(Exception onError) throws Exception {
        if (isString())
            return (JSONString) this;
        throw onError;
    }

    public JSONString toString(String onError) throws Exception {
        return toString(new IllegalStateException(onError));
    }
}