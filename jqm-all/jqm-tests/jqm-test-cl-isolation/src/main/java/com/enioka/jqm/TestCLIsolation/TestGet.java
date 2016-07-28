package com.enioka.jqm.TestCLIsolation;

public class TestGet
{
    public static void main(String[] args)
    {
        if (TestStatic.getStaticVariable() != TestStatic.DEFAULT_STATIC_VALUE)
        {
            throw new RuntimeException("Value modified");
        }
    }
}
