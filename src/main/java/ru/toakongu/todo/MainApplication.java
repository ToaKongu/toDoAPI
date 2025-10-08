package ru.toakongu.todo;

import ru.toakongu.todo.server.HttpServer;
import ru.toakongu.todo.tasks.DatabaseManager;

public class MainApplication {
    public static void main(String[] args) {
        HttpServer server = new HttpServer(8080);
        server.start();

        // При завершении программы — корректно закрываем ресурсы
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Выключаю сервер...");
            DatabaseManager.close();
            server.shutdownExecutor(); // добавим метод ниже
        }));
    }
}
