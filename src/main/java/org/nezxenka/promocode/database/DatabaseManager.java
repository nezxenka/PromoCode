package org.nezxenka.promocode.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.nezxenka.promocode.PromoCode;
import org.nezxenka.promocode.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
public class DatabaseManager {

    private final PromoCode plugin;
    private final DatabaseConfig config;
    private HikariDataSource dataSource;

    public DatabaseManager(PromoCode plugin, DatabaseConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void initialize() {
        setupHikariCP();
        createTables();
    }

    private void setupHikariCP() {
        HikariConfig hikariConfig = new HikariConfig();

        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%d/%s?useSSL=false&autoReconnect=true&characterEncoding=utf8&useUnicode=true&serverTimezone=UTC",
                config.getMySQLHost(),
                config.getMySQLPort(),
                config.getMySQLDatabase()
        );

        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(config.getMySQLUser());
        hikariConfig.setPassword(config.getMySQLPassword());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
        hikariConfig.setMinimumIdle(config.getMinimumIdle());
        hikariConfig.setMaxLifetime(config.getMaxLifetime());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getIdleTimeout());
        hikariConfig.setKeepaliveTime(config.getKeepaliveTime());

        hikariConfig.setPoolName("PromoCode-Pool");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

        dataSource = new HikariDataSource(hikariConfig);
        plugin.getLogger().info("HikariCP connection pool initialized!");
    }

    private void createTables() {
        String createActivationsTable = """
                CREATE TABLE IF NOT EXISTS promo_activations (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    player_name VARCHAR(255) NOT NULL,
                    ip_address VARCHAR(255) NOT NULL,
                    promo_code VARCHAR(255) NOT NULL,
                    promo_group VARCHAR(255) NOT NULL,
                    activation_count INT NOT NULL DEFAULT 1,
                    first_activation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_activation TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_player (player_name),
                    INDEX idx_ip (ip_address),
                    INDEX idx_code (promo_code),
                    INDEX idx_group (promo_group),
                    UNIQUE KEY unique_player_code (player_name, promo_code)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;

        String createGlobalActivationsTable = """
                CREATE TABLE IF NOT EXISTS promo_global_activations (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    promo_code VARCHAR(255) NOT NULL UNIQUE,
                    activation_count INT NOT NULL DEFAULT 0,
                    last_activation TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_code (promo_code)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                """;

        try (Connection conn = getConnection()) {
            conn.createStatement().execute(createActivationsTable);
            conn.createStatement().execute(createGlobalActivationsTable);
            plugin.getLogger().info("Database tables created successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating database tables!");
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not initialized or closed!");
        }
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed!");
        }
    }

    public boolean hasPlayerActivatedGroup(String playerName, String ipAddress, String group) {
        String query = "SELECT COUNT(*) FROM promo_activations WHERE (player_name = ? OR ip_address = ?) AND promo_group = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, playerName);
            stmt.setString(2, ipAddress);
            stmt.setString(3, group);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error checking group activation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    public boolean hasPlayerActivated(String playerName, String ipAddress, String promoCode, int maxUses) {
        String query = "SELECT activation_count FROM promo_activations WHERE (player_name = ? OR ip_address = ?) AND promo_code = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, playerName);
            stmt.setString(2, ipAddress);
            stmt.setString(3, promoCode);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("activation_count");
                return count >= maxUses;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error checking promo activation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    public boolean canActivatePromo(String promoCode, int globalLimit) {
        if (globalLimit == -1) return true;
        
        String query = "SELECT activation_count FROM promo_global_activations WHERE promo_code = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, promoCode);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("activation_count");
                return count < globalLimit;
            }
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error checking global limit: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    public void activatePromo(String playerName, String ipAddress, String promoCode, String group) {
        String insertOrUpdate = """
                INSERT INTO promo_activations (player_name, ip_address, promo_code, promo_group, activation_count)
                VALUES (?, ?, ?, ?, 1)
                ON DUPLICATE KEY UPDATE activation_count = activation_count + 1, last_activation = CURRENT_TIMESTAMP
                """;
        
        String updateGlobal = """
                INSERT INTO promo_global_activations (promo_code, activation_count)
                VALUES (?, 1)
                ON DUPLICATE KEY UPDATE activation_count = activation_count + 1
                """;
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt1 = conn.prepareStatement(insertOrUpdate);
                 PreparedStatement stmt2 = conn.prepareStatement(updateGlobal)) {
                
                stmt1.setString(1, playerName);
                stmt1.setString(2, ipAddress);
                stmt1.setString(3, promoCode);
                stmt1.setString(4, group);
                stmt1.executeUpdate();
                
                stmt2.setString(1, promoCode);
                stmt2.executeUpdate();
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error activating promo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getTotalActivations() {
        String query = "SELECT SUM(activation_count) FROM promo_activations";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting total activations: " + e.getMessage());
        }
        
        return 0;
    }

    public int getUniquePlayers() {
        String query = "SELECT COUNT(DISTINCT player_name) FROM promo_activations";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting unique players: " + e.getMessage());
        }
        
        return 0;
    }
}
