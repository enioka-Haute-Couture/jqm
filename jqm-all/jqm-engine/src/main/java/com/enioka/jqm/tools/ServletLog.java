/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enioka.jqm.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

/**
 * Servlet for retrieving the {@link Deliverable}s created by a job execution.
 */
@SuppressWarnings("serial")
class ServletLog extends HttpServlet
{
    private static Logger jqmlogger = Logger.getLogger(ServletLog.class);
    private String logRoot = "";

    public ServletLog()
    {
        RollingFileAppender a = (RollingFileAppender) Logger.getRootLogger().getAppender("rollingfile");
        logRoot = FilenameUtils.getFullPath(a.getFile());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        String outId = request.getParameter("out");
        String errId = request.getParameter("err");
        String id = outId;
        FileInputStream fis = null;
        OutputStream out = null;
        String logEnd = ".stdout.log";

        if (errId != null)
        {
            logEnd = ".stderr.log";
            id = errId;
        }

        try
        {
            out = response.getOutputStream();

            File f = new File(FilenameUtils.concat(logRoot, StringUtils.leftPad("" + id, 10, "0") + logEnd));
            fis = new FileInputStream(f);

            response.setContentType("application/octet-stream");

            // Copy bytes from an InputStream to an OutputStream.
            IOUtils.copy(fis, out);
        }
        catch (FileNotFoundException e)
        {
            jqmlogger.warn(e);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
        catch (IOException e)
        {
            jqmlogger.warn(e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally
        {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(fis);
        }
    }
}
