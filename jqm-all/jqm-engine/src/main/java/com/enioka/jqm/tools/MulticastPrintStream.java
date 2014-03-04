package com.enioka.jqm.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class MulticastPrintStream extends PrintStream
{
    private static Logger jqmlogger = Logger.getLogger(MulticastPrintStream.class);

    // private PrintStream original;
    private BufferedWriter original = null;
    private Map<Thread, BufferedWriter> loggers = new HashMap<Thread, BufferedWriter>();
    private String rootLogDir;

    MulticastPrintStream(OutputStream out, String rootLogDir)
    {
        super(out);
        this.original = new BufferedWriter(new OutputStreamWriter(out));
        this.rootLogDir = rootLogDir;
    }

    synchronized void registerThread(String fileName)
    {
        try
        {
            Writer w = new FileWriter(FilenameUtils.concat(rootLogDir, fileName), true);
            loggers.put(Thread.currentThread(), new BufferedWriter(w));
        }
        catch (IOException e)
        {
            // A PrintStream is supposed to never throw IOException
            jqmlogger.warn("could not register specific logger for a thread. Stdout will be used instead.", e);
        }
    }

    synchronized void unregisterThread()
    {
        try
        {
            BufferedWriter bf = getThreadWriter();
            if (bf != null)
            {
                loggers.get(Thread.currentThread()).close();
            }
        }
        catch (IOException e)
        {
            // A PrintStream is supposed to never throw IOException
            jqmlogger.warn("could not close log file", e);
        }
        loggers.remove(Thread.currentThread());
    }

    private BufferedWriter getThreadWriter()
    {
        BufferedWriter textOut = loggers.get(Thread.currentThread());
        if (textOut == null)
        {
            textOut = original;
        }
        return textOut;
    }

    /** Check to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException
    {
        if (out == null)
            throw new IOException("Stream closed");
    }

    private void write(String s)
    {
        write(s, false);
    }

    private void write(String s, boolean newLine)
    {
        BufferedWriter textOut = getThreadWriter();
        try
        {
            synchronized (textOut)
            {
                ensureOpen();
                textOut.write(s);
                if (newLine)
                {
                    textOut.newLine();
                }
                textOut.flush();
            }
        }
        catch (InterruptedIOException x)
        {
            Thread.currentThread().interrupt();
        }
        catch (IOException x)
        {
            // don't log exceptions, it could trigger a StackOverflow
        }
    }

    // TODO: write something that's not a performance hog...
    @Override
    public void write(byte buf[], int off, int len)
    {
        write(new String(buf, off, len));
    }

    // ///////////////////////////////////////////////////////////////////
    @Override
    public void println()
    {
        write("", true);
    }

    @Override
    public void println(boolean b)
    {
        write(b ? "true" : "false", true);
    }

    @Override
    public void println(char x)
    {
        write(String.valueOf(x), true);
    }

    @Override
    public void println(int x)
    {
        write(String.valueOf(x), true);
    }

    @Override
    public void println(long x)
    {
        write(String.valueOf(x), true);
    }

    @Override
    public void println(float x)
    {
        write(String.valueOf(x), true);
    }

    @Override
    public void println(double x)
    {
        write(String.valueOf(x), true);
    }

    @Override
    public void println(char x[])
    {
        write(String.valueOf(x), true);
    }

    @Override
    public void println(String x)
    {
        write(String.valueOf(x), true);
    }

    @Override
    public void println(Object x)
    {
        write(String.valueOf(x), true);
    }

    // ///////////////////////////////////////////////////////////////////
    @Override
    public void print(boolean b)
    {
        write(b ? "true" : "false");
    }

    @Override
    public void print(char x)
    {
        write(String.valueOf(x), false);
    }

    @Override
    public void print(int x)
    {
        write(String.valueOf(x), false);
    }

    @Override
    public void print(long x)
    {
        write(String.valueOf(x), false);
    }

    @Override
    public void print(float x)
    {
        write(String.valueOf(x), false);
    }

    @Override
    public void print(double x)
    {
        write(String.valueOf(x), false);
    }

    @Override
    public void print(char x[])
    {
        write(String.valueOf(x), false);
    }

    @Override
    public void print(String x)
    {
        write(String.valueOf(x), false);
    }

    @Override
    public void print(Object x)
    {
        write(String.valueOf(x), false);
    }
}
