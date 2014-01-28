package com.enioka.jqm.tools;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;

class ServletEnqueue extends HttpServlet
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

        JobRequest jd = new JobRequest(applicationName, user);

        jd.setModule(req.getParameter("module"));
        jd.setEmail(req.getParameter("mail"));
        jd.setModule(req.getParameter("module"));
        jd.setKeyword1(req.getParameter("other1"));
        jd.setKeyword2(req.getParameter("other2"));
        jd.setKeyword3(req.getParameter("other3"));
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

        int i = JqmClientFactory.getClient().enqueue(jd);
        resp.getWriter().write("" + i);
        resp.getWriter().close();
    }
}
