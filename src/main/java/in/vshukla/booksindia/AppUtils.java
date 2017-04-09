package in.vshukla.booksindia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by venkatesh on 9/4/17.
 */
public class AppUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUtils.class);

    public static String getProperty(String propName) {
        String val = System.getProperty(propName);
        if (val != null && !val.trim().isEmpty()) {
            LOGGER.debug("JVM Properties. Key : {}, Value : {}", propName, val);
            return val;
        }
        LOGGER.warn("Property {} not found in JVM Args.", propName);
        val = System.getenv(propName);
        if (val != null && !val.trim().isEmpty()) {
            LOGGER.debug("Environmental Variable. Key : {}, Value : {}.", propName, val);
            return val;
        }
        LOGGER.warn("Property {} not found in Environmental Variables.", propName);
        return null;
    }
}
