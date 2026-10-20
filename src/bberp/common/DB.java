package bberp.common;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * HikariCP pool for the dbemployee database. Settings come from system
 * properties, then environment variables, then local erp.properties.
 */
public final class DB {
    private DB() {}

    public static HikariDataSource create() {
        HikariConfig c = new HikariConfig();
        c.setJdbcUrl(setting("DB_URL", "jdbc:mysql://localhost:3306/dbemployee"));
        c.setUsername(setting("DB_USER", "root"));
        c.setPassword(setting("DB_PASSWORD", "root"));
        c.setMaximumPoolSize(5);
        c.setMinimumIdle(1);
        c.setConnectionTimeout(5000);
        return new HikariDataSource(c);
    }

    private static String setting(String key, String fallback) {
        String v = System.getProperty(key);
        if (v == null) v = System.getenv(key);
        return v == null || v.isEmpty() ? fallback : v;
    }
}
