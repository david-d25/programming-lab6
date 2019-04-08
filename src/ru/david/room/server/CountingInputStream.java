package ru.david.room.server;

import java.io.IOException;
import java.io.InputStream;

/**
 * Поток-счётчик, используется для вычисления объема переданных данных
 */
public class CountingInputStream extends InputStream {
    private InputStream is;
    private long counter = 0;

    CountingInputStream(InputStream is) {
        this.is = is;
    }

    /**
     * @return количество прочитанных из этого потока байтов
     */
    long countedBytes() {
        return counter;
    }

    /**
     * Сбрасывает счётчик
     */
    void resetCounter() {
        counter = 0;
    }

    @Override
    public int read() throws IOException {
        int read = is.read();
        counter++;
        return read;
    }
}
