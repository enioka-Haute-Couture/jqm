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
