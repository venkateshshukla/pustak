package in.vshukla.booksindia;

import in.vshukla.booksindia.exceptions.AppRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

import static in.vshukla.booksindia.AppUtils.blankStringCheck;

/**
 * This is a class following singleton pattern.
 * It would be responsible for holding the DbConnection and executing the sql commands against it.
 *
 * Created by venkatesh on 9/4/17.
 */
public class DbConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbConnection.class);

    private static final Object lock = new Object();

    private Connection connection;

    private final AtomicBoolean isInitialized = new AtomicBoolean(Boolean.FALSE);

    /**
     * Made private for Singleton pattern.
     */
    private DbConnection() {
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
        if (!DbConnectionHolder.INSTANCE.isInitialized.get()) {
            synchronized (lock) {
                DbConnectionHolder.INSTANCE.initialize();
            }
        }
        return DbConnectionHolder.INSTANCE;
    }

    /**
     * Initialize the DB connection.
     * Checks for presence of Driver as well.
     */
    private void initialize() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("DB: Unable to find sql driver.", e);
            throw new AppRuntimeException("Unable to find sql driver", e);
        }

        DbCred cred = getDbCred();
        this.connection = getConnection(cred);
        this.isInitialized.set(Boolean.TRUE);
    }

    /**
     * Create a connection using the given Database credentials.
     *
     * @param cred  Database credentials
     * @return
     */
    private Connection getConnection(DbCred cred) {
        LOGGER.debug("DB: Attempting connection using {}", cred);
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(cred.url, cred.user, cred.pwd);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            LOGGER.error("DB: Unable to open SQL Connection", e);
            throw new AppRuntimeException("Unable to open SQL Connection.", e);
        }
        LOGGER.info("DB: Connection successful.");
        return connection;
    }

    /**
     * Create a {@link DbCred} instance using environmental variables.
     * The variables used are :
     *
     * URL = {@value AppConstants#PROP_DB_URL}; Default value = {@value AppConstants#DEFAULT_DB_URL} using Environment variable {@value AppConstants#PROP_USER}
     * USER = {@value AppConstants#PROP_DB_USR}; Default value = Environment variable {@value AppConstants#PROP_USER}
     * PASSWD = {@value AppConstants#PROP_DB_PWD}; Default value = {@value AppConstants#DEFAULT_DB_PWD}
     *
     * @return {@link DbCred} object containing the Database connection parameters.
     */
    private DbCred getDbCred() {
        String url = AppUtils.getProperty(AppConstants.PROP_DB_URL, () -> String.format(AppConstants.DEFAULT_DB_URL, System.getenv(AppConstants.PROP_USER)));
        String user = AppUtils.getProperty(AppConstants.PROP_DB_USR, () -> System.getenv(AppConstants.PROP_USER));
        String pwd = AppUtils.getProperty(AppConstants.PROP_DB_PWD, () -> AppConstants.DEFAULT_DB_PWD);
        return new DbCred(url, user, pwd);
    }

    /**
     * Execute the given SQL statement and commit.
     * @param sql   The SQL to execute.
     * @throws SQLException In case of any errors while execution.
     */
    public void executeSql(String sql) throws SQLException {
        executeSql(sql, true, AppConstants.DEFAULT_FETCH_SIZE, ResultSet.FETCH_FORWARD);
    }

    /**
     * Execute the given SQL statement. Commit as per the requirement.
     * @param sql
     * @param doCommit
     * @throws SQLException
     */
    public void executeSql(String sql, boolean doCommit) throws SQLException {
        executeSql(sql, doCommit, AppConstants.DEFAULT_FETCH_SIZE, ResultSet.FETCH_FORWARD);
    }

    /**
     * Utilizing the open DB Connection, execute the given SQL.
     * If needed, execute a commit.
     *
     * @param sql       Given SQL statement.
     * @param doCommit  Should the changes be committed?
     * @param fetchSize The number of records to be fetched in one go. Less is good on memory but slow.
     * @param fetchDirn The direction to be used while fetching.
     * @throws SQLException In case of any errors while execution.
     */
    public void executeSql(String sql, boolean doCommit, int fetchSize, int fetchDirn) throws SQLException {

        connectionCheck();
        blankStringCheck(sql, "DB : Cannot execute null or empty SQL.");

        LOGGER.info("DB : Executing the SQL : {}", sql);

        Statement stmt = connection.createStatement();
        stmt.setFetchDirection(fetchDirn);
        stmt.setFetchSize(fetchSize);
        stmt.executeUpdate(sql);
        stmt.close();

        if (doCommit) {
            LOGGER.info("DB : Committing the changes.");
            connection.commit();
        }
    }

    /**
     * Fetch ResultSet from DB Connection.
     *
     * @param sql   SELECT statement to be executed
     * @return      {@link ResultSet} containing the information fetched using given SQL.
     * @throws SQLException
     */
    public ResultSet fetchResult(String sql) throws SQLException {
        connectionCheck();
        blankStringCheck(sql, "DB : Cannot execute blank SQL.");
        Statement stmt = connection.createStatement();
        return executeFetch(sql, AppConstants.DEFAULT_FETCH_SIZE, ResultSet.FETCH_FORWARD);
    }

    /**
     * Using the given fetchSize and fetchDirection, get the ResultSet.
     *
     * @param sql       SELECT statement to be executed
     * @param fetchSize Size of the batch of rows to be fetched in one go.
     * @param fetchDirn Direction of fetch.
     * @return
     * @throws SQLException
     */
    public ResultSet executeFetch(String sql, int fetchSize, int fetchDirn) throws SQLException {
        connectionCheck();
        blankStringCheck(sql, "DB : Cannot execute blank SQL.");

        Statement stmt = connection.prepareStatement(sql);
        stmt.setFetchSize(fetchSize);
        stmt.setFetchDirection(fetchDirn);
        return stmt.getResultSet();
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

    /**
     * Check if the connection is initialized. Throw an {@link IllegalArgumentException} if not.
     */
    private void connectionCheck() {
        AppUtils.nullCheck(connection, "DB: Uninitialized DB Connection.");
    }

    private static class DbCred {
        String url, user, pwd;

        public DbCred(String url, String user, String pwd) {
            this.url = url;
            this.user = user;
            this.pwd = pwd;
        }

        @Override
        public String toString() {
            return "DbCred{" +
                    "url='" + url + '\'' +
                    ", user='" + user + '\'' +
                    ", pwd='" + pwd + '\'' +
                    '}';
        }
    }

}
