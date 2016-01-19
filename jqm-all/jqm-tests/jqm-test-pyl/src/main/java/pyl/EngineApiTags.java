package pyl;

import com.enioka.jqm.api.JobManager;

public class EngineApiTags
{
    static JobManager jm;

    public static void main(String[] args)
    {
        if (jm.definitionKeyword1() != "keyword1" && jm.definitionKeyword2() != null && jm.definitionKeyword3() != "keyword3")
        {
        	throw new RuntimeException("could not get the job def keywords");
        }
        
        if (jm.keyword1() != "Houba" && jm.keyword2() != null && jm.keyword3() != "Meuh")
        {
        	throw new RuntimeException("could not get the enqueue keywords");
        }
    }
}