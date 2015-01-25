package pyl;

public class Wait
{
    public static void main(String[] args)
    {
        int ms = 10000;
        if (args.length == 1)
        {
            ms = Integer.parseInt(args[0]);
        }
        System.out.println("Starting to wait for " + ms + "ms.");

        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            // this is a damn test
        }
        System.out.println("Done waiting");
    }
}
