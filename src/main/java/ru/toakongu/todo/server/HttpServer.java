package ru.toakongu.todo.server;

import ru.toakongu.todo.tasks.TaskController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private final int port;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            TaskController taskController = new TaskController();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новое подключение: " + clientSocket);

                executor.submit(() -> {
                    try {
                        HttpHandler handler = new HttpHandler(clientSocket, taskController);
                        handler.handle();
                    } catch (Exception e) {
                        System.out.println("Error handling connection: " + e.getMessage());
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
}
