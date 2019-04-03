package ru.david.room;

public class HoosegowOverflowException extends RuntimeException {
    HoosegowOverflowException() {
        super("Тюряга переполнена");
    }
}
