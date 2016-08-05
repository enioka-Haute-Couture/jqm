package pyl;

public class EngineHiddenJavaClasses implements Runnable
{
    @Override
    public void run()
    {        
        try {
            this.getClass().getClassLoader().loadClass("java.math.BigInteger");
        } catch(ClassNotFoundException e) {
            throw new RuntimeException("Could not load java.math.BigInteger");
        }
    }
}
