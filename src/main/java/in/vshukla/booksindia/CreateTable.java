package in.vshukla.booksindia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Class to create a table.
 * This class uses the Singleton DbConnection object to execute the table creation SQL.
 * Created by venkatesh on 8/4/17.
 */
public class CreateTable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTable.class);

    public static void main(String[] args) {
            String sql = "CREATE TABLE IF NOT EXISTS book (" +
                    "    id int primary key not null," +
                    "    isbn varchar(1024) not null," +
                    "    title varchar(1024)," +
                    "    gr_id varchar(1024)," +
                    "    review text" +
                    ")";

            DbConnection connection = DbConnection.getInstance();
            try {
                connection.executeSql(sql);
                LOGGER.info("Table creation successful.");
            } catch (SQLException e) {
                LOGGER.error("Error creating the table.", e);
            }
    }
}
