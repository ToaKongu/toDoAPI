package ru.toakongu.todo.server;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.toakongu.todo.tasks.Task;
import ru.toakongu.todo.tasks.TaskController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpHandler {
    private final Socket clientSocket;
    private final TaskController taskController;
    private final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    public HttpHandler(Socket clientSocket, TaskController taskController) {
        this.clientSocket = clientSocket;
        this.taskController = taskController;
    }

    public void handle() {
        try (
                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream()
        ) {
            String requestLine = readLine(input);
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }
            logger.debug(">>> "+ requestLine);

            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String path = parts[1];

            Map<String, String> headers = getHeaders(input);

            int contentLength = 0;
            if (headers.containsKey("Content-Length")) {
                contentLength = Integer.parseInt(headers.get("Content-Length"));
            }

            String body = getBody(contentLength, input);

            logger.debug(">>> method = " + method);
            logger.debug(">>> path = " + path);
            logger.debug(">>> body = " + body);

            String response = responseCreator(method, path, body);

            output.write(response.getBytes(StandardCharsets.UTF_8));
            output.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignore) {}
        }
    }

    private String responseCreator(String method, String path, String body) {
        String response;

        if (method.equals("GET") && path.equals("/tasks")) {
            String json = gson.toJson(taskController.getAllTasks());
            response = httpResponse(200, "OK", json, "application/json");

        } else if (method.equals("POST") && path.equals("/tasks")) {
            Task task = gson.fromJson(body, Task.class);
            taskController.addTask(task);
            response = httpResponse(201, "Created", "Задача добавлена");

        } else if (method.equals("DELETE") && path.equals("/tasks")) {
            taskController.deleteAllTasks();
            response = httpResponse(200, "OK", "Все задачи удалены");

        } else if (method.equals("DELETE") && path.startsWith("/tasks/")) {
            int idToDelete = Integer.parseInt(path.substring("/tasks/".length()));
            boolean isDeleted = taskController.deleteTask(idToDelete);
            if (isDeleted) {
                response = httpResponse(200, "OK", "Задача удалена");
            } else {
                response = httpResponse(404, "Not Found", "Задача не найдена");
            }

        } else if (method.equals("PUT") && path.startsWith("/tasks/")) {
            int idToUpdate = Integer.parseInt(path.substring("/tasks/".length()));
            Task task = gson.fromJson(body, Task.class);
            boolean isUpdated = taskController.updateTask(idToUpdate, task);
            if (isUpdated) {
                response = httpResponse(200, "OK", "Задача обновлена");
            } else {
                response = httpResponse(404, "Not Found", "Задача не найдена");
            }

        } else if (method.equals("PATCH") && path.startsWith("/tasks/") && path.endsWith("/toggle")) {
            logger.debug("Получаем id задачи из body");
            int taskId = Integer.parseInt(path.split("/")[2]);
            logger.debug("Меняем id задачи в базе данных");
            boolean isUpdated = taskController.toggleTaskStatus(taskId);
            if (isUpdated) {
                response = httpResponse(200, "OK", "Статус задачи обновлен");
            } else {
                response = httpResponse(404, "Not Found", "Задача не найдена");
            }

        } else {
            response = httpResponse(404, "Not Found", "Страница не найдена");
        }
        return response;
    }

    private String httpResponse(int code, String status, String content) {
        return httpResponse(code, status, content, "text/plain; charset=UTF-8");
    }

    private String httpResponse(int code, String status, String content, String contentType) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return "HTTP/1.1 " + code + " " + status + "\r\n" +
                "Content-Type: " + contentType + "; charset=UTF-8\r\n" +
                "Content-Length: " + bytes.length + "\r\n" +
                "\r\n" +
                content;
    }

    private String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        boolean seenCR = false;
        while ((b = in.read()) != -1) {
            if (b == '\r') {
                seenCR = true;
                continue; // ждём LF
            }
            if (b == '\n') {
                break;
            }
            if (seenCR) {
                // ранее был CR, но сейчас не LF — это редкий сценарий, просто добавим CR
                baos.write('\r');
                seenCR = false;
            }
            baos.write(b);
        }
        if (b == -1 && baos.size() == 0) return null;
        return baos.toString(StandardCharsets.UTF_8);
    }

    private Map<String, String> getHeaders(InputStream in) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = readLine(in)) != null && !line.isEmpty()) {
            String[] splitLine = line.split(":", 2);
            String key = splitLine[0].trim();
            String value = splitLine[1].trim();
            headers.put(key, value);
        }
        return headers;
    }

    private String getBody(int contentLength, InputStream input) {
        if (contentLength <= 0) {
            return "";
        }
        try {
            byte[] bodyBytes = new byte[contentLength];
            int bytesRead = 0;
            while (bytesRead < contentLength) {
                int read = input.read(bodyBytes, bytesRead, contentLength - bytesRead);
                if (read == -1) break; // клиент закрыл соединение
                bytesRead += read;
            }
            return new String(bodyBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading input stream: " + e.getMessage());
            return "";
        }
    }
}
