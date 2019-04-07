package ru.david.room;

import java.io.*;
import java.util.Arrays;

import static ru.david.room.Utils.progressBar;

public class FileLoader {
    /**
     * Читает файл, регулярно записывает прогресс в System.out
     * @param filename имя файла
     * @return содержимое в виде строки
     * @throws IOException если что-то пойдет не так
     */
    public static String getFileContent(String filename) throws IOException {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filename));
             InputStreamReader reader = new InputStreamReader(inputStream)
        ) {
            StringBuilder fileContent = new StringBuilder();
            long fileSize = new File(filename).length();
            Thread loadingProgress = new Thread(() -> {
                try {
                    Thread.sleep(250);
                    System.out.println("Читаем ваш файл...");
                    while (true) {
                        System.out.print(progressBar(1.0f * fileContent.length() / fileSize));
                        Thread.sleep(50);
                    }
                } catch (InterruptedException ignored) {}
            });
            loadingProgress.start();
            int current;
            do {
                current = reader.read();
                if (current != -1)
                    fileContent.append((char)current);
            } while (current != -1);
            loadingProgress.interrupt();

            return fileContent.toString();
        }
    }

    /**
     * Читает файл, регулярно записывает прогресс в System.out
     * @param filename имя файла
     * @return содержимое в виде последовательности байтов
     * @throws IOException если что-то пойдет не так
     */
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
