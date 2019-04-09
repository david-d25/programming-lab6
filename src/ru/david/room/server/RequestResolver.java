package ru.david.room.server;

import org.xml.sax.SAXException;
import ru.david.room.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class RequestResolver implements Runnable {

    private static long maxRequestSize = 268435456;
    private static long maxLoggableRequestSize = 128;

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private CountingInputStream countingStream;
    private Hoosegow hoosegow;
    private Socket socket;
    private Logger logger;

    RequestResolver(Socket socket, Hoosegow hoosegow, Logger logger) {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            countingStream = new CountingInputStream(socket.getInputStream());
            in = new ObjectInputStream(countingStream);
            this.socket = socket;
            this.hoosegow = hoosegow;
            this.logger = logger;

            new Thread(this).start();
        } catch (IOException e) {
            logger.err("Ошибка создания решателя запроса: " + e.toString());
        }
    }

    @Override
    public void run() {
        try {
            List<Message> messages = new LinkedList<>();
            while (true) {
                Object incoming = in.readObject();

                if (countingStream.countedBytes() > maxRequestSize) {
                    sendEndMessage("Запрос слишком большой (" + Utils.optimalInfoUnit(countingStream.countedBytes()) + "), размер запроса не должен быть больше " + Utils.optimalInfoUnit(maxRequestSize));
                    return;
                }

                if (incoming instanceof Message) {
                    messages.add((Message) incoming);
                    if (((Message) incoming).hasEndFlag())
                        break;
                } else {
                    sendEndMessage("Клиент отправил данные в неверном формате");
                    return;
                }
            }

            if (messages.size() == 1) {
                Message message = messages.get(0);
                if (message.getMessage().length() <= maxLoggableRequestSize)
                    logger.log("Запрос от " + socket.getInetAddress() + ": " + message.getMessage());
                else
                    logger.log("Запрос от " + socket.getInetAddress() + ", размер запроса: " + Utils.optimalInfoUnit(message.getMessage().length()));

                processMessage(message);
            } else {
                logger.log("Запрос из " + messages.size() + " сообщений от " + socket.getInetAddress());
                for (int i = 0; i < messages.size(); i++)
                    processMessage(messages.get(i), i+1 == messages.size());
            }

        } catch (IOException e) {
            logger.err("Ошибка исполнения запроса: " + e.getMessage());
            sendEndMessage("На сервере произошла ошибка: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            sendEndMessage("Клиент отправил данные в неверном формате");
        }
    }

    /**
     * Отправляет сообщение, отмеченное как последнее
     * @param message текст сообщения
     */
    private void sendEndMessage(String message) {
        sendMessage(message, true);
    }

    /**
     * Отправляет сообщение с указанным флагом окончания
     * @param message текст сообщения
     * @param endFlag флаг окончания
     */
    private void sendMessage(String message, boolean endFlag) {
        try {
            out.writeObject(new Message(message, endFlag));
        } catch (IOException e) {
            logger.log("Ошибка отправки данных клиенту: " + e.getLocalizedMessage());
        }
    }

    /**
     * Обрабатывает сообщение, отправляемый клиенту результат будет отмечен как последний
     * @param message сообщение
     */
    private void processMessage(Message message) {
        processMessage(message, true);
    }

    /**
     * Обрабатывает сообщение
     * @param message сообщение
     * @param endFlag если он true, результат обработки отправится клиенту как последний
     */
    private void processMessage(Message message, boolean endFlag) {
        if (message == null) {
            sendMessage("Задан пустой запрос", endFlag);
            return;
        }

        switch (message.getMessage()) {
            case "info":
                sendMessage(hoosegow.getCollectionInfo(), endFlag);
                return;

            case "show":
                try {
                    for (Iterator<Creature> iterator = hoosegow.getCollection().iterator(); iterator.hasNext(); )
                        out.writeObject(new Message<>("", iterator.next(), !iterator.hasNext()));
                    if (hoosegow.getSize() == 0)
                        out.writeObject(new Message<>("", null));
                } catch (IOException e) {
                    logger.log("Ошибка исполнения запроса show: " + e.getLocalizedMessage());
                }
                return;

            case "save":
                if (!message.hasArgument()) {
                    sendMessage("Имя не указано.\n" +
                            "Введите \"help save\", чтобы узнать, как пользоваться командой", endFlag);
                    return;
                }
                try {
                    if (!(message.getArgument() instanceof String)) {
                        sendMessage("Клиент отправил запрос в неверном формате (аргумент сообщения должен быть строкой)", endFlag);
                        return;
                    }
                    PleaseWaitMessage pleaseWaitMessage = new PleaseWaitMessage(out,
                            "Ваш запрос в процессе обработки. Пожалуйста, подождите...",
                            2000);
                    HoosegowStateController.saveState(
                            hoosegow,
                            new OutputStreamWriter(new FileOutputStream((String)message.getArgument()), StandardCharsets.UTF_8)
                    );
                    pleaseWaitMessage.clear();
                    if (endFlag)
                        sendMessage("Сохранение успешно, господин. В тюряге " + hoosegow.getSize() + " существ", true);
                } catch (IOException e) {
                    sendEndMessage("Ошибка чтения/записи");
                }
                return;

            case "load":
                if (!message.hasArgument()) {
                    sendMessage("Имя не указано.\n" +
                            "Введите \"help load\", чтобы узнать, как пользоваться командой", endFlag);
                    return;
                }
                try {
                    if (!(message.getArgument() instanceof String)) {
                        sendMessage("Клиент отправил запрос в неверном формате (аргумент сообщения должен быть строкой)", endFlag);
                        return;
                    }
                    PleaseWaitMessage pleaseWaitMessage = new PleaseWaitMessage(out,
                            "Ваш запрос в процессе обработки. Пожалуйста, подождите...",
                            2000);
                    hoosegow.clear();
                    HoosegowStateController.loadState(
                            hoosegow,
                            FileLoader.getFileContent((String)message.getArgument(), false)
                    );
                    pleaseWaitMessage.clear();
                    sendMessage("Загрузка успешна! В тюряге " + hoosegow.getSize() + " существ", endFlag);
                } catch (AccessDeniedException e) {
                    sendMessage("Нет доступа для чтения", endFlag);
                } catch (FileNotFoundException e) {
                    sendMessage("Файл не найден", endFlag);
                } catch (IOException e) {
                    sendEndMessage("Ошибка чтения/записи");
                } catch (SAXException | ParserConfigurationException e) {
                    sendMessage("Ошибка обработки файла: " + e.getLocalizedMessage(), endFlag);
                } catch (HoosegowOverflowException e) {
                    sendMessage("В тюряге не осталось места, некоторые существа загрузились", endFlag);
                }
                return;

            case "import":
                if (!message.hasArgument()) {
                    sendMessage("Имя не указано.\n" +
                            "Введите \"help import\", чтобы узнать, как пользоваться командой", endFlag);
                    return;
                }
                try {
                    if (!(message.getArgument() instanceof String)) {
                        sendMessage("Клиент отправил запрос в неверном формате (аргумент сообщения должен быть строкой)", endFlag);
                        return;
                    }
                    PleaseWaitMessage pleaseWaitMessage = new PleaseWaitMessage(out,
                            "Ваш запрос в процессе обработки. Пожалуйста, подождите...",
                            2000);
                    HoosegowStateController.loadState(
                            hoosegow,
                            (String)message.getArgument()
                    );
                    pleaseWaitMessage.clear();
                    sendMessage("Загрузка успешна! В тюряге " + hoosegow.getSize() + " существ", endFlag);
                } catch (IOException e) {
                    sendEndMessage("Ошибка чтения/записи");
                } catch (SAXException | ParserConfigurationException e) {
                    sendMessage("Ошибка обработки файла: " + e.getLocalizedMessage(), endFlag);
                } catch (HoosegowOverflowException e) {
                    sendMessage("В тюряге не остмалось места, некоторые существа не загрузились", endFlag);
                }
                return;

            case "remove_last":
                if (hoosegow.getSize() == 0)
                    sendMessage("Тюряга пуста, господин", endFlag);
                sendMessage("Удалено это существо: " + hoosegow.removeLast(), endFlag);
                return;

            case "add":
                try {
                    if (!message.hasArgument()) {
                        sendMessage(helpFor(message.getMessage()), endFlag);
                        return;
                    }
                    if (!(message.getArgument() instanceof Creature)) {
                        sendMessage("Клиент отправил данные в неверном формате (аргумент должен быть сериализованным объектом)", endFlag);
                        return;
                    }
                    Creature creature = (Creature)message.getArgument();
                    hoosegow.add(creature);
                    sendMessage("Существо " + creature.getName() + " добавлено в тюрягу", endFlag);
                    return;
                } catch (HoosegowOverflowException e) {
                    sendMessage("Недостаточно места в тюряге. " +
                            "В тюрягу может поместиться не больше " + Hoosegow.getMaxCollectionElements() + " существ.\n" +
                            "Попробуйте удалить кого-то, чтобы освободить место.", endFlag);
                } catch (Exception e) {
                    sendMessage("Не получилось создать существо: " + e.getMessage(), endFlag);
                }
                return;

            case "remove_greater":
                try {
                    if (!message.hasArgument()) {
                        sendMessage(helpFor(message.getMessage()), endFlag);
                        return;
                    }
                    if (!(message.getArgument() instanceof Creature)) {
                        sendMessage("Клиент отправил данные в неверном формате (аргумент должен быть сериализованным объектом)", endFlag);
                        return;
                    }
                    sendMessage("Удалено " + hoosegow.removeGreaterThan((Creature)message.getArgument()) + " существ", endFlag);
                    return;
                } catch (Exception e) {
                    sendMessage(e.getMessage(), endFlag);
                }


            case "remove":
                try {
                    if (!message.hasArgument()) {
                        sendMessage(helpFor(message.getMessage()), endFlag);
                        return;
                    }
                    if (!(message.getArgument() instanceof Creature)) {
                        sendMessage("Клиент отправил данные в неверном формате (аргумент должен быть сериализованным объектом)", endFlag);
                        return;
                    }
                    boolean removed = hoosegow.remove((Creature)message.getArgument());
                    if (removed)
                        sendMessage("Сущепство удалено, господин", endFlag);
                    else
                        sendMessage("Господин, такого существа не нашлось в тюряге", endFlag);
                } catch (Exception e) {
                    sendMessage(e.getMessage(), endFlag);
                }
                return;

            case "?":
            case "help":
            case "хелб":
            case "хелп":
            case "хэлб":
            case "хэлп":
            case "хлеб":
            case "чоита":
            case "шоцетаке":
            case "помогите":
            case "памагити":
            case "напомощь":
                if (!message.hasArgument())
                    sendMessage(helpFor("help"), endFlag);
                else {
                    if (message.getArgument() instanceof String)
                        sendMessage(helpFor((String) message.getArgument()), endFlag);
                    else
                        sendMessage("Клент отправил данные в неверном формате (аргумент должен быть строкой)", endFlag);
                }
                return;

            default:
                sendMessage("Не могу понять команду " + message.getMessage() + ", введите help, чтобы получить помощь", endFlag);
        }
    }

    /**
     * Возвращает инструкции к команде
     * @param command команда, для которой нужна инструкция
     * @return инструкция к указанной команде
     */
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
                        "Если вы хотите добавить сразу несколько существ, то можете вместо\n" +
                        "json-объекта указать json-массив из объектов, каждый из которых будет\n" +
                        "добавлен в коллекцию.\n\n" +
                        "Например:\n" +
                        "> add {\"x\": 12, \"y\": 34}\n" +
                        "> add {\"x\": 42, \"y\": -53.1, \"width\": 12, \"name\": \"Стёпа\"}\n" +
                        "> add [ {\"x\": 12, \"y\": 34}, {\"y\": 62, \"x\": 1, \"width\": 23} ]";
            case "remove_greater":
                return  "Чтобы удалить все существа, превосходящие нужный, введите \"remove_greater\", а\n" +
                        "затем json-объект, описывающий существо, с которым будет выполняться сравнение\n" +
                        "при удалении. Для описания существа можно использовать слудеющие параметры:\n" +
                        "x - число, обязательное, X-координата существа\n" +
                        "y - число, обязательное, Y-координата существа\n" +
                        "width - число, необязательное, ширина существа\n" +
                        "height - число, необязательное, высота существа\n" +
                        "name - строка, необязательное, имя существа\n\n" +
                        "Если вы хотите удалить сразу несколько существ, то можете вместо\n" +
                        "json-объекта указать json-массив из объектов, каждое существо будет\n" +
                        "обработано по отдельности.\n\n" +
                        "Например:\n" +
                        "> remove_greater {\"x\": 32, \"y\": 7}\n" +
                        "> remove_greater {\"x\": -5, \"y\": 0.1, \"width\": 1, \"name\": \"Василий\"}\n" +
                        "> remove_greater [ {\"x\": 12, \"y\": 34}, {\"y\": 62, \"x\": 1, \"width\": 23} ]";
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
                        "Если вы хотите удалить сразу несколько существ, то можете вместо\n" +
                        "json-объекта указать json-массив из объектов, каждый из которых будет\n" +
                        "удален из коллекции.\n\n" +
                        "Например:\n" +
                        "> remove {\"x\": 5, \"y\": 1}\n" +
                        "> remove {\"x\": 65536, \"y\": 123214, \"height\": 203, \"name\": \"Пётр I\"}\n" +
                        "> remove [ {\"x\": 12, \"y\": 34}, {\"y\": 62, \"x\": 1, \"width\": 23} ]";

                default:
                    return  "Неизвестная команда " + command + "\n" +
                            "Введите \"help\", чтобы узнать, какие есть команды";
        }
    }

    /**
     * @return Максимальный размер запроса в байтах
     */
    static long getMaxRequestSize() {
        return maxRequestSize;
    }

    /**
     * Задаёт максимальный размер запроса
     * @param maxRequestSize максимальный размер запроса в байтах
     */
    static void setMaxRequestSize(long maxRequestSize) {
        RequestResolver.maxRequestSize = maxRequestSize;
    }

    /**
     * Получает максимальный размер запроса, содержимое которого отобразится в локах.
     * Если размер запроса превысит данное значение, вместо сожержимого в локах будет указан размер запроса.
     * @return Максимальный размер логгируемого содержимого запроса
     */
    static long getMaxLoggableRequestSize() {
        return maxLoggableRequestSize;
    }

    /**
     * Устанавливает максимальный размер запроса, содержимое которого отобразится в локах.
     * Если размер запроса превысит данное значение, вместо сожержимого в локах будет указан размер запроса.
     * @param maxLoggableRequestSize Максимальный размер логгируемого содержимого запроса
     */
    static void setMaxLoggableRequestSize(long maxLoggableRequestSize) {
        RequestResolver.maxLoggableRequestSize = maxLoggableRequestSize;
    }
}