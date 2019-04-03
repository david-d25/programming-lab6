package ru.david.room.server;

import org.xml.sax.SAXException;
import ru.david.room.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.concurrent.atomic.AtomicLong;

import static ru.david.room.CreatureFactory.makeCreatureFromJSON;

class RequestResolver implements Runnable {

    private static final long MAX_REQUEST_SIZE = 268435456;
    private static final long MAX_LOGGABLE_REQUEST_SIZE = 128;

    private PrintWriter clientOut;
    private BufferedReader clientIn;
    private Hoosegow hoosegow;
    private Socket clientSocket;
    private Logger logger;

    private volatile AtomicLong loadingProgress = new AtomicLong();

    RequestResolver(Socket clientSocket, Hoosegow hoosegow, Logger logger) {
        try {
            clientOut = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.clientSocket = clientSocket;
            this.hoosegow = hoosegow;
            this.logger = logger;

            new Thread(this).start();
        } catch (IOException e) {
            logger.err(e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            StringBuilder builder = new StringBuilder();
            long size = Long.parseLong(clientIn.readLine());
            if (size > MAX_REQUEST_SIZE) {
                sendAndClose("Запрос слишком большой (" + optimalInfoUnit(size) + "), размер запроса не должен быть больше " + optimalInfoUnit(MAX_REQUEST_SIZE));
                return;
            }

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    while (loadingProgress.longValue() != size) {
                        Thread.sleep(1500);
                        clientOut.println("Отправляем запрос... (" + 100 * loadingProgress.longValue() / size + "%)");
                        clientOut.flush();
                    }
                } catch (InterruptedException ignored) {}
            }).start();

            for (; loadingProgress.longValue() < size; loadingProgress.incrementAndGet())
                builder.append((char) clientIn.read());

            String request = builder.toString();

            if (request.length() <= MAX_LOGGABLE_REQUEST_SIZE)
                logger.log("Запрос от " + clientSocket.getInetAddress() + ": " + request);
            else
                logger.log("Запрос от " + clientSocket.getInetAddress() + ", размер запроса: " + optimalInfoUnit(request.length()));

