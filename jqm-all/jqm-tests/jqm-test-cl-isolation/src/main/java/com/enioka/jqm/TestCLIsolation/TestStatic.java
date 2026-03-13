package com.enioka.jqm.testclisolation;

/**
 * Holder for a static variable used to test class loader isolation.
 */
public class TestStatic
{
    /** Default value for the static variable. */
    public static final int DEFAULT_STATIC_VALUE = 0;
    private static int staticVariable = DEFAULT_STATIC_VALUE;

    /**
     * Gets the current value of the static variable.
     *
     * @return the current value
     */
    public static int getStaticVariable()
    {
        return staticVariable;
    }

    /**
     * Sets the static variable to a new value.
     *
     * @param staticVariable
     *            the new value
     */
    public static void setStaticVariable(int staticVariable)
    {
        TestStatic.staticVariable = staticVariable;
    }
}
