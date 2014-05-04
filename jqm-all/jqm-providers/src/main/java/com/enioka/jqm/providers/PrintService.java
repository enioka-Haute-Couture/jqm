package com.enioka.jqm.providers;

import java.io.InputStream;

import javax.print.DocFlavor;
import javax.print.PrintException;

/**
 * This is a simple wrapper around the Java Print Services.
 */
public interface PrintService
{
    /**
     * Prints a document on the designated print queue.
     * 
     * @param printQueueName
     *            the name of the queue as defined in the printer manager/CUPS. Case sensitive.
     * @param data
     *            the object to print. Will be interpreted as a document of flavor {@link DocFlavor.INPUT_STREAM} with auto flavour
     *            detection.
     * @param jobName
     *            name of the job as will be registered in the printer manager/CUPS.
     * 
     * @throws IllegalArgumentException
     *             if the printer queue cannot be found, if the queue name is null, if data is null, or if data is of the wrong flavor.
     * @throws PrintException
     *             if the job parameters are incorrect
     */
    void print(String printQueueName, String jobName, InputStream data) throws PrintException;

    /**
     * Prints a document on the designated print queue.
     * 
     * @param printQueueName
     *            the name of the queue as defined in the printer manager/CUPS. Case sensitive.
     * @param data
     *            the object to print. Will be interpreted as a document of flavor {@link DocFlavor.BYTE_ARRAY} with auto flavour detection.
     * @param jobName
     *            name of the job as will be registered in the printer manager/CUPS.
     * 
     * @throws IllegalArgumentException
     *             if the printer queue cannot be found, if the queue name is null, if data is null, or if data is of the wrong flavor.
     * @throws PrintException
     *             if the job parameters are incorrect
     */
    void print(String printQueueName, String jobName, byte[] data) throws PrintException;

    /**
     * Raw exposition of the Java Print API. This method is mainly used internally by the API. See
     * {@link #print(String, String, InputStream)} and {@link #print(String, String, byte[])} for easier to use methods.
     * 
     * @param printQueueName
     * @param data
     * @param flavor
     * @param jobName
     * 
     * @throws IllegalArgumentException
     *             if the printer queue cannot be found, if the queue name is null, if data is null, or if data is of the wrong flavor.
     * @throws PrintException
     *             if the job parameters are incorrect
     */
    void print(String printQueueName, String jobName, Object data, DocFlavor flavor) throws PrintException;
}
