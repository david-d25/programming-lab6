package ru.david.room;

public class CoordinatesOutOfRoomException extends Exception {
    @Override
    public String getMessage() {
        return "Выход за пределы комнаты!";
    }
}
