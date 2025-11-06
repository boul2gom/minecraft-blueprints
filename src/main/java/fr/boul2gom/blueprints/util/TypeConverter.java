package fr.boul2gom.blueprints.util;

/**
 * Utility class for type conversions in blueprint execution.
 * Reduces code duplication across nodes that need to convert between pin value types.
 *
 * All methods are static and handle null values gracefully.
 */
public class TypeConverter {

    /**
     * Converts a value to a Number, returning a default if conversion fails.
     *
     * @param value the value to convert
     * @param default_value the default value if conversion fails or value is null
     * @return the converted number or default
     */
    public static Number to_number(Object value, Number default_value) {
        if (value instanceof Number num) {
            return num;
        }
        return default_value;
    }

    /**
     * Converts a value to an int, returning a default if conversion fails.
     *
     * @param value the value to convert
     * @param default_value the default value if conversion fails or value is null
     * @return the converted int or default
     */
    public static int to_int(Object value, int default_value) {
        if (value instanceof Number num) {
            return num.intValue();
        }
        return default_value;
    }

    /**
     * Converts a value to a float, returning a default if conversion fails.
     *
     * @param value the value to convert
     * @param default_value the default value if conversion fails or value is null
     * @return the converted float or default
     */
    public static float to_float(Object value, float default_value) {
        if (value instanceof Number num) {
            return num.floatValue();
        }
        return default_value;
    }

    /**
     * Converts a value to a double, returning a default if conversion fails.
     *
     * @param value the value to convert
     * @param default_value the default value if conversion fails or value is null
     * @return the converted double or default
     */
    public static double to_double(Object value, double default_value) {
        if (value instanceof Number num) {
            return num.doubleValue();
        }
        return default_value;
    }

    /**
     * Converts a value to a boolean, returning a default if conversion fails.
     * Numbers are converted: 0 = false, non-zero = true.
     *
     * @param value the value to convert
     * @param default_value the default value if conversion fails or value is null
     * @return the converted boolean or default
     */
    public static boolean to_boolean(Object value, boolean default_value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number num) {
            return num.doubleValue() != 0.0;
        }
        return default_value;
    }

    /**
     * Converts a value to a String, returning a default if value is null.
     *
     * @param value the value to convert
     * @param default_value the default value if value is null
     * @return the string representation or default
     */
    public static String to_string(Object value, String default_value) {
        if (value == null) {
            return default_value;
        }
        return value.toString();
    }

    /**
     * Validates that a value is a Number, throwing an exception if not.
     *
     * @param value the value to validate
     * @param context_message context message for the exception (e.g., "AddNode input 'a'")
     * @return the value as a Number
     * @throws IllegalStateException if value is not a Number
     */
    public static Number require_number(Object value, String context_message) {
        if (value == null) {
            throw new IllegalStateException(
                String.format("%s: value is null (expected Number)", context_message)
            );
        }
        if (!(value instanceof Number)) {
            throw new IllegalStateException(
                String.format("%s: value must be a Number, got %s",
                    context_message, value.getClass().getSimpleName())
            );
        }
        return (Number) value;
    }

    /**
     * Validates that a value is a Boolean, throwing an exception if not.
     *
     * @param value the value to validate
     * @param context_message context message for the exception
     * @return the value as a Boolean
     * @throws IllegalStateException if value is not a Boolean
     */
    public static Boolean require_boolean(Object value, String context_message) {
        if (value == null) {
            throw new IllegalStateException(
                String.format("%s: value is null (expected Boolean)", context_message)
            );
        }
        if (!(value instanceof Boolean)) {
            throw new IllegalStateException(
                String.format("%s: value must be a Boolean, got %s",
                    context_message, value.getClass().getSimpleName())
            );
        }
        return (Boolean) value;
    }

    /**
     * Validates that a value is a String, throwing an exception if not.
     *
     * @param value the value to validate
     * @param context_message context message for the exception
     * @return the value as a String
     * @throws IllegalStateException if value is not a String
     */
    public static String require_string(Object value, String context_message) {
        if (value == null) {
            throw new IllegalStateException(
                String.format("%s: value is null (expected String)", context_message)
            );
        }
        if (!(value instanceof String)) {
            throw new IllegalStateException(
                String.format("%s: value must be a String, got %s",
                    context_message, value.getClass().getSimpleName())
            );
        }
        return (String) value;
    }

    /**
     * Validates that a value is a Number and converts it to an int.
     * Throws an exception if the value is not a Number.
     *
     * @param value the value to validate and convert
     * @param context_message context message for the exception
     * @return the value as an int
     * @throws IllegalStateException if value is not a Number
     */
    public static int require_int(Object value, String context_message) {
        return require_number(value, context_message).intValue();
    }

    /**
     * Validates that a value is a Number and converts it to a float.
     * Throws an exception if the value is not a Number.
     *
     * @param value the value to validate and convert
     * @param context_message context message for the exception
     * @return the value as a float
     * @throws IllegalStateException if value is not a Number
     */
    public static float require_float(Object value, String context_message) {
        return require_number(value, context_message).floatValue();
    }

    /**
     * Validates that a value is a Number and converts it to a double.
     * Throws an exception if the value is not a Number.
     *
     * @param value the value to validate and convert
     * @param context_message context message for the exception
     * @return the value as a double
     * @throws IllegalStateException if value is not a Number
     */
    public static double require_double(Object value, String context_message) {
        return require_number(value, context_message).doubleValue();
    }
}
