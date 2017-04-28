package in.vshukla.booksindia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Some common functions used across application.
 * Created by venkatesh on 9/4/17.
 */
public class AppUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUtils.class);

    /**
     * Get value of a system property.
     * The precedence is as follows.
     * JVM Prop > Env variable > Default value
     *
     * @param propName  Name of the property
     * @param defaulValSupplier Default value supplier in case both JVM and Env variables are blank.
     * @return  Value of the required property. defaultVal in case it is not found.
     */
    public static String getProperty (String propName, Supplier<String> defaulValSupplier) {
        String val = getProperty(propName);
        if (val == null) {
            val = defaulValSupplier.get();
        }
        return val;
    }

    /**
     * Get the value of given property.
     * Precedence is as follows.
     * JVM Prop > Env variable
     * Null is returned in case this property is not found.
     * @param propName  Name of the property
     * @return Value of required property. Null in case it is not found.
     */
    public static String getProperty (String propName) {
        blankStringCheck(propName, "Cannot fetch using a blank property name.");
        String val = System.getProperty(propName);
        if (val != null && !val.trim().isEmpty()) {
            LOGGER.debug("JVM Properties. Key : {}, Value : {}", propName, val);
            return val.trim();
        }
        LOGGER.warn("Property {} not found in JVM Args.", propName);
        val = System.getenv(propName);
        if (val != null && !val.trim().isEmpty()) {
            LOGGER.debug("Environmental Variable. Key : {}, Value : {}.", propName, val);
            return val.trim();
        }
        LOGGER.warn("Property {} not found in Environmental Variables.", propName);
        return null;
    }

    /**
     * Check for blankness of a String. Throw an {@link IllegalArgumentException} if it is.
     * @param str   String to be checked for blankness.
     * @param msg   Error msg.
     */
    public static void blankStringCheck (String str, String msg) {
        if (str == null || str.trim().isEmpty()) {
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Check if the given object is null. Throw {@link IllegalArgumentException} if it is.
     * @param obj   Object to be tested for null.
     * @param msg   Error msg.
     */
    public static void nullCheck (Object obj, String msg) {
        if (obj == null) {
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Perform basic cleaning operations. In case of null, returns an empty string.
     * For non-null inputs, it returns trimmed value.
     *
     * @param value Value to be cleaned
     * @return  Cleaned value. Empty string in case of null. Trimmed otherwise.
     */
    public static String cleanValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
