/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * Data Utility class for the Shouter API.
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class DataUtil {

    /**
     * Tests if a string is empty
     *
     * @param testString  the string to test
     * @return true if empty or null, false otherwise
     */
    public static boolean isEmpty(String testString) {
        return (testString == null || testString.isEmpty());
    }

    /**
     * Retrieves the request parameter in the form of a string.
     *
     * @param request - the HttpServletRequest object to interrogate
     * @param parameter - the parameter to retrieve
     *
     * @return the retrieved parameter (or an empty string if it is non-existent.)
     */
    public static String formatParameter(HttpServletRequest request, String parameter) {
        String value = request.getParameter(parameter);

        if (isEmpty(value)) {
            value = "";
        }

        return value;
    }

    /**
     * Checks if a string can be parsed as an integer
     *
     * @param testString the string to test
     * @return true if an integer, false otherwise
     */
    public static boolean isInteger(String testString) {
        Integer stringToInt = null;
        try {
            stringToInt = Integer.parseInt(testString);
        } catch (Exception ignore) { }

        return (stringToInt != null);
    }

    /**
     * Checks if a double is a valid location (0 < x < 360)
     *
     * @param location the location to check
     * @return true if valid, false otherwise
     */
    public static boolean isDegrees(Double location) {
        return (location != null && location > 0.0 && location < 360.0);
    }

    /**
     * Checks if a String is 256 characters or less
     *
     * @param message the string to check
     * @return true if less than 257 characters, false otherwise
     */
    public static boolean is256characters(String message) {
        return (message != null && message.length() < 257);
    }
}
