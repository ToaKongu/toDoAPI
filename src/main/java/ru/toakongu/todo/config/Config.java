package ru.toakongu.todo.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private final Properties props = new Properties();

    public Config(String filename) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (input == null) {
                throw new IllegalArgumentException("Файл конфигурации не найден: " + filename);
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке конфигурации: " + filename, e);
        }
    }

    public String get(String key) {
        return props.getProperty(key);
    }
}
