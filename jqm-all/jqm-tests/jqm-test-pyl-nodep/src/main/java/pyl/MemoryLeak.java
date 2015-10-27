package pyl;

import java.util.ArrayList;
import java.util.List;

public class MemoryLeak
{
    public static void main(String[] args)
    {
        final List<byte[]> segments = new ArrayList<byte[]>(64000);
        final int size = 6400000;

        for (int i = 0; i < Integer.MAX_VALUE; i++)
        {
            segments.add(new byte[size * i]);
        }
    }
}
