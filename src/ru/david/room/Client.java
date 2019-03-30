package ru.david.room;

import ru.david.room.json.JSONEntity;
import ru.david.room.json.JSONNumber;
import ru.david.room.json.JSONObject;
import ru.david.room.json.JSONParser;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.AccessDeniedException;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Client {
    private static final String CONFIG_FILENAME = "client-config.json";

    private static boolean multiline = false;

    private static String serverAddress = "localhost";
    private static int serverPort = 0;

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {}

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Добро пожаловать, мой господин!");

        loadConfig();

        if (args.length > 0)
            System.out.println(doImport(args[0]));

        System.out.println("Введите команду:\n");

        while (true) {
            try {
                System.out.print("> ");
                String query = multiline ? getMultilineCommand(reader) : reader.readLine();
                String response = processCommand(query);
                System.out.println(response);
            } catch (IOException e) {
                System.out.println("Не удалось прочитать стандартный поток вввода: " + e.getMessage());
            }
        }
    }

    /**
     * Выполняет первичную обработку команды. Может быть использован для команд,
     * выполняемых на клиенте без участия сервера
     * @param query команда пользователя
     * @return результат операции для вывода на экран
     */
    private static String processCommand(String query) {
        if (query == null)
            System.exit(0);

        query = query.trim().replaceAll("\\s{2,}", " ");
        Command command = new Command(query);

        if (command.name.isEmpty())
            return "Введите команду";

        switch (command.name) {
            case "exit":
                System.exit(0);

            case "multiline":
                multiline = !multiline;
                return "Многострочные команды " + (multiline ? "включены. Используйте ';' для завешения команды." : "выключены");

            case "import":
                if (command.argument != null)
                    return doImport(command.argument);

                default:
                    return sendCommand(query);
        }
    }

    /**
     * Формирует команду import и отправляет на сервер
     * @param filename имя файла, содержимое которого будет отправлено
     * @return результат операции для вывода на экран
     */
    private static String doImport(String filename) {
        try {
            String content = FileLoader.getFileContent(filename);
            System.out.println(content.length());
            return sendCommand("import " + content);
        } catch (AccessDeniedException e) {
            return "Нет доступа для чтения";
        } catch (FileNotFoundException e) {
            return "Файл не найден";
        } catch (IOException e) {
            return "Ошибка чтения/записи: " + e.getLocalizedMessage();
        }
    }

    /**
     * Отправляет команду на сервер
     * @param command команда, которую нужно отправить
     * @return результат операции для вывода на экран
     */
    private static String sendCommand(String command) {
        try (Socket socket = new Socket(serverAddress, serverPort)) {
            InputStreamReader in = new InputStreamReader(socket.getInputStream());
            OutputStream out = socket.getOutputStream();
            out.write(command.getBytes());
            StringBuilder builder = new StringBuilder();
            while (!socket.isClosed()) {
                int current = in.read();
                if (current == -1)
                    break;
                builder.append((char) current);
            }
            return builder.toString();
        } catch (UnknownHostException e) {
            return "Ошибка подключения к серверу: неизвестный хост";
        } catch (IOException e) {
            return "Ошибка подключения: " + e.getLocalizedMessage();
        }
    }

    /**
     * Вощвращает слелующую команду пользователя. Предназначен для многострочного ввода.
     * @param reader поток, из которого будет читаться команда
     * @return введённая пользователем команда
     * @throws IOException если что-то пойдёт не так
     */
    private static String getMultilineCommand(BufferedReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        char current;
        boolean inString = false;
        do {
            current = (char)reader.read();
            if (current != ';' || inString)
                builder.append(current);
            if (current == '"')
                inString = !inString;
        } while (current != ';' || inString);
        return builder.toString();
    }

    /**
     * Выполняет загрузку конфигурации клиента (адрес и порт сервера)
     */
    private static void loadConfig() {
        System.out.println("Загрузка настроек...");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILENAME));
            StringBuilder builder = new StringBuilder();

            while (reader.ready())
                builder.append((char)reader.read());

            if (builder.length() == 0) {
                System.out.println("Файл настроек пуст");
                return;
            }

            JSONObject object = JSONParser.parse(builder.toString()).toObject(
                    "Файл должен содержать json-объект"
            );

            JSONEntity addressEntity = object.getItem("address");
            if (addressEntity != null) {
                serverAddress = addressEntity.toString(
                        "Адрес должен быть строкой, но это " + addressEntity.getTypeName()
                ).getContent();
                System.out.println("Задан адрес сервера: " + serverAddress);
            } else
                System.out.println("Используется адрес сервера по умолчанию: " + serverAddress);

            JSONEntity portEntity = object.getItem("port");
            if (portEntity != null) {
                JSONNumber number = portEntity.toNumber(
                        "Порт должен быть числом, но это " + portEntity.getTypeName()
                );

                double port = number.getValue();
                if (port < 0 || port > 65535)
                    System.err.println("Порт должен быть целым числом от 0 до 65535");
                else {
                    serverPort = (int)port;
                    System.out.println("Задан порт: " + serverPort);
                }
            } else
                System.out.println("Используется порт по умолчанию: " + serverPort);

            System.out.println();

        } catch (FileNotFoundException e) {
            System.out.println("Файл настроек не найден, для задания настроек используйте файл " + CONFIG_FILENAME);
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла настроек: " + e.getLocalizedMessage());
        } catch (Exception e) {
            System.err.print("Настройки не загружены: ");
            System.err.println(e.getMessage());
        }
    }
}