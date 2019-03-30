package ru.david.room.server;

public class CoordinatesOutOfRoomException extends Exception {
    @Override
    public String getMessage() {
        return "Выход за пределы комнаты!";
    }
}
