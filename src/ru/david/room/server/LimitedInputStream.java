package ru.david.room.server;

import java.io.IOException;
import java.io.InputStream;

/**
 * Поток ввода, ограничивающий количество информации, проходящее через него.
 * Если превысить установленный порог, выкинется {@link LimitedInputStream}.
 */
public class LimitedInputStream extends InputStream {
    private InputStream is;
    private int count;
    private int limit;

    /**
     * Создаёт ограничивающий поток ввода
     * @param is поток-источник
     * @param limit максимальное количество байт, которое пропустит поток
     */
    LimitedInputStream(InputStream is, int limit) {
        this.is = is;
        this.limit = limit;
    }

    @Override
    public int read() throws IOException {
        int result = is.read();
        if (result != -1)
            count++;
        if (count > limit)
            throw new LimitAchievedException();
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = is.read(b, off, len);
        if (result != -1)
            count += result;
        if (count > limit)
            throw new LimitAchievedException();
        return result;
    }
}
