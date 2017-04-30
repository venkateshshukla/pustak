package in.vshukla.booksindia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by venkatesh on 28/4/17.
 */
public class FetchResult {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchResult.class);

    private static final Indexer indexer = new Indexer();

    public static void main(String[] args) {
        DbConnection dbConnection = DbConnection.getInstance();
        try {
            indexer.initialize();
            dbConnection.processLargeResultSet("SELECT * FROM BOOK", FetchResult::indexDataSafe);
        } catch (SQLException e) {
            LOGGER.error("Caught SQL Exception", e);
        } catch (IOException e) {
            LOGGER.error("IO Exception while initializiing index.", e);
        }
    }

    private static void printDataClean(ResultSet resultSet) {
        try {
            printData(resultSet);
        } catch (SQLException e) {
            LOGGER.error("Caught SQL Exception", e);
        }
    }

    private static void printData(final ResultSet resultSet) throws SQLException {
            ResultSetMetaData metaData = resultSet.getMetaData();
            System.out.print("BOOK {");
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
               System.out.print(String.format(" \"%s\" : \"%s\",", metaData.getColumnName(i),
                       AppUtils.cleanValue(resultSet.getString(i))));
            }
            System.out.println(" };");
    }

    private static void indexDataSafe(final ResultSet resultSet) {
        try {
            indexData(resultSet);
        } catch (SQLException e) {
            LOGGER.error("SQL Execution failed.", e);
        } catch (IOException e) {
            LOGGER.error("IO Error while indexing.", e);
        }
    }


    private static void indexData(final ResultSet resultSet) throws SQLException, IOException {
        ResultSetMetaData metaData =  resultSet.getMetaData();
        Map<String, String> data = new HashMap<>(metaData.getColumnCount());
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            data.put(metaData.getColumnName(i), AppUtils.cleanValue(resultSet.getString(i)));
        }
        indexer.insert(data);
    }

}
