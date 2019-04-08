package ru.david.room.server;

import ru.david.room.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;

class PleaseWaitMessage {
    private Thread thread;
    PleaseWaitMessage(ObjectOutputStream writer, String message, long delay) {
        thread = new Thread(() -> {
            try {
                Thread.sleep(delay);
                writer.writeObject(new Message(message));
            } catch (InterruptedException | IOException ignored) {}
        });
        thread.start();
    }

    void clear() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }
}
