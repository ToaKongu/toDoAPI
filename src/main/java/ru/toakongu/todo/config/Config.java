package ru.toakongu.todo.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private final Properties props = new Properties();

    public Config(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки файла конфигурации: " + filePath, e);
        }
    }

    public String get(String key) {
        return props.getProperty(key);
    }
}
