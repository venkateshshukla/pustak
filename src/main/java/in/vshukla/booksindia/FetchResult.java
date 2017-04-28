package in.vshukla.booksindia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by venkatesh on 28/4/17.
 */
public class FetchResult {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchResult.class);

    public static void main(String[] args) {
        DbConnection dbConnection = DbConnection.getInstance();
        try {
            dbConnection.processResult("SELECT * FROM BOOK", FetchResult::printDataClean, 10);
        } catch (SQLException e) {
            LOGGER.error("Caught SQL Exception", e);
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

}
