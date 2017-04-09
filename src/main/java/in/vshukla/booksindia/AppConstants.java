package in.vshukla.booksindia;

/**
 * Created by venkatesh on 9/4/17.
 */
public interface AppConstants {
    String PROP_DB_URL = "BOOKSINDIA_DB_URL";
    String PROP_DB_USR = "BOOKSINDIA_DB_USR";
    String PROP_DB_PWD = "BOOKSINDIA_DB_PWD";

    String PROP_USER = "USER";

    String DEFAULT_DB_URL = "jdbc:postgresql://localhost/%s";

    int DEFAULT_FETCH_SIZE = 128;
}
