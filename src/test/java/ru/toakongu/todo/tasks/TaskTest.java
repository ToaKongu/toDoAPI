package ru.toakongu.todo.tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskTest {
    @Test
    void testTaskTitle() {
        Task task = new Task("Учить тесты");
        assertEquals("Учить тесты", task.getTitle());
    }
}
