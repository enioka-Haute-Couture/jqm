package com.enioka.jqm.tools;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

import com.enioka.jqm.api.Dispatcher;

class ServletStatus extends HttpServlet
{
	private static final long serialVersionUID = 1668370491597158300L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String sid = req.getParameter("id");
		if (sid == null)
		{
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			return;
		}

		Integer id = null;
		try
		{
			id = Integer.parseInt(sid);
		} catch (Exception e)
		{
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			return;
		}

		resp.getWriter().write("" + Dispatcher.getJob(id).getState());
		resp.getWriter().close();
	}
}
