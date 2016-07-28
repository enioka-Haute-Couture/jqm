package com.enioka.jqm.TestCLIsolation;


public class TestStatic
{
    public static final int DEFAULT_STATIC_VALUE = 0;
    private static int staticVariable = DEFAULT_STATIC_VALUE;

    public static int getStaticVariable()
    {
        return staticVariable;
    }

    public static void setStaticVariable(int staticVariable)
    {
        TestStatic.staticVariable = staticVariable;
    }
}
