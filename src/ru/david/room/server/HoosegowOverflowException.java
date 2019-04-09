package ru.david.room.server;

public class HoosegowOverflowException extends RuntimeException {
    HoosegowOverflowException() {
        super("Тюряга переполнена");
    }
}
