package com.enioka.jqm.client.jdbc.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.client.api.JqmClientException;
import com.enioka.jqm.client.api.JqmInvalidRequestException;
import com.enioka.jqm.client.shared.SelfDestructFileStream;
import com.enioka.jqm.client.shared.SimpleApiSecurity;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.model.GlobalParameter;

/**
 * A simple encapsulation of Java 11+ HTTP client allowing to retrieve files from remote JQM nodes.
 */
class SimpleHttpClient
{
    private static Logger jqmlogger = LoggerFactory.getLogger(SimpleHttpClient.class);

    // Input configuration
    private final Properties p;

    // Cached values
    private String protocol = null;
    private HttpClient client = null;

    SimpleHttpClient(Properties p)
    {
        this.p = p;
    }

    InputStream getFile(DbConn cnx, String url)
    {
        File file = null;
        String nameHint = null;

        // Init the client if needed. (not threadsafe, not an issue as duplicate clients are cheap)
        var client = this.getHttpClient(cnx);

        // Check the URL
        URI uri;
        try
        {
            uri = new URI(url);
        }
        catch (URISyntaxException e)
        {
            throw new JqmInvalidRequestException("Invalid URL " + url, e);
        }

        // We will download the file to a temporary location
        File destDir = new File(System.getProperty("java.io.tmpdir"));
        if (!destDir.isDirectory() && !destDir.mkdir())
        {
            throw new JqmClientException("could not create temp directory " + destDir.getAbsolutePath());
        }
        file = new File(destDir + "/" + UUID.randomUUID().toString());
        jqmlogger.trace("File will be copied into {} as {}", destDir, file.getName());

        // Auth stuff
        var authData = SimpleApiSecurity.getId(cnx);
        String encodedAuth = null;
        if (authData != null)
        {
            encodedAuth = Base64.getEncoder().encodeToString((authData.usr + ":" + authData.pass).getBytes(StandardCharsets.UTF_8));
        }

        // Create request
        var rq = HttpRequest.newBuilder().uri(uri).header("Authorization", "Basic " + encodedAuth).GET().build();

        // Run
        HttpResponse<Path> rs;
        try
        {
            rs = client.send(rq, BodyHandlers.ofFile(file.toPath()));
        }
        catch (IOException e)
        {
            throw new JqmClientException("Could not download file from JQM node at url " + url, e);
        }
        catch (InterruptedException e)
        {
            // Clear state
            Thread.currentThread().interrupt();
            throw new JqmClientException("File download interrupted", e);
        }

        // Manage result.
        if (rs.statusCode() != 200)
        {
            throw new JqmClientException(
                    "Could not retrieve file from JQM node. The file may have been purged, or the node may be unreachable. HTTP code was: "
                            + rs.statusCode());
        }

        // There may be a filename hint inside the response
        var hs = rs.headers().allValues("Content-Disposition");
        if (hs.size() == 1)
        {
            var h = hs.get(0);
            if (h.contains("filename="))
            {
                nameHint = h.split("=")[1];
            }
        }

        // Return the result as a stream
        SelfDestructFileStream res;
        try
        {
            res = new SelfDestructFileStream(file);
        }
        catch (FileNotFoundException e)
        {
            throw new JqmClientException("Could not open temporary file downloaded from URL", e);
        }
        res.nameHint = nameHint;
        return res;
    }

    private HttpClient getHttpClient(DbConn cnx)
    {
        if (this.client != null)
        {
            return this.client;
        }

        SSLContext sslContext = null;
        if (getFileProtocol(cnx).equals("https://"))
        {
            sslContext = getTlsContext();
        }

        if (sslContext == null)
        {
            this.client = HttpClient.newHttpClient();
            return this.client;
        }
        else
        {
            this.client = HttpClient.newBuilder().sslContext(sslContext).build();
            return this.client;
        }
    }

    String getFileProtocol(DbConn cnx)
    {
        if (protocol == null)
        {
            protocol = "http://";
            try
            {
                String prm = GlobalParameter.getParameter(cnx, "enableWsApiSsl", "false");
                if (Boolean.parseBoolean(prm))
                {
                    protocol = "https://";
                }
            }
            catch (NoResultException e)
            {
                protocol = "http://";
            }
        }
        return protocol;
    }

    private SSLContext getTlsContext()
    {
        SSLContext context;
        try
        {
            context = SSLContext.getInstance("TLS");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new JqmClientException("Could not get default TLS context", e);
        }

        if (p != null && p.containsKey("com.enioka.jqm.ws.truststoreFile"))
        {

            KeyStore trust = null;
            var trustoreType = p.getProperty("com.enioka.jqm.ws.truststoreType", "JKS");
            var trustoreFile = p.getProperty("com.enioka.jqm.ws.truststoreFile");

            // Create in-memory store
            try
            {
                trust = KeyStore.getInstance(trustoreType);
            }
            catch (KeyStoreException e)
            {
                throw new JqmInvalidRequestException("Specified trust store type [" + trustoreType + "] is invalid", e);
            }

            // Load the JKS file into the store
            try (InputStream trustIs = new FileInputStream(trustoreFile))
            {
                String trustp = this.p.getProperty("com.enioka.jqm.ws.truststorePass", null);
                trust.load(trustIs, (trustp == null ? null : trustp.toCharArray()));
            }
            catch (FileNotFoundException e)
            {
                throw new JqmInvalidRequestException("Trust store file [" + trustoreFile + "] cannot be found", e);
            }
            catch (Exception e)
            {
                throw new JqmInvalidRequestException("Could not load the trust store file", e);
            }

            // Load the keys from the store into the SSL context
            TrustManagerFactory tmf;
            try
            {
                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trust);
                context.init(null, tmf.getTrustManagers(), null);
            }
            catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e)
            {
                throw new JqmClientException("Could not create a custom TLS context", e);
            }
        }

        return context;
    }
}
