package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.Statement;

public class V2__expand_job_description_length extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE jobs MODIFY COLUMN description VARCHAR(2000) NOT NULL");
        }
    }
}
