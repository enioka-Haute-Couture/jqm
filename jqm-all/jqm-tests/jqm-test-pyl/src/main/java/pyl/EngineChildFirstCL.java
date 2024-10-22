package pyl;

import java.util.Hashtable;

import com.enioka.jqm.providers.UrlFactory;

public class EngineChildFirstCL
{
    public static void main(String[] args) throws Exception
    {
        var factory = new UrlFactory();
        var prms = new Hashtable<String, String>();
        prms.put("URL", "http://meuh");
        var url = factory.getObjectInstance(null, null, null, prms);

        // Normal (parent) implem should return meuh.
        // Overloaded (child) implem always returns "houba hop"
        if (url.toString().equals("http://meuh"))
        {
            throw new RuntimeException("Value was meuh, meaning parent classloader was used instead of child first");
        }
    }
}
