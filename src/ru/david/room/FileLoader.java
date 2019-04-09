package ru.david.room;

import java.io.*;
import java.util.Arrays;

import static ru.david.room.Utils.progressBar;

public class FileLoader {
    /**
     * Читает файл
     * @param filename имя файла
     * @param showProgressBar если true, в System.out будет отправляться красивая полоса прогресса
     * @return содержимое в виде строки
     * @throws IOException если что-то пойдет не так
     */
    public static String getFileContent(String filename, boolean showProgressBar) throws IOException {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filename));
             InputStreamReader reader = new InputStreamReader(inputStream)
        ) {
            long fileSize = new File(filename).length();
            StringBuilder fileContent = new StringBuilder();

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
            if (showProgressBar)
                loadingProgress.start();

            int current;
            while ((current = reader.read()) != -1)
                fileContent.append((char)current);

            if (showProgressBar)
                loadingProgress.interrupt();

            return fileContent.toString();
        }
    }

    /**
     * Читает файл, регулярно записывает текущий прогресс в System.out
     * @param filename имя файла
     * @return содержимое в виде строки
     * @throws IOException если что-то пойдет не так
     */
    public static String getFileContent(String filename) throws IOException {
        return getFileContent(filename, true);
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
