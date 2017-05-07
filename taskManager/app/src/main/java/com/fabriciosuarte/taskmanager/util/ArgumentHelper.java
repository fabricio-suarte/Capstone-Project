package com.fabriciosuarte.taskmanager.util;

/**
 * A helper for operations regarding methods arguments, parameters, etc.
 */
public final class ArgumentHelper {

    /**
     * Test if a given string argument is null or empty. If it is, an exception is thrown
     * @param arg The argument value
     * @param argName The name of the argument
     */
    public static void validateNullString(String arg, String argName) {

        if(arg == null || argName.isEmpty())
            throw new IllegalArgumentException( String.format("'%s' cannot be null or empty!", argName));
    }

    /**
     * Test if a given argument is null. If it is, an exception is thrown
     * @param arg The argument value
     * @param argName The name of the argument
     */
    public static void validateNull(Object arg, String argName) {

        if(arg == null)
            throw new IllegalArgumentException( String.format("'%s' cannot be null!", argName));
    }

    /**
     * Test if a given argument is less or equal to N. If it is, an exception is thrown
     * @param arg The argument value
     * @param argName The name of the argument
     * @param n The 'n' integer for the testing
     */
    public static void validateLessOrEqualToN(int arg, String argName, int n) {
        if(arg <= n)
            throw new IllegalArgumentException( String.format("'%s' must be greater than '%s'!", argName, n));
    }

    /**
     * Test if a given argument is less or equal to N. If it is, an exception is thrown
     * @param arg The argument value
     * @param argName The name of the argument
     * @param n The 'n' integer for the testing
     */
    public static void validateLessOrEqualToN(long arg, String argName, long n) {
        if(arg <= n)
            throw new IllegalArgumentException( String.format("'%s' must be greater than '%s'!", argName, n));
    }
}
