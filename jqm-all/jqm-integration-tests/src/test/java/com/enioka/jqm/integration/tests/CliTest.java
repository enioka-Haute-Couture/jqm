package com.enioka.jqm.integration.tests;

import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.Assert;

public class CliTest extends JqmBaseTest
{
    @Test
    public void testNewParamLaunch() throws Exception
    {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("param1", "val");
        String port = System.getenv("JQM_CONSOLE_PORT");

        URL url = new URL("http://localhost:8080/client/ji");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(stringBuilder(parameters));
        out.flush();
        out.close();

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code: " + responseCode);
        Assert.assertEquals(200, responseCode);
    }

    public static String stringBuilder(Map<String, String> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet())
        {
            if (result.length() > 0) result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
