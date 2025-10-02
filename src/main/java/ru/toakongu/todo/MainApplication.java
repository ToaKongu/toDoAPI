package ru.toakongu.todo;

import ru.toakongu.todo.server.HttpServer;

public class MainApplication {
    public static void main(String[] args) {
        HttpServer server = new HttpServer(8080);
        server.start();
    }
}
