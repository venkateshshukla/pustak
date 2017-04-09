package in.vshukla.booksindia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This is a class following singleton pattern.
 * It would be responsible for holding the DbConnection and executing the sql commands against it.
 *
 * Created by venkatesh on 9/4/17.
 */
public class DbConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbConnection.class);

    private Connection connection;

    private DbConnection() {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("DB: Unable to find sql driver.", e);
            throw new AppException("Unable to find sql driver", e);
        }

        String url = AppUtils.getProperty(AppConstants.PROP_DB_URL);
        if (url == null) {
            String user = System.getenv(AppConstants.PROP_USER);
            url = String.format(AppConstants.DEFAULT_DB_URL, user);
        }

        String user = AppUtils.getProperty(AppConstants.PROP_DB_USR);
        String pwd = AppUtils.getProperty(AppConstants.PROP_DB_PWD);

        LOGGER.debug("DB: Attempting connection to URL {} with USER {} and PWD {}", url, user, pwd);
        try {
            connection = DriverManager.getConnection(url, user, pwd);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            LOGGER.error("DB: Unable to open SQL Connection", e);
            throw new AppException("Unable to open SQL Connection.", e);
        }
        LOGGER.info("DB: Connection successful.");
    }

    /**
     * Holder Class holds the singleton DbConnection object.
     */
    private static class DbConnectionHolder {
        private static final DbConnection INSTANCE = new DbConnection();
    }

    /**
     * Get the Database Connection instance.
     * @return Singleton DB instance.
     */
    public static DbConnection getInstance() {
        return DbConnectionHolder.INSTANCE;
    }

    /**
     * Execute the given SQL statement and commit.
     * @param sql   The SQL to execute.
     * @throws SQLException In case of any errors while execution.
     */
    public void executeSql(String sql) throws SQLException {
        executeSql(sql, true);
    }

    /**
     * Utilizing the open DB Connection, execute the given SQL.
     * If needed, execute a commit.
     *
     * @param sql       Given SQL statement.
     * @param doCommit  Should the changes be committed?
     * @throws SQLException In case of any errors while execution.
     */
    public void executeSql(String sql, boolean doCommit) throws SQLException {
        if (connection == null) {
            LOGGER.error("DB: Uninitialized DB Connection.");
            throw new AppException("Uninitialized DB Connection.");
        }

        if (sql == null || sql.trim().isEmpty()) {
            LOGGER.warn("DB : Cannot execute null or empty SQL.");
            throw new IllegalArgumentException("Null or empty SQL.");
        }

        LOGGER.info("DB : Executing the SQL : {}", sql);

        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();

        if (doCommit) {
            LOGGER.info("DB : Committing the changes.");
            connection.commit();
        }
    }

    /**
     * Close the DB connection if open.
     */
    public void close() {
        if (connection == null) {
            LOGGER.error("DB : Uninitialized DB Connection.");
            return;
        }

        try {
             if (connection.isClosed()) {
                LOGGER.warn("Connection is already closed.");
                return;
             }
             connection.close();
        } catch (SQLException e) {
            LOGGER.error("Error closing the connection.", e);
        }

    }

}
