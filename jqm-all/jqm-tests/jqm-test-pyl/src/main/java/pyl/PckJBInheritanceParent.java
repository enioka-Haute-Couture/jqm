package pyl;

import com.enioka.jqm.api.JobBase;

public class PckJBInheritanceParent extends JobBase
{
    @Override
    public void start()
    {
        System.out.println("Hello from job " + this.getApplicationName());
        doSomething();
    }

    protected void doSomething()
    {
        throw new IllegalStateException("this method should have beeen overloaded");
    }
}
