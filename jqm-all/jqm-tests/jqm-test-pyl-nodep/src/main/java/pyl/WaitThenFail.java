package pyl;

public class WaitThenFail
{
    public static void main(String[] args)
    {
        int ms = 10000;
        if (args.length == 1)
        {
            ms = Integer.parseInt(args[0]);
        }

        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Job interrupted before failing", e);
        }

        throw new RuntimeException("Job failed on purpose after waiting");
    }
}
