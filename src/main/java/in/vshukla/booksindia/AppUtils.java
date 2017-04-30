package in.vshukla.booksindia;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Predicate;
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
     */
    public static void blankStringCheck (String str) {
        blankStringCheck(str, "Encountered blank string.");
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

    /**
     * Check if a directory of the given name is present.
     * Also checks if the file of the given name is a directory or not.
     *
     * @param dirStr Name of the directory to be checked for existence.
     * @return Answer to the question, does a directory with given name exist?
     */
    public static boolean directoryExists(String dirStr) {
        blankStringCheck(dirStr, "Cannot check for a blank directory name.");
        return isPresent(dirStr, (p) -> Files.isDirectory(p));
    }

    /**
     * Check if a regular file of the given name is present.
     * Also checks if the file of the given name is a regular file or not.
     *
     * @param fileStr Name of the file to be checked for existence.
     * @return Answer to the question, does a regular file with given name exist?
     */
    public static boolean fileExists(String fileStr) {
        blankStringCheck(fileStr, "Cannot check for a blank file name.");
        return isPresent(fileStr, (p) -> Files.isRegularFile(p));
    }

    /**
     * Check if a file of the given name is present.
     * File here refers to normal files, directories, symbolic links and other such stuff.
     *
     * @param fileStr Name of the file to be checked for existence.
     * @return Answer to the question, does this file exist?
     */
    public static boolean isPresent(String fileStr) {
        return isPresent(fileStr, (p) -> true);
    }

    /**
     * Check if a file of the given name is present.
     * A criteria can also be passed which would be evaluated in case of presence of the file.
     * File here refers to normal files, directories, symbolic links and other such stuff.
     *
     * @param fileStr   Name of the file to be checked for existence.
     * @param criteria  A criteria that can be evaluated on the {@link Path} corresponding to given file.
     * @return Answer to the question, does this file exists and does it satisfy the given criteria?
     */
    private static boolean isPresent(String fileStr, Predicate<Path> criteria) {
        blankStringCheck(fileStr, "Cannot check for a blank file name.");
        Path filePath = Paths.get(fileStr);
        return Files.exists(filePath) && criteria.test(filePath);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Convert an Object to a {@code Map<String, String>} where K is the field name and V is the value.
     *
     * @param object    Source object
     * @return          Map representation of the object
     */
    public static Map<String, String> getMapFromObject(Object object) {
        assert object != null : "Cannot map a null object";
        return MAPPER.convertValue(object, new TypeReference<Map<String, String>>() {});
    }

}
