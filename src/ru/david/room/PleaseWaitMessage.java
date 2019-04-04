package ru.david.room;

import java.io.PrintWriter;

public class PleaseWaitMessage {
    private Thread thread;
    public PleaseWaitMessage(PrintWriter writer, String message, long delay) {
        thread = new Thread(() -> {
            try {
                Thread.sleep(delay);
                writer.println(message);
            } catch (InterruptedException ignored) {}
        });
        thread.start();
    }

    public void clear() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }
}
