package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class V1__baseline_job_portal_schema extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        boolean mysql = isMysql(connection);

        createUsersTableIfMissing(connection);
        createJobsTableIfMissing(connection);
        createApplicationsTableIfMissing(connection);

        reconcileUsersTable(connection, mysql);
        reconcileJobsTable(connection, mysql);
        reconcileApplicationsTable(connection, mysql);

        createUniqueIndexIfMissing(connection, "users", "uk_users_email", "email");
        createUniqueIndexIfMissing(connection, "applications", "uk_application_user_job", "user_id, job_id");

        createForeignKeyIfMissing(connection, "applications", "fk_applications_user", "user_id", "users", "id");
        createForeignKeyIfMissing(connection, "applications", "fk_applications_job", "job_id", "jobs", "id");
    }

    private boolean isMysql(Connection connection) throws SQLException {
        return connection.getMetaData()
                .getDatabaseProductName()
                .toLowerCase(Locale.ROOT)
                .contains("mysql");
    }

    private void createUsersTableIfMissing(Connection connection) throws SQLException {
        if (tableExists(connection, "users")) {
            return;
        }
        execute(connection, """
                CREATE TABLE users (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  name VARCHAR(255) NOT NULL,
                  email VARCHAR(255) NOT NULL,
                  password VARCHAR(255),
                  auth_provider VARCHAR(255) NOT NULL,
                  role VARCHAR(255) NOT NULL,
                  created_at TIMESTAMP(6) NOT NULL,
                  updated_at TIMESTAMP(6) NOT NULL
                )
                """);
    }

    private void createJobsTableIfMissing(Connection connection) throws SQLException {
        if (tableExists(connection, "jobs")) {
            return;
        }
        execute(connection, """
                CREATE TABLE jobs (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  title VARCHAR(255) NOT NULL,
                  description VARCHAR(255) NOT NULL,
                  company VARCHAR(255) NOT NULL,
                  posted_date DATE NOT NULL,
                  created_at TIMESTAMP(6) NOT NULL,
                  updated_at TIMESTAMP(6) NOT NULL
                )
                """);
    }

    private void createApplicationsTableIfMissing(Connection connection) throws SQLException {
        if (tableExists(connection, "applications")) {
            return;
        }
        execute(connection, """
                CREATE TABLE applications (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  user_id BIGINT NOT NULL,
                  job_id BIGINT NOT NULL,
                  status VARCHAR(255) NOT NULL,
                  created_at TIMESTAMP(6) NOT NULL,
                  updated_at TIMESTAMP(6) NOT NULL
                )
                """);
    }

    private void reconcileUsersTable(Connection connection, boolean mysql) throws SQLException {
        addColumnIfMissing(connection, "users", "created_at", timestampColumnDefinition());
        addColumnIfMissing(connection, "users", "updated_at", timestampColumnDefinition());

        copyColumnIfPresent(connection, "users", "created_at", "createdAt");
        copyColumnIfPresent(connection, "users", "updated_at", "updatedAt");

        updateNullTimestamps(connection, "users", "created_at", currentTimestamp());
        updateNullTimestamps(connection, "users", "updated_at", currentTimestamp());

        enforceTimestampNotNull(connection, "users", "created_at", mysql);
        enforceTimestampNotNull(connection, "users", "updated_at", mysql);
    }

    private void reconcileJobsTable(Connection connection, boolean mysql) throws SQLException {
        addColumnIfMissing(connection, "jobs", "posted_date", "DATE");
        addColumnIfMissing(connection, "jobs", "created_at", timestampColumnDefinition());
        addColumnIfMissing(connection, "jobs", "updated_at", timestampColumnDefinition());

        copyColumnIfPresent(connection, "jobs", "created_at", "createdAt");
        copyColumnIfPresent(connection, "jobs", "updated_at", "updatedAt");

        execute(connection, """
                UPDATE jobs
                SET posted_date = COALESCE(posted_date, CURRENT_DATE)
                WHERE posted_date IS NULL
                """);
        execute(connection, """
                UPDATE jobs
                SET created_at = COALESCE(created_at, %s, %s)
                WHERE created_at IS NULL
                """.formatted(postedDateTimestampExpression(mysql), currentTimestamp()));
        updateNullTimestamps(connection, "jobs", "updated_at", currentTimestamp());

        enforceDateNotNull(connection, "jobs", "posted_date", mysql);
        enforceTimestampNotNull(connection, "jobs", "created_at", mysql);
        enforceTimestampNotNull(connection, "jobs", "updated_at", mysql);
    }

    private void reconcileApplicationsTable(Connection connection, boolean mysql) throws SQLException {
        addColumnIfMissing(connection, "applications", "status", "VARCHAR(255)");
        addColumnIfMissing(connection, "applications", "created_at", timestampColumnDefinition());
        addColumnIfMissing(connection, "applications", "updated_at", timestampColumnDefinition());

        copyColumnIfPresent(connection, "applications", "created_at", "applied_at");
        copyColumnIfPresent(connection, "applications", "created_at", "appliedAt");
        copyColumnIfPresent(connection, "applications", "created_at", "createdAt");
        copyColumnIfPresent(connection, "applications", "updated_at", "updatedAt");

        execute(connection, """
                UPDATE applications
                SET status = COALESCE(status, 'APPLIED')
                WHERE status IS NULL
                """);
        updateNullTimestamps(connection, "applications", "created_at", currentTimestamp());
        updateNullTimestamps(connection, "applications", "updated_at", currentTimestamp());

        enforceVarcharNotNull(connection, "applications", "status", mysql);
        enforceTimestampNotNull(connection, "applications", "created_at", mysql);
        enforceTimestampNotNull(connection, "applications", "updated_at", mysql);
    }

    private void addColumnIfMissing(Connection connection, String tableName, String columnName, String definition) throws SQLException {
        if (columnExists(connection, tableName, columnName)) {
            return;
        }
        execute(connection, "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
    }

    private void copyColumnIfPresent(Connection connection, String tableName, String targetColumn, String sourceColumn) throws SQLException {
        if (!columnExists(connection, tableName, sourceColumn) || !columnExists(connection, tableName, targetColumn)) {
            return;
        }
        execute(connection, """
                UPDATE %s
                SET %s = COALESCE(%s, %s)
                WHERE %s IS NULL
                """.formatted(tableName, targetColumn, targetColumn, sourceColumn, targetColumn));
    }

    private void updateNullTimestamps(Connection connection, String tableName, String columnName, String fallbackExpression) throws SQLException {
        execute(connection, """
                UPDATE %s
                SET %s = COALESCE(%s, %s)
                WHERE %s IS NULL
                """.formatted(tableName, columnName, columnName, fallbackExpression, columnName));
    }

    private void enforceTimestampNotNull(Connection connection, String tableName, String columnName, boolean mysql) throws SQLException {
        if (mysql) {
            execute(connection, "ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " TIMESTAMP(6) NOT NULL");
        } else {
            execute(connection, "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " TIMESTAMP(6) NOT NULL");
        }
    }

    private void enforceDateNotNull(Connection connection, String tableName, String columnName, boolean mysql) throws SQLException {
        if (mysql) {
            execute(connection, "ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " DATE NOT NULL");
        } else {
            execute(connection, "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " DATE NOT NULL");
        }
    }

    private void enforceVarcharNotNull(Connection connection, String tableName, String columnName, boolean mysql) throws SQLException {
        if (mysql) {
            execute(connection, "ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " VARCHAR(255) NOT NULL");
        } else {
            execute(connection, "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " VARCHAR(255) NOT NULL");
        }
    }

    private void createUniqueIndexIfMissing(Connection connection, String tableName, String indexName, String columns) throws SQLException {
        if (indexExists(connection, tableName, indexName)) {
            return;
        }
        execute(connection, "CREATE UNIQUE INDEX " + indexName + " ON " + tableName + " (" + columns + ")");
    }

    private void createForeignKeyIfMissing(Connection connection,
                                           String tableName,
                                           String fkName,
                                           String columnName,
                                           String referenceTable,
                                           String referenceColumn) throws SQLException {
        if (foreignKeyExists(connection, tableName, fkName)) {
            return;
        }
        execute(connection, "ALTER TABLE " + tableName
                + " ADD CONSTRAINT " + fkName
                + " FOREIGN KEY (" + columnName + ") REFERENCES " + referenceTable + " (" + referenceColumn + ")");
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        for (String pattern : tablePatterns(tableName)) {
            try (ResultSet rs = metaData.getTables(connection.getCatalog(), currentSchema(connection), pattern, new String[]{"TABLE"})) {
                while (rs.next()) {
                    if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        for (String pattern : tablePatterns(tableName)) {
            try (ResultSet rs = metaData.getColumns(connection.getCatalog(), currentSchema(connection), pattern, null)) {
                while (rs.next()) {
                    if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))
                            && columnName.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean indexExists(Connection connection, String tableName, String indexName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        for (String pattern : tablePatterns(tableName)) {
            try (ResultSet rs = metaData.getIndexInfo(connection.getCatalog(), currentSchema(connection), pattern, false, false)) {
                while (rs.next()) {
                    String currentTable = rs.getString("TABLE_NAME");
                    String existing = rs.getString("INDEX_NAME");
                    if (currentTable != null
                            && tableName.equalsIgnoreCase(currentTable)
                            && existing != null
                            && indexName.equalsIgnoreCase(existing)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean foreignKeyExists(Connection connection, String tableName, String foreignKeyName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        for (String pattern : tablePatterns(tableName)) {
            try (ResultSet rs = metaData.getImportedKeys(connection.getCatalog(), currentSchema(connection), pattern)) {
                while (rs.next()) {
                    String currentTable = rs.getString("FKTABLE_NAME");
                    String existing = rs.getString("FK_NAME");
                    if (currentTable != null
                            && tableName.equalsIgnoreCase(currentTable)
                            && existing != null
                            && foreignKeyName.equalsIgnoreCase(existing)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String currentTimestamp() {
        return "CURRENT_TIMESTAMP(6)";
    }

    private String postedDateTimestampExpression(boolean mysql) {
        return mysql ? "CAST(posted_date AS DATETIME(6))" : "CAST(posted_date AS TIMESTAMP)";
    }

    private String currentSchema(Connection connection) throws SQLException {
        String schema = connection.getSchema();
        return schema == null || schema.isBlank() ? null : schema;
    }

    private String[] tablePatterns(String tableName) {
        return new String[]{tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)};
    }

    private String timestampColumnDefinition() {
        return "TIMESTAMP(6)";
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
