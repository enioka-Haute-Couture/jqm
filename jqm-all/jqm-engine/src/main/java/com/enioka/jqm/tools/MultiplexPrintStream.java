/**
 * Copyright © 2013 enioka. All rights reserved
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.enioka.jqm.model.GlobalParameter;

/**
 * The goal of this Stream is to provide a replacement for stdout/err in which every running job instance has its own personal flow. This is
 * basically flow multiplexing, with the multiplexing key being the caller Thread object. Used by default, can be disabled with a
 * {@link GlobalParameter}. <br>
 * Should a payload create a new thread, its stdout would go to the global log as the multiplexing key is the Thread. But is not a big deal
 * as creating threads inside an app server is not a good idea anyway.
 */
class MultiplexPrintStream extends PrintStream
{
    private static Logger jqmlogger = Logger.getLogger(MultiplexPrintStream.class);
    private static Logger alljobslogger = Logger.getLogger("alljobslogger");
    private static String ls = System.getProperty("line.separator");

    private BufferedWriter original = null;
    private boolean useCommonLogFile = false;
    private ThreadLocal<BufferedWriter> logger = new ThreadLocal<BufferedWriter>()
    {
        protected BufferedWriter initialValue()
        {
            return original;
        }
    };
    String rootLogDir;

    MultiplexPrintStream(OutputStream out, String rootLogDir, boolean alsoWriteToCommonLog)
    {
        super(out);
        this.useCommonLogFile = alsoWriteToCommonLog;
        this.original = new BufferedWriter(new OutputStreamWriter(out));
        this.rootLogDir = rootLogDir;

        File d = new File(this.rootLogDir);
        if (!d.isDirectory() && !d.mkdir())
        {
            throw new JqmInitError("could not create log dir " + this.rootLogDir);
        }
    }

    void registerThread(String fileName)
    {
        try
        {
            unregisterThread();
            Writer w = new FileWriter(FilenameUtils.concat(rootLogDir, fileName), true);
            logger.set(new BufferedWriter(w));
        }
        catch (IOException e)
        {
            // A PrintStream is supposed to never throw IOException
            jqmlogger.warn("could not register specific logger for a thread. Stdout will be used instead.", e);
        }
    }

    void unregisterThread()
    {
        try
        {
            BufferedWriter bf = logger.get();
            if (bf != original)
            {
                bf.close();
                logger.remove();
            }
        }
        catch (IOException e)
        {
            // A PrintStream is supposed to never throw IOException
            jqmlogger.warn("could not close log file", e);
        }
    }

    /** Check to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException
    {
        if (out == null)
        {
            throw new IOException("Stream closed");
        }
    }

    private void write(String s)
    {
        write(s, false);
    }

    private void write(String s, boolean newLine)
    {
        BufferedWriter textOut = logger.get();
        try
        {
            ensureOpen();
            textOut.write(s);
            if (newLine)
            {
                textOut.newLine();
            }
            textOut.flush();

            if (useCommonLogFile && textOut != original)
            {
                alljobslogger.info(s + (newLine ? ls : ""));
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
    public void write(byte[] buf, int off, int len)
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
    public void println(char[] x)
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
    public void print(char[] x)
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
