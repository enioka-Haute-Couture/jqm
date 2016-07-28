package pyl;

import com.enioka.jqm.TestCLIsolation.TestStatic;

public class EngineCLIsolationGet
{
    public static void main(String[] args)
    {
        if (TestStatic.getStaticVariable() != TestStatic.DEFAULT_STATIC_VALUE)
        {
            throw new RuntimeException("Value modified");
        }
    }
}
