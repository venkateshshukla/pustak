package in.vshukla.booksindia;

import java.sql.ResultSet;

/**
 * Created by venkatesh on 9/4/17.
 */
public interface AppConstants {
    String PROP_DB_URL = "BOOKSINDIA_DB_URL";
    String PROP_DB_USR = "BOOKSINDIA_DB_USR";
    String PROP_DB_PWD = "BOOKSINDIA_DB_PWD";
    String PROP_IDX_LOC = "BOOKSINDIA_IDX_LOC";

    String PROP_USER = "USER";

    String DEFAULT_DB_URL = "jdbc:postgresql://localhost/%s";
    String DEFAULT_DB_PWD = "password";
    String DEFAULT_IDX_LOC = "/tmp/booksindia";

    int DEFAULT_FETCH_SIZE = 128;
    int DEFAULT_FETCH_DIRN = ResultSet.FETCH_FORWARD;

    String PROP_PS2 = "PS2";
    String DEFAULT_PS2 = ">> ";

}
