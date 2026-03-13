package com.enioka.jqm.testclisolation;

/**
 * Test class for getting a static variable.
 *
 * Used to test class loader isolation.
 */
public class TestGet
{
    /**
     * Main entry point that checks a static variable has not been modified.
     *
     * @param args
     *            command line arguments (unused)
     */
    public static void main(String[] args)
    {
        if (TestStatic.getStaticVariable() != TestStatic.DEFAULT_STATIC_VALUE)
        {
            throw new RuntimeException("Value modified");
        }
    }
}
