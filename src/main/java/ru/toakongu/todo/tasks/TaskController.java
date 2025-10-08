package ru.toakongu.todo.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    public List<String> getAllTasks() {
        List<String> tasks = new ArrayList<>();
        String sql = "SELECT id, title, done FROM tasks;";
        logger.debug("SQL запрос: {}", sql);

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                boolean done = rs.getBoolean("done");
                tasks.add(id + ". " + title + " - " + (done ? "✅" : "❌"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public void addTask(Task task) {
        String sql = "INSERT INTO tasks (title) VALUES (?);";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            logger.debug("Подключились к базе данных");
            stmt.setString(1, task.getTitle());
            stmt.executeUpdate();
            logger.debug("Выполнили запрос");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteTask(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTask(int id, Task task) {
        String sql = "UPDATE tasks SET title = ?, done = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, task.getTitle());
            stmt.setBoolean(2, task.getDone());
            stmt.setInt(3, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean toggleTaskStatus(int id) {
        String sql = "UPDATE tasks SET done = NOT done WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteAllTasks() {
        String sql = "DELETE FROM tasks";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
