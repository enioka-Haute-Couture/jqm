package pyl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.enioka.jqm.api.JobManager;

public class EngineApiTmpDir implements Runnable
{
    // This will be injected by the JQM engine - it could be named anything
    JobManager jm;

    @Override
    public void run()
    {
        // Working with a temp directory
        File workDir = jm.getWorkDir();
        System.out.println("Work dir is " + workDir.getAbsolutePath());

        // Creating a temp file that should be removed
        PrintWriter writer;
        File dest = new File(workDir, "marsu.txt");
        try
        {
            writer = new PrintWriter(dest, "UTF-8");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        writer.println("The first line");
        writer.println("The second line");
        writer.close();
    }
}