            sendAndClose(processRequest(request));

        } catch (IOException e) {
            logger.err("Ошибка исполнения запроса: " + e.getMessage());
        } catch (NumberFormatException e) {
            sendAndClose("Клиент отправил данные в неверном формате");
        }
    }

    private void sendAndClose(String content) {
        clientOut.println(content);
        clientOut.close();
    }

    private String processRequest(String request) {
        if (request == null)
            return "Задан пустой запрос";

        request = request.trim().replaceAll("\\s{2,}", " ");
        Command command = new Command(request);

        StringBuilder result = new StringBuilder();

        switch (command.name) {
            case "info":
                return hoosegow.getCollectionInfo();

            case "show":
                if (hoosegow.getSize() == 0)
                    return "Тюряга пуста, господин";
                result.append("Существа в тюряге:\n");
                for (Creature creature : hoosegow.getCollection())
                    result.append(creature.toString()).append('\n');
                return result.toString();

            case "save":
                if (command.argument == null)
                    return  "Имя не указано.\n" +
                            "Введите \"help save\", чтобы узнать, как пользоваться командой";
                try {
                    HoosegowStateController.saveState(
                            hoosegow,
                            new OutputStreamWriter(new FileOutputStream(command.argument), StandardCharsets.UTF_8)
                    );
                    return "Сохранение успешно, господин";
                } catch (IOException e) {
                    return "Ошибка чтения/записи";
                }

            case "load":
                if (command.argument == null)
                    return  "Имя не указано.\n" +
                            "Введите \"help load\", чтобы узнать, как пользоваться командой";
                try {
                    hoosegow.clear();
                    HoosegowStateController.loadState(
                            hoosegow,
                            FileLoader.getFileContent(command.argument)
                    );
                    return "Загрузка успешна! В тюряге " + hoosegow.getSize() + " существ";
                } catch (AccessDeniedException e) {
                    return "Нет доступа для чтения";
                } catch (FileNotFoundException e) {
                    return "Файл не найден";
                } catch (IOException e) {
                    return "Ошибка чтения/записи";
                } catch (SAXException | ParserConfigurationException e) {
                    return "Ошибка обработки файла: " + e.getLocalizedMessage();
                }

            case "import":
                if (command.argument == null)
                    return  "Имя не указано.\n" +
                            "Введите \"help import\", чтобы узнать, как пользоваться командой";
                try {
                    HoosegowStateController.loadState(
                            hoosegow,
                            command.argument
                    );
                    return "Загрузка успешна! В тюряге " + hoosegow.getSize() + " существ";
                } catch (IOException e) {
                    return "Ошибка чтения/записи";
                } catch (SAXException | ParserConfigurationException e) {
                    return "Ошибка обработки файла: " + e.getLocalizedMessage();
                }

            case "remove_last":
                if (hoosegow.getSize() == 0)
                    return "Тюряга пуста, господин";
                return "Удалено это существо: " + hoosegow.removeLast();

            case "add":
                try {
                    if (command.argument == null)
                        return helpFor(command.name);
                    hoosegow.add(makeCreatureFromJSON(command.argument));
                    return "Существо добавлено в тюрягу";
                } catch (Exception e) {
                    return "Не получилось создать существо: " + e.getMessage();
                }

            case "remove_greater":
                try {
                    Creature creature = makeCreatureFromJSON(command.argument);
                    return "Удалено " + hoosegow.removeGreaterThan(creature) + " существ";
                } catch (Exception e) {
                    return e.getMessage();
                }


            case "remove":
                try {
                    if (command.argument == null)
                        return helpFor(command.name);
                    boolean removed = hoosegow.remove(makeCreatureFromJSON(command.argument));
                    if (removed)
                        return "Сущепство удалено, господин";
                    return "Господин, такого существа не нашлось в тюряге";
                } catch (Exception e) {
                    return e.getMessage();
                }

            case "?":
            case "help":
            case "хелб":
            case "хелп":
            case "хэлб":
            case "хэлп":
            case "помогите":
            case "памагити":
            case "напомощь":
                if (command.argument == null)
                    return helpFor("help");
                else
                    return helpFor(command.argument);

            default:
                return "Не могу понять команду " + command.name + ", введите help, чтобы получить помощь";
        }
    }

    private static String helpFor(String command) {
        switch (command) {
            case "help":
                return  "Похоже, ты тут впервые, боец!\n" +
                        "Вот команды, которыми можно пользоваться:\n\n" +
                        "exit - свалить отсюда\n" +
                        "info - информация о коллекции\n" +
                        "show - состояние тюряги\n" +
                        "save {файл} - сохранить в файл\n" +
                        "remove_last - удалить последний элемент\n" +
                        "add {elem} - добавить элемент, обязательные поля: x и y, дополнительно width, height, name\n" +
                        "remove_greater {elem} - удалить всех, кто круче указанного существа\n" +
                        "multiline - включить/выключить ввод в несколько строк\n" +
                        "import {file} - добавить данные из файла клиента в коллекцию\n" +
                        "load {file} - загрузить состояние коллекции из файла сервера\n" +
                        "save {file} - сохранить состояние коллекции в файл сервера\n" +
                        "remove {elem} - удалить элемент\n" +
                        "help {command} - инструкция к команде\n" +
                        "help, ?, памагити, хелб, хэлп, и т.п. - показать этот текст";
            case "exit":
                return "Введите \"exit\", чтобы выйти";
            case "info":
                return "Введите \"info\", чтобы узнать о количестве существ в тюряге, времени создания и типе используемой коллекции";
            case "show":
                return "Выводит список существ в тюряге";
            case "save {файл}":
                return  "Введите \"save\", а затем имя файла, чтобы сохранить в него состояние тюряги.\n" +
                        "Файл будет содержать дату создания, а также список существ в формате xml\n\n" +
                        "Например:\n" +
                        "> save saved_state.xml";
            case "remove_last":
                return "Введите \"remove_last\", чтобы удалить последнее существо из тюряги";
            case "add":
                return  "Чтобы создать существо, введите \"add\", а затем json-объект,\n" +
                        "описывающий существо. Для описания существа можно использовать\n" +
                        "следующие параметры:\n" +
                        "x - число, обязательное, X-координата существа\n" +
                        "y - число, обязательное, Y-координата существа\n" +
                        "width - число, необязательное, ширина существа\n" +
                        "height - число, необязательное, высота существа\n" +
                        "name - строка, необязательное, имя существа\n\n" +
                        "Например:\n" +
                        "> add {\"x\": 12, \"y\": 34}\n" +
                        "> add {\"x\": 42, \"y\": -53.1, \"width\": 12, \"name\": \"Стёпа\"}";
            case "remove_greater":
                return  "Чтобы удалить все существа, превосходящие нужный, введите \"remove_greater\", а\n" +
                        "затем json-объект, описывающий существо, с которым будет выполняться сравнение\n" +
                        "при удалении. Для описания существа можно использовать слудеющие параметры:\n" +
                        "x - число, обязательное, X-координата существа\n" +
                        "y - число, обязательное, Y-координата существа\n" +
                        "width - число, необязательное, ширина существа\n" +
                        "height - число, необязательное, высота существа\n" +
                        "name - строка, необязательное, имя существа\n\n" +
                        "Например:\n" +
                        "> add {\"x\": 32, \"y\": 7}\n" +
                        "> add {\"x\": -5, \"y\": 0.1, \"width\": 1, \"name\": \"Василий\"}";
            case "multiline":
                return  "Переключает режим многострочного ввода. Если многострочный ввод выключен, введите \"multiline\",\n" +
                        "чтобы включить его. После того, как вы включили многострочный режим, ваши команды будут\n" +
                        "отдельяться друг от друга знаком ';'.\n" +
                        "Чтобы выключить многострочный ввод, введите \"multiline;\". Обратите внимание, что в режиме\n" +
                        "многострочного ввода также нужен знак ';' после команды отключения многострочного ввода.";
            case "import":
                return  "Иногда бывает так, что нужно передать содержимое всего файла на сервер, где этого файла нет.\n" +
                        "Используйте команду \"import\", чтобы сделать это. После имени команды укажите файл,\n" +
                        "содержимое которого передастся на сервер. Обратите внимание, что, если в файле указана дата\n" +
                        "создания коллекции, текущая дата будет перезаписана. Кроме того, import не удаляет\n" +
                        "предыдущих существ из коллекции. Файл должен хранить данные в формате xml\n\n" +
                        "Например:\n" +
                        "> import client_file.xml";
            case "load":
                return  "Эта команда идентична команде import (введите \"help import\", чтобы узнать о ней), за\n" +
                        "исключением нескольких деталей:\n" +
                        "- load используется для загрузки файла сервера\n" +
                        "- load полностью перезаписывает состояние коллекции";
            case "save":
                return  "Эта команда сохраняет состояние коллекции в файл сервера в формате xml.\n\n" +
                        "Например:\n" +
                        "> save server_file.xml";
            case "remove":
                return  "Чтобы удалить существо, введите \"remove\", а затем json-объект,\n" +
                        "описывающий существо. Для описания существа можно использовать\n" +
                        "следующие параметры:\n" +
                        "x - число, X-координата существа\n" +
                        "y - число, Y-координата существа\n" +
                        "width - число, ширина существа\n" +
                        "height - число, высота существа\n" +
                        "name - строка, имя существа\n\n" +
                        "Например:\n" +
                        "> remove {\"x\": 5, \"y\": 1}\n" +
                        "> remove {\"x\": 65536, \"y\": 123214, \"height\": 203, \"name\": \"Пётр I\"}";

                default:
                    return  "Неизвестная команда " + command + "\n" +
                            "Введите \"help\", чтобы узнать, какие есть команды";
        }
    }

    private static String optimalInfoUnit(long bytes) {
        String[] units = {"байт", "КиБ", "МиБ", "ГиБ", "ТиБ"};
        long result = bytes;
        int divided = 0;
        while (result > 1024) {
            result /= 1024;
            divided++;
        }
        if (divided >= units.length)
            divided = units.length-1;
        return result + " " + units[divided];
    }
}