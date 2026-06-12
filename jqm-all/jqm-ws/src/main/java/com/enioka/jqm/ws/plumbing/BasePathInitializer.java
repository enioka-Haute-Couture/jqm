package com.enioka.jqm.ws.plumbing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Patches the base tag in index.html at application startup to the value in envrionment variable JQM_BASE_PATH to support deployment behind
 * a reverse proxy with a path prefix.
 *
 * If the variable is absent no patching is done.
 */
@WebListener
public class BasePathInitializer implements ServletContextListener
{
    private static final Logger log = LoggerFactory.getLogger(BasePathInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        String prefix = System.getenv("JQM_BASE_PATH");
        if (prefix == null || prefix.isEmpty())
        {
            log.info("JQM_BASE_PATH not set, leaving base URL in index.html to default '/'");
            return;
        }
        log.debug("JQM_BASE_PATH set to '{}', patching base URL in index.html at this path", prefix);

        if (!prefix.startsWith("/"))
        {
            prefix = "/" + prefix;
        }
        prefix = prefix.replaceAll("/+$", "");
        String basePath = prefix + "/";

        String realPath = sce.getServletContext().getRealPath("/index.html");
        if (realPath == null)
        {
            log.error("Cannot patch index.html: getRealPath returned null");
            return;
        }

        Path indexFile = Paths.get(realPath);
        if (!Files.exists(indexFile))
        {
            log.error("Cannot patch index.html: file not found at {}", realPath);
            return;
        }

        try
        {
            String content = Files.readString(indexFile, StandardCharsets.UTF_8);
            String patched = content.replace("<base href=\"/\">", "<base href=\"" + basePath + "\">")
                    .replace("<base href=\"/\" />", "<base href=\"" + basePath + "\" />")
                    .replace("<base href=\"./\">", "<base href=\"" + basePath + "\">")
                    .replace("<base href=\"./' />", "<base href=\"" + basePath + "\" />");
            if (patched.equals(content))
            {
                log.error("index.html does not contain expected <base href=\"/\"> tag ");
                return;
            }

            Files.writeString(indexFile, patched, StandardCharsets.UTF_8);
            log.info("index.html was patched with base path {}", basePath);
        }
        catch (IOException e)
        {
            log.error("Failed to patch index.html with base path {}", basePath, e);
        }
    }
}
