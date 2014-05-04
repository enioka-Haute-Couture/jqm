package com.enioka.jqm.providers;

import java.io.InputStream;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.PrinterName;

class PrintServiceImpl implements PrintService
{
    PrintServiceImpl()
    {
        // This is package-private constructor.
    }

    @Override
    public void print(String printQueueName, String jobName, InputStream data) throws PrintException
    {
        print(printQueueName, "houba", data, DocFlavor.INPUT_STREAM.AUTOSENSE);
    }

    @Override
    public void print(String printQueueName, String jobName, byte[] data) throws PrintException
    {
        print(printQueueName, "houba", data, DocFlavor.BYTE_ARRAY.AUTOSENSE);
    }

    @Override
    public void print(String printQueueName, String jobName, Object data, DocFlavor flavor) throws PrintException
    {
        // Arguments tests
        if (printQueueName == null)
        {
            throw new IllegalArgumentException("printQueueName must be non null");
        }
        if (data == null)
        {
            throw new IllegalArgumentException("data must be non null");
        }
        if (flavor == null)
        {
            throw new IllegalArgumentException("flavor must be non null");
        }
        if (jobName == null)
        {
            throw new IllegalArgumentException("job name must be non null");
        }

        // Find the queue
        AttributeSet set = new HashPrintServiceAttributeSet();
        set.add(new PrinterName(printQueueName, null));
        javax.print.PrintService[] services = PrintServiceLookup.lookupPrintServices(null, set);

        if (services.length == 0 || services[0] == null)
        {
            throw new IllegalArgumentException("There is no printer queue defined with name " + printQueueName
                    + " supporting document flavour " + flavor.toString());
        }
        javax.print.PrintService queue = services[0];

        // Create job
        DocPrintJob job = queue.createPrintJob();
        PrintRequestAttributeSet jobAttrs = new HashPrintRequestAttributeSet();
        jobAttrs.add(new JobName(jobName, null));

        // Create payload
        Doc doc = new SimpleDoc(data, flavor, null);

        // Do it
        job.print(doc, jobAttrs);
    }
}
