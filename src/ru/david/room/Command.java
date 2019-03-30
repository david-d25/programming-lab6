package ru.david.room;

public class Command {
    public String name;
    public String argument;

    public Command (String command) {
        int spaceIndex = command.indexOf(' ');
        if (spaceIndex == -1)
            name = command;
        else {
            name = command.substring(0, spaceIndex);
            argument = command.substring(spaceIndex + 1);
        }
    }
}
