package jmri.util;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;

import jmri.Reportable;


/**
 * Converts between java types, for example String to Double and double to boolean.
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public final class TypeConversionUtil {

    /**
     * Is this object an integer number?
     * <P>
     * The method returns true if the object is any of these classes:
     * <ul>
     *   <li>AtomicInteger</li>
     *   <li>AtomicLong</li>
     *   <li>BigInteger</li>
     *   <li>Byte</li>
     *   <li>Short</li>
     *   <li>Integer</li>
     *   <li>Long</li>
     * </ul>
     * @param object the object to check
     * @return true if the object is an object that is an integer, false otherwise
     */
    public static boolean isIntegerNumber(Object object) {
        return (object instanceof java.util.concurrent.atomic.AtomicInteger)
                || (object instanceof java.util.concurrent.atomic.AtomicLong)
                || (object instanceof java.math.BigInteger)
                || (object instanceof Byte)
                || (object instanceof Short)
                || (object instanceof Integer)
                || (object instanceof Long);
    }

    /**
     * Is this object an integer or a floating point number?
     * <P>
     * The method returns true if the object is any of these classes:
     * <ul>
     *   <li>AtomicInteger</li>
     *   <li>AtomicLong</li>
     *   <li>BigInteger</li>
     *   <li>Byte</li>
     *   <li>Short</li>
     *   <li>Integer</li>
     *   <li>Long</li>
     *   <li>BigDecimal</li>
     *   <li>Float</li>
     *   <li>Double</li>
     * </ul>
     * @param object the object to check
     * @return true if the object is an object that is either an integer or a
     * float, false otherwise
     */
    public static boolean isFloatingNumber(Object object) {
        return isIntegerNumber(object)
                || (object instanceof java.math.BigDecimal)
                || (object instanceof Float)
                || (object instanceof Double);
    }

    /**
     * Is this object a String?
     * @param object the object to check
     * @return true if the object is a String, false otherwise
     */
    public static boolean isString(Object object) {
        return object instanceof String;
    }


    /**
     * Convert a value to a boolean.
     * <P>
     * Rules:
     * "0" string is converted to false
     * "0.000" string is converted to false, if the number of decimals is &gt; 0
     * An integer number is converted to false if the number is 0
     * A floating number is converted to false if the number is -0.5 &lt; x &lt; 0.5
     * The string "true" (case insensitive) returns true.
     * The string "false" (case insensitive) returns false.
     * A Reportable is first converted to a string using toReportString() and then
     * treated as a string.
     * A JSON TextNode is first converted to a string using asText() and then
     * treated as a string.
     * Everything else throws an exception.
     * <P>
     * For objects that implement the Reportable interface, the value is fetched
     * from the method toReportString().
     *
     * @param value the value to convert
     * @param do_i18n true if internationalization should be done, false otherwise
     * @return the boolean value
     */
    public static boolean convertToBoolean(@CheckForNull Object value, boolean do_i18n) {
        if (value instanceof Boolean) {
            return ((Boolean)value);
        }

        // JSON text node
        if (value instanceof com.fasterxml.jackson.databind.node.TextNode) {
            value = ((com.fasterxml.jackson.databind.node.TextNode)value).asText();
        }

        if (value instanceof Reportable) {
            value = ((Reportable)value).toReportString();
        }

        if (value == null) {
            throw new IllegalArgumentException("Value is null");
        }

        try {
            // Can the value be converted to a long?
            value = convertToLong(value, true, true, false);
        } catch (NumberFormatException e) {
            try {
                // Can the value be converted to a double?
                value = convertToDouble(value, do_i18n, true, true, false);
            } catch (NumberFormatException e2) {
                // Do nothing
            }
        }

        if (value instanceof Number) {
            double number = ((Number)value).doubleValue();
            return ! ((-0.5 < number) && (number < 0.5));
        } else if (value instanceof Boolean) {
            return (Boolean)value;
        } else {
            switch (value.toString().toLowerCase()) {
                case "false": return false;
                case "true": return true;
                default: throw new IllegalArgumentException(String.format("Value \"%s\" can't be converted to a boolean", value));
            }
        }
    }

    private static boolean convertStringToBoolean_JythonRules(@Nonnull String str, boolean do_i18n) {
        // try to parse the string as a number
        try {
            double number;
            if (do_i18n) {
                number = IntlUtilities.doubleValue(str);
            } else {
                number = Double.parseDouble(str);
            }
//                System.err.format("The string: '%s', result: %1.4f%n", str, (float)number);
            return ! ((-0.5 < number) && (number < 0.5));
        } catch (NumberFormatException | ParseException ex) {
            log.debug("The string '{}' cannot be parsed as a number", str);
        }

//            System.err.format("The string: %s, %s%n", str, value.getClass().getName());
        String patternString = "^0(\\.0+)?$";
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
//                System.err.format("The string: '%s', result: %b%n", str, false);
            return false;
        }
