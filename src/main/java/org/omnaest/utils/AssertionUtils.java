package org.omnaest.utils;

/**
 * Helper asserting variables
 * 
 * @author omnaest
 */
public class AssertionUtils
{
    private AssertionUtils()
    {
    }

    /**
     * @throws IllegalArgumentException
     * @param object
     */
    public static void assertIsNotNull(Object object)
    {
        assertIsNotNull("Object must not be null", object);
    }

    /**
     * Similar to {@link #assertIsNotNull(Object)} but allows to provide a message
     * 
     * @param message
     * @param object
     */
    public static void assertIsNotNull(String message, Object object)
    {
        if (object == null)
        {
            throw new IllegalArgumentException(message);
        }
    }
}
