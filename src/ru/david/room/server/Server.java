package ru.david.room.server;

import ru.david.room.Hoosegow;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Server {
    private static final String OUT_LOG_FILE = "out.log";
    private static final String ERR_LOG_FILE = "err.log";
    private static final int PORT = 8080;

    private static ServerSocket serverSocket;
    private static Logger logger;

    public static void main(String[] args) {
        initLogger();

        try {
            serverSocket = new ServerSocket(PORT);
            logger.log("Сервер запущен и слушает порт " + PORT + "...");
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

    private static void initLogger() {
        try {
            logger = new Logger(
                    new PrintStream(new TeeOutputStream(System.out, new FileOutputStream(OUT_LOG_FILE))),
                    new PrintStream(new TeeOutputStream(System.err, new FileOutputStream(ERR_LOG_FILE)))
            );
        } catch (IOException e) {
            System.err.println("Ошибка записи логов: " + e.getLocalizedMessage());
            System.exit(1);
        }
    }
}