//            System.err.format("The string: '%s', result: %b%n", str, !str.isEmpty());
        return !str.isEmpty();
    }

    /**
     * Convert a value to a boolean by Jython rules.
     * <P>
     * Rules:
     * null is converted to false
     * empty string is converted to false
     * "0" string is converted to false
     * "0.000" string is converted to false, if the number of decimals is &gt; 0
     * empty map is converted to false
     * empty collection is converted to false
     * An integer number is converted to false if the number is 0
     * A floating number is converted to false if the number is -0.5 &lt; x &lt; 0.5
     * Everything else is converted to true
     * <P>
     * For objects that implement the Reportable interface, the value is fetched
     * from the method toReportString().
     *
     * @param value the value to convert
     * @param do_i18n true if internationalization should be done, false otherwise
     * @return the boolean value
     */
    public static boolean convertToBoolean_JythonRules(@CheckForNull Object value, boolean do_i18n) {
        if (value == null) {
            return false;
        }

        if (value instanceof Map) {
            var map = ((Map<?,?>)value);
            return !map.isEmpty();
        }

        if (value instanceof Collection) {
            var collection = ((Collection<?>)value);
            return !collection.isEmpty();
        }

        if (value.getClass().isArray()) {
            var array = ((Object[])value);
            return array.length > 0;
        }

        // JSON text node
        if (value instanceof com.fasterxml.jackson.databind.node.TextNode) {
            value = ((com.fasterxml.jackson.databind.node.TextNode)value).asText();
        }

        if (value instanceof Reportable) {
            value = ((Reportable)value).toReportString();
        }

        if (value instanceof Number) {
            double number = ((Number)value).doubleValue();
            return ! ((-0.5 < number) && (number < 0.5));
        } else if (value instanceof Boolean) {
            return (Boolean)value;
        } else {
            if (value == null) return false;
            return convertStringToBoolean_JythonRules(value.toString(), do_i18n);
        }
    }

    private static long convertStringToLong(@Nonnull String str, boolean checkAll, boolean throwOnError, boolean warnOnError) {
        String patternString = "^(\\-?\\d+)";
        if (checkAll) patternString += "$";
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        // Only look at the beginning of the string
        if (matcher.lookingAt()) {
            String theNumber = matcher.group(1);
            long number = Long.parseLong(theNumber);
//            System.err.format("Number: %1.5f%n", number);
            log.debug("the string {} is converted to the number {}", str, number);
            return number;
        } else {
            if (warnOnError) {
                log.warn("the string \"{}\" cannot be converted to a number", str);
            }
            if (throwOnError) {
                throw new NumberFormatException(
                        String.format("the string \"%s\" cannot be converted to a number", str));
            }
            return 0;
        }
    }

    /**
     * Convert a value to a long.
     * <P>
     * Rules:
     * null is converted to 0
     * empty string is converted to 0
     * empty collection is converted to 0
     * an instance of the interface Number is converted to the number
     * a string that can be parsed as a number is converted to that number.
     * a string that doesn't start with a digit is converted to 0
     * <P>
     * For objects that implement the Reportable interface, the value is fetched
     * from the method toReportString() before doing the conversion.
     *
     * @param value the value to convert
     * @return the long value
     */
    public static long convertToLong(@CheckForNull Object value) {
        return convertToLong(value, false, false);
    }

    /**
     * Convert a value to a long.
     * <P>
     * Rules:
     * null is converted to 0
     * empty string is converted to 0
     * empty collection is converted to 0
     * an instance of the interface Number is converted to the number
     * a string that can be parsed as a number is converted to that number.
     * a string that doesn't start with a digit is converted to 0
     * <P>
     * For objects that implement the Reportable interface, the value is fetched
     * from the method toReportString() before doing the conversion.
     *
     * @param value the value to convert
     * @param checkAll true if the whole string should be checked, false otherwise
     * @param throwOnError true if a NumberFormatException should be thrown on error, false otherwise
     * @return the long value
     * @throws NumberFormatException on error if throwOnError is true
     */
    public static long convertToLong(@CheckForNull Object value, boolean checkAll, boolean throwOnError) {
        return convertToLong(value, checkAll, throwOnError, true);
    }

    /**
     * Convert a value to a long.
     * <P>
     * Rules:
     * null is converted to 0
     * empty string is converted to 0
     * empty collection is converted to 0
     * an instance of the interface Number is converted to the number
     * a string that can be parsed as a number is converted to that number.
     * a string that doesn't start with a digit is converted to 0
     * <P>
     * For objects that implement the Reportable interface, the value is fetched
     * from the method toReportString() before doing the conversion.
     *
     * @param value the value to convert
     * @param checkAll true if the whole string should be checked, false otherwise
     * @param throwOnError true if a NumberFormatException should be thrown on error, false otherwise
     * @param warnOnError true if a warning message should be logged on error
     * @return the long value
     * @throws NumberFormatException on error if throwOnError is true
     */
    public static long convertToLong(@CheckForNull Object value, boolean checkAll, boolean throwOnError, boolean warnOnError)
            throws NumberFormatException {
        if (value == null) {
            log.warn("the object is null and the returned number is therefore 0.0");
            return 0;
        }

        // JSON text node
        if (value instanceof com.fasterxml.jackson.databind.node.TextNode) {
            value = ((com.fasterxml.jackson.databind.node.TextNode)value).asText();
        }

        if (value instanceof Reportable) {
            value = ((Reportable)value).toReportString();
        }

        if (value instanceof Number) {
//            System.err.format("Number: %1.5f%n", ((Number)value).doubleValue());
            if (!(value instanceof Byte) && !(value instanceof Short) && !(value instanceof Integer) && !(value instanceof Long)) {
                if (throwOnError) {
                    throw new NumberFormatException(
                            String.format("the value %s cannot be converted to an integer", value));
                }
            }
            return ((Number)value).longValue();
        } else if (value instanceof Boolean) {
            if (throwOnError) {
                throw new NumberFormatException(
                        String.format("the boolean value \"%b\" cannot be converted to an integer", ((Boolean)value)));
            }
            return Boolean.TRUE.equals(value) ? 1 : 0;
        } else {
            if (value == null) {
                if (throwOnError) {
                    throw new NumberFormatException(
                            "the null value cannot be converted to an integer");
                }
                return 0;
            }
            return convertStringToLong(value.toString(), checkAll, throwOnError, warnOnError);
        }
    }

    private static double convertStringToDouble(@Nonnull String str, boolean checkAll, boolean throwOnError, boolean warnOnError) {
        String patternString = "^(\\-?\\d+(\\.\\d+)?(e\\-?\\d+)?)";
        if (checkAll) patternString += "$";
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        // Only look at the beginning of the string
        if (matcher.lookingAt()) {
            String theNumber = matcher.group(1);
            double number = Double.parseDouble(theNumber);
//            System.err.format("Number: %1.5f%n", number);
            log.debug("the string {} is converted to the number {}", str, number);
            return number;
        } else {
            if (warnOnError) {
                log.warn("the string \"{}\" cannot be converted to a number", str);
            }
            if (throwOnError) {
                throw new NumberFormatException(
                        String.format("the string \"%s\" cannot be converted to a number", str));
            }
            return 0.0d;
        }
    }

    /**
     * Convert a value to a double.
     * <P>
     * Rules:
     * null is converted to 0
     * empty string is converted to 0
     * empty collection is converted to 0
     * an instance of the interface Number is converted to the number
     * a string that can be parsed as a number is converted to that number.
     * if a string starts with a number AND do_i18n is false, it's converted to that number
     * a string that doesn't start with a digit is converted to 0
     * <P>
     * For objects that implement the Reportable interface, the value is fetched
     * from the method toReportString() before doing the conversion.
     *
     * @param value the value to convert
     * @param do_i18n true if internationalization should be done, false otherwise
     * @return the double value
     */
    public static double convertToDouble(@CheckForNull Object value, boolean do_i18n) {
        return convertToDouble(value, do_i18n, false, false);
    }

    /**
     * Convert a value to a double.
     * <P>
     * Rules:
     * null is converted to 0
     * empty string is converted to 0
     * empty collection is converted to 0
     * an instance of the interface Number is converted to the number
     * a string that can be parsed as a number is converted to that number.
     * if a string starts with a number AND do_i18n is false, it's converted to that number
     * a string that doesn't start with a digit is converted to 0
     * <P>
     * For objects that implement the Reportable interface, the value is fetched
     * from the method toReportString() before doing the conversion.
     *
     * @param value the value to convert
     * @param do_i18n true if internationalization should be done, false otherwise
     * @param checkAll true if the whole string should be checked, false otherwise
     * @param throwOnError true if a NumberFormatException should be thrown on error, false otherwise
     * @return the double value
     * @throws NumberFormatException on error if throwOnError is true
     */
    public static double convertToDouble(@CheckForNull Object value, boolean do_i18n, boolean checkAll, boolean throwOnError) {
        return convertToDouble(value, do_i18n, checkAll, throwOnError, true);
    }

    /**
     * Convert a value to a double.
     * <P>
     * Rules:
     * null is converted to 0
     * empty string is converted to 0
     * empty collection is converted to 0
     * an instance of the interface Number is converted to the number
     * a string that can be parsed as a number is converted to that number.
     * if a string starts with a number AND do_i18n is false, it's converted to that number
     * a string that doesn't start with a digit is converted to 0
     * <P>
     * For objects that implement the Reportable interface, the value is fetched
     * from the method toReportString() before doing the conversion.
     *
     * @param value the value to convert
     * @param do_i18n true if internationalization should be done, false otherwise
     * @param checkAll true if the whole string should be checked, false otherwise
     * @param throwOnError true if a NumberFormatException should be thrown on error, false otherwise
     * @param warnOnError true if a warning message should be logged on error
     * @return the double value
     * @throws NumberFormatException on error if throwOnError is true
     */
    public static double convertToDouble(@CheckForNull Object value, boolean do_i18n, boolean checkAll, boolean throwOnError, boolean warnOnError) {
        if (value == null) {
            log.warn("the object is null and the returned number is therefore 0.0");
            return 0.0d;
        }

        // JSON text node
        if (value instanceof com.fasterxml.jackson.databind.node.TextNode) {
            value = ((com.fasterxml.jackson.databind.node.TextNode)value).asText();
        }

        if (value instanceof Reportable) {
            value = ((Reportable)value).toReportString();
        }

        if (value instanceof Number) {
//            System.err.format("Number: %1.5f%n", ((Number)value).doubleValue());
            return ((Number)value).doubleValue();
        } else if (value instanceof Boolean) {
            if (throwOnError) {
                throw new NumberFormatException(
                        String.format("the boolean value \"%b\" cannot be converted to a number", ((Boolean)value)));
            }
            return Boolean.TRUE.equals(value) ? 1 : 0;
        } else {
            if (value == null) {
                if (throwOnError) {
                    throw new NumberFormatException(
                            "the null value cannot be converted to a number");
                }
                return 0.0;
            }

            if (do_i18n) {
                // try to parse the string as a number
                try {
                    double number = IntlUtilities.doubleValue(value.toString());
//                    System.err.format("The string: '%s', result: %1.4f%n", value, (float)number);
                    return number;
                } catch (ParseException ex) {
                    log.debug("The string '{}' cannot be parsed as a number", value);
                }
            }
            return convertStringToDouble(value.toString(), checkAll, throwOnError, warnOnError);
        }
    }

    /**
     * Convert a value to a String.
     *
     * @param value the value to convert
     * @param do_i18n true if internationalization should be done, false otherwise
     * @return the String value
     */
    @Nonnull
    public static String convertToString(@CheckForNull Object value, boolean do_i18n) {
        if (value == null) {
            return "";
        }

        // JSON text node
        if (value instanceof com.fasterxml.jackson.databind.node.TextNode) {
            return ((com.fasterxml.jackson.databind.node.TextNode)value).asText();
        }

        if (value instanceof Reportable) {
            return ((Reportable)value).toReportString();
        }

        if (value instanceof Number) {
            if (do_i18n) {
                return IntlUtilities.valueOf(((Number)value).doubleValue());
            }
        }

        return value.toString();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TypeConversionUtil.class);
}
