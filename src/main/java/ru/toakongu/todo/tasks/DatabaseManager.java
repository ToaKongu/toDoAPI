package ru.toakongu.todo.tasks;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.toakongu.todo.config.Config;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static final HikariDataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    static {

        try {
            HikariConfig config = new HikariConfig();
            Config DBconfig = new Config("application.properties");
            config.setJdbcUrl(DBconfig.get("db.url"));
            config.setUsername(DBconfig.get("db.user"));
            config.setPassword(DBconfig.get("db.password"));
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30_000);
            config.setMaxLifetime(29 * 60_000);
            config.setIdleTimeout(5 * 60_000);
            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации пула соединений: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        logger.debug("Попытка получить соединение из пула...");
        Connection conn = dataSource.getConnection();
        logger.info("Соединение успешно получено");
        return conn;
    }

    public static void close() {
        dataSource.close();
    }
}
