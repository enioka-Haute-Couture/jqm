/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Pierre COPPEE (pierre.coppee@enioka.com)
 * Contributors : Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

@SuppressWarnings("serial")
class Servlet extends HttpServlet
{
	private static Logger jqmlogger = Logger.getLogger(Servlet.class);

	public Servlet()
	{

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		String fileName = request.getParameter("file");
		File realFileName = new File(fileName);
		FileInputStream fis = null;
		OutputStream out = null;

		try
		{
			out = response.getOutputStream();
			fis = new FileInputStream(realFileName);
			response.setContentType("application/octet-stream");

			IOUtils.copy(fis, out); // Copy bytes from an InputStream to an OutputStream.

		} catch (FileNotFoundException e)
		{
			jqmlogger.warn(e);
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} catch (IOException e)
		{
			jqmlogger.warn(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally
		{
			IOUtils.closeQuietly(out); // Good practice
			IOUtils.closeQuietly(fis);
		}
	}
}
