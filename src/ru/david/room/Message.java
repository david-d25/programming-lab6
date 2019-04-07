package ru.david.room;

import java.io.Serializable;

/**
 * Класс, использующийся для передачи сообщений между клиентом и сервером,
 * используется для соответствия требованию о сериализации передаваемых объектов
 * и для решения проблемы с разделением команды и её объектов-аргументов
 */
public class Message<T extends Serializable> implements Serializable {
    private String message;
    private T argument;
    private boolean endFlag;

    /**
     * Создаёт сообщение с указанным текстовым запросом, объектом-аргументов и флагом окончания
     * @param message текстовый запрос
     * @param argument объект-аргумент, прикреплённый к сообщению
     * @param endFlag флаг окончания
     */
    Message(String message, T argument, boolean endFlag) {
        this.message = message;
        this.argument = argument;
        this.endFlag = endFlag;
    }

    /**
     * Создаёт сообщение с указанным текстовым запросом и объектом-аргументом
     * @param message текстовый запрос
     * @param argument объект-аргумент, прикреплённый к сообщению
     */
    Message(String message, T argument) {
        this(message, argument, false);
    }

    /**
     * Создаёт сообщение с указанным текстовым запросом и флагом окончания
     * @param message текстовый запрос
     * @param endFlag флаг окончания
     */
    Message(String message, boolean endFlag) {
        this(message, null, endFlag);
    }

    /**
     * Создаёт сообщение с указанным текстовым запросом
     * @param message текствый запрос
     */
    Message(String message) {
        this(message, null, false);
    }

    /**
     * @return текстовый запрос сообщения
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return объект-аргумент, прикреплённый к сообщению
     */
    public T getArgument() {
        return argument;
    }

    /**
     * @return true, если к сообщению прикреплён объект-аругмент
     */
    public boolean hasArgument() {
        return argument != null;
    }

    /**
     * @return true, если сообщение отмечено как последнее (если установлен флаг окончания)
     */
    public boolean hasEndFlag() {
        return endFlag;
    }
}
