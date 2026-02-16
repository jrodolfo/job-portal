package net.jrodolfo.jobportal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

@Component
public class ApplicationSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ApplicationSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public ApplicationSchemaInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            if (!tableExists(connection, "applications")) {
                return;
            }

            String product = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
            boolean mysql = product.contains("mysql");
            reconcileCreatedAtColumn(connection, mysql);
            reconcileColumn(connection, "updated_at", "updatedAt", mysql);

            String nowExpression = mysql ? "NOW(6)" : "CURRENT_TIMESTAMP";
            jdbcTemplate.execute("UPDATE applications SET created_at = COALESCE(created_at, " + nowExpression + ")");
            jdbcTemplate.execute("UPDATE applications SET updated_at = COALESCE(updated_at, " + nowExpression + ")");

            if (mysql) {
                jdbcTemplate.execute("ALTER TABLE applications MODIFY COLUMN created_at DATETIME(6) NOT NULL");
                jdbcTemplate.execute("ALTER TABLE applications MODIFY COLUMN updated_at DATETIME(6) NOT NULL");
            }
        } catch (Exception ex) {
            log.error("Failed to initialize applications schema compatibility columns", ex);
        }
    }

    private void reconcileCreatedAtColumn(Connection connection, boolean mysql) throws SQLException {
        boolean hasCreatedSnake = columnExists(connection, "applications", "created_at");
        if (hasCreatedSnake) {
            return;
        }

        boolean hasAppliedSnake = columnExists(connection, "applications", "applied_at");
        boolean hasAppliedCamel = columnExists(connection, "applications", "appliedAt");

        if (mysql && hasAppliedSnake) {
            jdbcTemplate.execute("ALTER TABLE applications CHANGE COLUMN applied_at created_at DATETIME(6) NULL");
            return;
        }

        if (mysql && hasAppliedCamel) {
            jdbcTemplate.execute("ALTER TABLE applications CHANGE COLUMN appliedAt created_at DATETIME(6) NULL");
            return;
        }

        String addCreatedColumn = mysql
                ? "ALTER TABLE applications ADD COLUMN created_at DATETIME(6) NULL"
                : "ALTER TABLE applications ADD COLUMN created_at TIMESTAMP NULL";
        jdbcTemplate.execute(addCreatedColumn);

        if (hasAppliedSnake) {
            jdbcTemplate.execute("UPDATE applications SET created_at = COALESCE(created_at, applied_at)");
        } else if (hasAppliedCamel) {
            jdbcTemplate.execute("UPDATE applications SET created_at = COALESCE(created_at, appliedAt)");
        }
    }

    private void reconcileColumn(Connection connection, String snakeCaseColumn, String camelCaseColumn, boolean mysql) throws SQLException {
        boolean hasSnake = columnExists(connection, "applications", snakeCaseColumn);
        boolean hasCamel = columnExists(connection, "applications", camelCaseColumn);

        if (hasSnake) {
            return;
        }

        if (mysql && hasCamel) {
            String sql = "ALTER TABLE applications CHANGE COLUMN " + camelCaseColumn + " " + snakeCaseColumn + " DATETIME(6) NULL";
            jdbcTemplate.execute(sql);
            return;
        }

        String sql = mysql
                ? "ALTER TABLE applications ADD COLUMN " + snakeCaseColumn + " DATETIME(6) NULL"
                : "ALTER TABLE applications ADD COLUMN " + snakeCaseColumn + " TIMESTAMP NULL";
        jdbcTemplate.execute(sql);
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(connection.getCatalog(), null, tableName, null)) {
            while (rs.next()) {
                if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, tableName, null)) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
                    return true;
                }
            }
            return false;
        }
    }
}
