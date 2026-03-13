package com.enioka.jqm.testclisolation;

/**
 * Test class for setting a static variable.
 *
 * Used to test class loader isolation.
 */
public class TestSet
{
    /**
     * Main entry point that sets a static variable.
     *
     * @param args
     *            command line arguments (unused)
     */
    public static void main(String[] args)
    {
        TestStatic.setStaticVariable(1);
    }
}
