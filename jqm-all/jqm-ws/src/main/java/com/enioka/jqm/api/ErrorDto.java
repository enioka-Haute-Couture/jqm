package com.enioka.jqm.api;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ErrorDto extends RuntimeException
{
    private static final long serialVersionUID = 6661530066660670253L;

    private String userReadableMessage, developerMessage, stacktrace;
    private Integer errorCode;

    Status httpStatus;

    public ErrorDto()
    {
        super();
    }

    public ErrorDto(String userReadableMessage, String devMessage, Integer errorCode, Status status)
    {
        this.userReadableMessage = userReadableMessage;
        this.httpStatus = status;
        this.developerMessage = devMessage;
        this.errorCode = errorCode;

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < trace.length; i++)
            pw.println("\tat " + trace[i]);
        this.stacktrace = sw.toString();
    }

    public ErrorDto(String userReadableMessage, Integer errorCode, Exception e, Status status)
    {
        this.userReadableMessage = userReadableMessage;
        this.httpStatus = status;

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        sw.toString();
        this.stacktrace = sw.toString();
        this.errorCode = errorCode;
    }

    public String getUserReadableMessage()
    {
        return userReadableMessage;
    }

    public void setUserReadableMessage(String userReadableMessage)
    {
        this.userReadableMessage = userReadableMessage;
    }

    public String getDeveloperMessage()
    {
        return developerMessage;
    }

    public void setDeveloperMessage(String developerMessage)
    {
        this.developerMessage = developerMessage;
    }

    public Integer getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode)
    {
        this.errorCode = errorCode;
    }

    public String getStacktrace()
    {
        return stacktrace;
    }

    public void setStacktrace(String stacktrace)
    {
        this.stacktrace = stacktrace;
    }
}
