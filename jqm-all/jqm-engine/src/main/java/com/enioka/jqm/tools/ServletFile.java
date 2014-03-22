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

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.Deliverable;

/**
 * Servlet for retrieving the {@link Deliverable}s created by a job execution.
 */
@SuppressWarnings("serial")
class ServletFile extends HttpServlet
{
    private static Logger jqmlogger = Logger.getLogger(ServletFile.class);
    private EntityManager em = Helpers.getNewEm();

    public ServletFile()
    {

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        String fileRandomId = request.getParameter("file");
        FileInputStream fis = null;
        OutputStream out = null;
        Deliverable d = null;
        try
        {
            d = em.createQuery("SELECT d from Deliverable d WHERE d.randomId = :ii", Deliverable.class).setParameter("ii", fileRandomId)
                    .getSingleResult();
        }
        catch (Exception e)
        {
            jqmlogger.info("A request for an unexisting file was received", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        File f = new File(d.getFilePath());
        jqmlogger.debug("A file will be returned: " + f.getAbsolutePath());

        try
        {
            out = response.getOutputStream();
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
            // Good practice
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(fis);
        }
    }
}
