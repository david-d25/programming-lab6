package ru.david.room;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
}
