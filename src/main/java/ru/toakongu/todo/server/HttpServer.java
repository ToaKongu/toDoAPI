package ru.toakongu.todo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.toakongu.todo.tasks.TaskController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpServer {
    private final int port;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Сервер запущен на порту " + port);

            TaskController taskController = new TaskController();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Новое подключение: " + clientSocket);

                executor.submit(() -> {
                    try {
                        HttpHandler handler = new HttpHandler(clientSocket, taskController);
                        handler.handle();
                    } catch (Exception e) {
                        logger.info("Error handling connection: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        try { clientSocket.close(); } catch (IOException e) {}
                    }
                });


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdownExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
