package ru.david.room;

import ru.david.room.json.JSONEntity;
import ru.david.room.json.JSONNumber;
import ru.david.room.json.JSONObject;
import ru.david.room.json.JSONParser;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.file.AccessDeniedException;
import java.util.LinkedList;
import java.util.List;

public class Client {
    private static boolean multiline = false;

    private static String serverAddress = "localhost";
    private static int serverPort = 8080;

    private static Command previousCommand = null;

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {}

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        if (args.length > 0)
            loadConfig(args[0]);
        else
            System.out.println("Файл настроек не указан\n");

        System.out.println("Добро пожаловать, мой господин!");

        System.out.println("Введите команду:\n");

        while (true) {
            try {
                System.out.print("> ");
                String query = multiline ? getMultilineCommand(reader) : reader.readLine();
                if (query == null) return;
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
        query = query.trim().replaceAll("\\s{2,}", " ");
        Command command = new Command(query);

        if (command.name.isEmpty())
            return "Введите команду";

        if (previousCommand == null)
            if (command.name.equals("again"))
                return "again не должен быть первой командой. Введите help again, чтобы узнать больше";
            else
                previousCommand = command;
        else if (command.name.equals("again"))
            command = previousCommand;
        else
            previousCommand = command;


        switch (command.name) {
            case "exit":
                System.exit(0);

            case "show":
                return doShow();

            case "address":
                if (command.argument == null)
                    return "Адрес сервера: " + serverAddress +
                            "\nЧтобы изменить адрес, введите его после команды";
                serverAddress = command.argument;
                return "Установлен адрес " + serverAddress;
            case "port":
                if (command.argument == null)
                    return "Порт: " + serverPort +
                            "\nЧтобы изменить порт, введите его после команды";
                int newPort;
                try {
                    newPort = Integer.parseInt(command.argument);
                } catch (NumberFormatException e) {
                    newPort = -1;
                }
                if (newPort < 1 || newPort > 65535)
                    return "Порт должен быть числом от 1 до 65535";
                serverPort = newPort;
                return "Установлен порт " + serverPort;

            case "add":
            case "remove":
            case "remove_greater":
                return doWithCreatureArgument(command.name, command.argument);

            case "multiline":
                multiline = !multiline;
                return "Многострочные команды " + (multiline ? "включены. Используйте ';' для завешения команды." : "выключены");

            case "import":
                if (command.argument != null)
                    return doImport(command.argument);

                default:
                    return sendCommand(command.name, command.argument);
        }
    }

    /**
     * Выполняет команду, аргумент которой является json-представлением экземпляра класса Creature
     * @param command имя команжы
     * @param jsonArgument аргумент команды
     * @return результат выполнения
     */
    private static String doWithCreatureArgument(String command, String jsonArgument) {
        try {
            if (jsonArgument == null)
                return sendCommand(command, null);
            else {
                Creature[] creatures;
                try {
                    creatures = CreatureFactory.generate(jsonArgument);
                } catch (Exception e) {
                    return e.getMessage();
                }
                try (SocketChannel channel = SocketChannel.open()) {
                    channel.connect(new InetSocketAddress(serverAddress, serverPort));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);

                    for (int i = 0; i < creatures.length; i++) {
                        // Making a Message instance and writing it to ByteArrayOutputStream
                        Message message = new Message<>(command, creatures[i], i + 1 == creatures.length);
                        oos.writeObject(message);
                    }

                    // Sending message using channel
                    ByteBuffer sendingBuffer = ByteBuffer.allocate(baos.size());
                    sendingBuffer.put(baos.toByteArray());
                    sendingBuffer.flip();
                    channel.write(sendingBuffer);

                    // Getting response
                    ObjectInputStream ois = new ObjectInputStream(channel.socket().getInputStream());
                    while (true) {
                        Message incoming = (Message) ois.readObject();
                        System.out.println(incoming.getMessage());
                        if (incoming.hasEndFlag())
                            break;
                    }
                } catch (UnresolvedAddressException e) {
                    return "Не удалось определить адрес сервера. Воспользуйтесь командой address, чтобы изменить адрес.";
                } catch (UnknownHostException e) {
                    return "Ошибка подключения к серверу: неизвестный хост. Воспользуйтесь командой address, чтобы изменить адрес";
                } catch (SecurityException e) {
                    return "Нет разрешения на подключение, проверьте свои настройки безопасности";
                } catch (ConnectException e) {
                    return "Нет соединения с сервером. Введите again, чтобы попытаться ещё раз, или измените адрес (команда address)";
                } catch (IOException e) {
                    return "Ошибка ввода-вывода: " + e;
                } catch (ClassNotFoundException e) {
                    return "Ошибка: клиент отправил данные в недоступном для клиента формате (" + e.getLocalizedMessage() + ")";
                }
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "";
    }

    /**
     * Выполняет команлу show, вывод отправляет в System.out
     * @return пустую строку или сообщение (возможно, об ошибке)
     */
    private static String doShow() {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(serverAddress, serverPort));

            // Creating a Message instance and writing it to ByteArrayOutputStream
            Message message = new Message("show", true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new ObjectOutputStream(baos).writeObject(message);

            // Sending the message to server using channel
            ByteBuffer byteBuffer = ByteBuffer.allocate(baos.size());
            byteBuffer.put(baos.toByteArray()).flip();
            channel.write(byteBuffer);

            ObjectInputStream ois = new ObjectInputStream(channel.socket().getInputStream());
            List<Creature> result = new LinkedList<>();

            // Reading the response
            while (!channel.socket().isClosed()) {
                Message incoming = (Message)ois.readObject();
                if (!incoming.hasArgument())
                    break;
                if (incoming.getArgument() instanceof Creature)
                    result.add((Creature)incoming.getArgument());
                else
                    return "Сервер вернул данные в неверном формате";
                if (incoming.hasEndFlag())
                    break;
            }

            // Writing the response to System.out
            if (result.size() > 0) {
                System.out.println("Существа с тюряге");
                result.forEach(System.out::println);
            } else
                return "Тюряга пустая, господин";
            return "";
        } catch (UnresolvedAddressException e) {
            return "Не удалось определить адрес сервера. Воспользуйтесь командой address, чтобы изменить адрес.";
        } catch (UnknownHostException e) {
            return "Ошибка подключения к серверу: неизвестный хост. Воспользуйтесь командой address, чтобы изменить адрес";
        } catch (SecurityException e) {
            return "Нет разрешения на подключение, проверьте свои настройки безопасности";
        } catch (ConnectException e) {
            return "Нет соединения с сервером. Введите again, чтобы попытаться ещё раз, или измените адрес (команда address)";
        } catch (EOFException e) {
            return "";
        } catch (IOException e) {
            return "Ошибка ввода-вывода: " + e;
        } catch (ClassNotFoundException e) {
            return "Сервер отпавил класс, который не может прочитать клиент";
        }
    }

    /**
     * Формирует команду import и отправляет на сервер
     * @param filename имя файла, содержимое которого будет отправлено
     * @return пустую строку или сообщение об ошибке, если есть
     */
    private static String doImport(String filename) {
        try {
            String content = FileLoader.getFileContent(filename);
            return sendCommand("import", content);
        } catch (FileNotFoundException e) {
            return "Нет такого файла";
        } catch (AccessDeniedException e) {
            return "Нет доступа к файлу";
        } catch (IOException e) {
            return "Ошибка ввода-вывода: " + e.getMessage();
        } catch (Exception e) {
            return "Неизвестная ошибка: " + e.toString();
        }
    }

    /**
     * Отправляет команду на сервер, результат отправляет в System.out,
     * использует каналы согласно условию задания
     * @param name команда, которую нужно отправить
     * @param argument аргумент команды
     * @return пустую строку или сообщение об ошибке, если есть
     */
    private static String sendCommand(String name, Serializable argument) {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(serverAddress, serverPort));

            // Making a Message instance and writing it to ByteArrayOutputStream
            Message message = new Message<>(name, argument, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(message);

            // Sending message using channel
            ByteBuffer sendingBuffer = ByteBuffer.allocate(baos.size());
            sendingBuffer.put(baos.toByteArray());
            sendingBuffer.flip();
            new Thread(() -> {
                try {
                    channel.write(sendingBuffer);
                } catch (IOException ignored) {
                }
            }).start();

            // Getting Message instance from response
            ObjectInputStream ois = new ObjectInputStream(channel.socket().getInputStream());
            while (true) {
                Message incoming = (Message) ois.readObject();
                System.out.println(incoming.getMessage());
                if (incoming.hasEndFlag())
                    break;
            }
            return "";
        } catch (UnresolvedAddressException e) {
            return "Не удалось определить адрес сервера. Воспользуйтесь командой address, чтобы изменить адрес.";
        } catch (UnknownHostException e) {
            return "Ошибка подключения к серверу: неизвестный хост. Воспользуйтесь командой address, чтобы изменить адрес";
        } catch (SecurityException e) {
            return "Нет разрешения на подключение, проверьте свои настройки безопасности";
        } catch (ConnectException e) {
            return "Нет соединения с сервером. Введите again, чтобы попытаться ещё раз, или измените адрес (команда address)";
        } catch (IOException e) {
            return "Ошибка ввода-вывода, обработка запроса прервана";
        } catch (ClassNotFoundException e) {
            return "Ошибка: клиент отправил данные в недоступном для клиента формате (" + e.getLocalizedMessage() + ")";
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
     * @param configFilename файл конфигурации
     */
    private static void loadConfig(String configFilename) {
        System.out.println("Загрузка настроек...");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(configFilename));
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
                if (port < 1 || port > 65535)
                    System.err.println("Порт должен быть целым числом от 1 до 65535");
                else {
                    serverPort = (int)port;
                    System.out.println("Задан порт: " + serverPort);
                }
            } else
                System.out.println("Используется порт по умолчанию: " + serverPort);

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