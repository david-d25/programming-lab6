package ru.david.room;

import java.io.*;
import java.util.Arrays;

public class FileLoader {
    public static String getFileContent(String filename) throws IOException {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filename));
             InputStreamReader reader = new InputStreamReader(inputStream)
        ) {
            StringBuilder fileContent = new StringBuilder();
            int current;
            do {
                current = reader.read();
                if (current != -1)
                    fileContent.append((char)current);
            } while (current != -1);

            return fileContent.toString();
        }
    }

    public static byte[] getFileBytes(String filename) throws IOException {
        try (InputStream inputStream = new FileInputStream(filename)) {
            File file = new File(filename);
            byte[] bytes = new byte[(int)file.length()];
            int read = inputStream.read(bytes);
            if (read < bytes.length)
                bytes = Arrays.copyOf(bytes, read);

            return bytes;
        }
    }
}
