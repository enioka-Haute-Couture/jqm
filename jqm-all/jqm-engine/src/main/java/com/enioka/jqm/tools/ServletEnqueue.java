package com.enioka.jqm.tools;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;

public class ServletEnqueue extends HttpServlet
{
	private static final long serialVersionUID = 5632427472597258194L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.setStatus(HttpStatus.BAD_REQUEST_400);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String applicationName = req.getParameter("applicationname");
		// If authentication is used one day, replace this line by the name from the context
		String user = req.getParameter("user");

		JobDefinition jd = new JobDefinition(applicationName, user);

		jd.setModule(req.getParameter("module"));
		jd.setEmail(req.getParameter("mail"));
		jd.setModule(req.getParameter("module"));
		jd.setOther1(req.getParameter("other1"));
		jd.setOther2(req.getParameter("other2"));
		jd.setOther3(req.getParameter("other3"));
		if (req.getParameter("parentid") != null)
		{
			jd.setParentID(Integer.parseInt(req.getParameter("parentid")));
		}
		jd.setSessionID(req.getParameter("sessionid"));

		int j = 0;
		while (req.getParameter("param_" + j) != null)
		{
			j++;
			jd.addParameter(req.getParameter("param_" + j), req.getParameter("paramvalue_" + j));
		}

		int i = Dispatcher.enQueue(jd);
		resp.getWriter().write("" + i);
		resp.getWriter().close();
	}
}
