package pyl;

public class PckJBInheritance extends PckJBInheritanceParent
{
    @Override
    protected void doSomething()
    {
        System.out.println("something was done inside overloaded method");
    }
}
