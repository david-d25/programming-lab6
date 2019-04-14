package ru.david.room.server;

import ru.david.room.FileLoader;
import ru.david.room.Utils;
import ru.david.room.json.JSONEntity;
import ru.david.room.json.JSONNumber;
import ru.david.room.json.JSONObject;
import ru.david.room.json.JSONParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static String outLogFile = "out.log";
    private static String errLogFile = "err.log";
    private static int port = 8080;

    private static ServerSocket serverSocket;
    private static Logger logger;

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {}

        if (args.length > 0)
            loadConfig(args[0]);
        else
            System.out.println("Файл настроек не указан\n");

        initLogger();

        try {
            serverSocket = new ServerSocket(port);
            logger.log("Сервер запущен и слушает порт " + port + "...");
        } catch (IOException e) {
            logger.err("Ошибка создания серверного сокета (" + e.getLocalizedMessage() + "), приложение будет остановлено.");
            System.exit(1);
        }

        Hoosegow hoosegow = new Hoosegow();

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new RequestResolver(clientSocket, hoosegow, logger);

            } catch (IOException e) {
                logger.err("Connection error: " + e.getMessage());
            }
        }
    }

    /**
     * Инициализирует логгер для сервера
     */
    private static void initLogger() {
        try {
            logger = new Logger(
                    new PrintStream(new TeeOutputStream(System.out, new FileOutputStream(outLogFile)), true, "UTF-8"),
                    new PrintStream(new TeeOutputStream(System.err, new FileOutputStream(errLogFile)), true, "UTF-8")
            );
        } catch (IOException e) {
            System.err.println("Ошибка записи логов: " + e.getLocalizedMessage());
            System.exit(1);
        }
    }

    /**
     * Загружает настройки сервера
     * @param configFilename файл настроек
     */
    private static void loadConfig(String configFilename) {
        System.out.println("Загрузка настроек...");
        try {
            String configContent = FileLoader.getFileContent(configFilename);

            JSONObject object = JSONParser.parse(configContent).toObject(
                    "Файл должен содержать json-объект"
            );

            JSONEntity maxRequestSizeEntity = object.getItem("max_request_size");
            if (maxRequestSizeEntity != null) {
                RequestResolver.setMaxRequestSize(
                        (int)maxRequestSizeEntity.toNumber(
                        "Макс. размер запроса (max_request_size) должен быть числом, но это " + maxRequestSizeEntity.getTypeName()
                        ).getValue()
                );
                System.out.println("Задан макс. размер запроса: " + Utils.optimalInfoUnit(RequestResolver.getMaxRequestSize()));
            } else
                System.out.println("Используется макс. размер запроса по умолчанию: " + Utils.optimalInfoUnit(RequestResolver.getMaxRequestSize()));

            JSONEntity maxLoggableRequestSize = object.getItem("max_loggable_request_size");
            if (maxLoggableRequestSize != null) {
                RequestResolver.setMaxLoggableRequestSize(
                        (long)maxLoggableRequestSize.toNumber(
                                "Макс. размер логгируемого запроса (max_loggable_request_size) должен быть числом, но это " + maxLoggableRequestSize.getTypeName()
                        ).getValue()
                );
                System.out.println("Задан макс. размер логгируемого запроса: " + Utils.optimalInfoUnit(RequestResolver.getMaxLoggableRequestSize()));
            } else
                System.out.println("Используется макс. размер логгируемого запроса по умолчанию: " + Utils.optimalInfoUnit(RequestResolver.getMaxLoggableRequestSize()));

            JSONEntity maxCollectionElements = object.getItem("max_collection_elements");
            if (maxCollectionElements != null) {
                Hoosegow.setMaxCollectionElements(
                        (int)maxCollectionElements.toNumber(
                                "Макс. количество существ (max_collection_elements) должно быть числом, но это " + maxCollectionElements.getTypeName()
                        ).getValue()
                );
                System.out.println("Задано макс. количество существ: " + Hoosegow.getMaxCollectionElements());
            } else
                System.out.println("Используется макс. количество существ по умолчанию: " + Hoosegow.getMaxCollectionElements());

            JSONEntity outLogFileEntity = object.getItem("out_log_file");
            if (outLogFileEntity != null) {
                outLogFile = outLogFileEntity.toString(
                        "Имя файла стандартного вывоа (out_log_file) должно быть строкой, но это " + outLogFileEntity.getTypeName()
                        ).getContent();
                System.out.println("Задано имя файла стандартного вывода: " + outLogFile);
            } else
                System.out.println("Используется имя файла стандартного вывода по умолчанию: " + outLogFile);

            JSONEntity errLogFileEntity = object.getItem("err_log_file");
            if (errLogFileEntity != null) {
                errLogFile = errLogFileEntity.toString(
                        "Имя файла вывода ошибок (err_log_file) должно быть строкой, но это " + errLogFileEntity.getTypeName()
                ).getContent();
                System.out.println("Задано имя файла вывода ошибок: " + errLogFile);
            } else
                System.out.println("Используется имя файла вывода ошибок по умолчанию: " + errLogFile);

            JSONEntity portEntity = object.getItem("port");
            if (portEntity != null) {
                JSONNumber number = portEntity.toNumber(
                        "Порт сервера должен быть числом, но это " + portEntity.getTypeName()
                );

                double port = number.getValue();
                if (port < 0 || port > 65535)
                    System.err.println("Порт сервера должен быть целым числом от 0 до 65535");
                else {
                    port = (int)port;
                    System.out.println("Задан порт сервера: " + (int)port);
                }
            } else
                System.out.println("Используется порт сервера по умолчанию: " + port);

            System.out.println();

        } catch (FileNotFoundException e) {
            System.out.println("Файл настроек не найден, для задания настроек используйте файл " + configFilename);
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла настроек: " + e.getLocalizedMessage());
        } catch (Exception e) {
            System.err.print("Настройки не загружены: ");
            System.err.println(e.getMessage());
        }
    }
}