package pyl;

import java.io.File;

import com.enioka.jqm.api.JobManager;

public class EngineApiExpectInputFile implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        String inputFile = jm.getWorkDir() + "/../../outputfiles/" + jm.applicationName() + "/" + jm.jobInstanceID() + "/SuperFile";
        File f = new File(inputFile);
        if (!f.exists())
        {
            throw new RuntimeException("File not found: " + inputFile);
        }
    }
}
