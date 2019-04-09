package ru.david.room.server;

class HoosegowOverflowException extends RuntimeException {
    HoosegowOverflowException() {
        super("Тюряга переполнена");
    }
}
