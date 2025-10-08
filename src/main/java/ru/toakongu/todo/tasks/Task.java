package ru.toakongu.todo.tasks;

public class Task {
    private final String title;
    private final boolean done = false;

    public Task(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean getDone() { return done; }

}
