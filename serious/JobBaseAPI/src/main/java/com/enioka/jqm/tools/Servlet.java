
package com.enioka.jqm.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

@SuppressWarnings("serial")
public class Servlet extends HttpServlet {

	public Servlet() {

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String fileName = request.getParameter("file");
		File realFileName = new File("/Users/pico/Downloads/tests", fileName);
		FileInputStream fis = null;
		OutputStream out = response.getOutputStream();

		try {

			fis = new FileInputStream(realFileName);
			response.setContentType("application/octet-stream");

			IOUtils.copy(fis, out); // Copy bytes from an InputStream to an
			                        // OutputStream.

		} finally {

			IOUtils.closeQuietly(out); // Good practice
			IOUtils.closeQuietly(fis);

		}
	}
}
