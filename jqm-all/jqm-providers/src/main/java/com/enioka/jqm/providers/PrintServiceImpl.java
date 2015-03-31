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
import javax.print.attribute.standard.RequestingUserName;

class PrintServiceImpl implements PrintService
{
    PrintServiceImpl()
    {
        // This is package-private constructor.
    }

    @Override
    public void print(String printQueueName, String jobName, InputStream data) throws PrintException
    {
        print(printQueueName, jobName, data, (String) null);
    }

    @Override
    public void print(String printQueueName, String jobName, InputStream data, String endUserName) throws PrintException
    {
        print(printQueueName, jobName, data, DocFlavor.INPUT_STREAM.AUTOSENSE, endUserName);
    }

    @Override
    public void print(String printQueueName, String jobName, byte[] data) throws PrintException
    {
        print(printQueueName, jobName, data, (String) null);
    }

    @Override
    public void print(String printQueueName, String jobName, byte[] data, String endUserName) throws PrintException
    {
        print(printQueueName, jobName, data, DocFlavor.BYTE_ARRAY.AUTOSENSE, endUserName);
    }

    @Override
    public void print(String printQueueName, String jobName, Object data, DocFlavor flavor) throws PrintException
    {
        print(printQueueName, jobName, data, flavor, null);
    }

    @Override
    public void print(String printQueueName, String jobName, Object data, DocFlavor flavor, String endUserName) throws PrintException
    {
        // Arguments tests
        if (printQueueName == null || printQueueName.isEmpty())
        {
            throw new IllegalArgumentException("printQueueName must be non null and non empty");
        }
        if (data == null)
        {
            throw new IllegalArgumentException("data must be non null");
        }
        if (flavor == null)
        {
            throw new IllegalArgumentException("flavor must be non null");
        }
        if (jobName == null || jobName.isEmpty())
        {
            throw new IllegalArgumentException("job name must be non null and non empty");
        }
        if (endUserName != null && endUserName.isEmpty())
        {
            throw new IllegalArgumentException("endUserName can be null but cannot be empty is specified");
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
        if (endUserName != null && queue.isAttributeCategorySupported(RequestingUserName.class))
        {
            jobAttrs.add(new RequestingUserName(endUserName, null));
        }

        // Create payload
        Doc doc = new SimpleDoc(data, flavor, null);

        // Do it
        job.print(doc, jobAttrs);
    }
}
