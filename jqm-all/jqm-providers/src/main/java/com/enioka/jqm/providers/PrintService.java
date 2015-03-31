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
    void print(String printQueueName, String jobName, InputStream data, String endUserName) throws PrintException;

    /**
     * Same as {@link #print(String, String, InputStream, String)} with a byte array instead of a stream.
     */
    void print(String printQueueName, String jobName, byte[] data, String endUserName) throws PrintException;

    /**
     * See {@link #print(String, String, InputStream, String)} (with a null endUserName)
     */
    void print(String printQueueName, String jobName, InputStream data) throws PrintException;

    /**
     * See {@link #print(String, String, InputStream, String)} (with a null endUserName)
     */
    void print(String printQueueName, String jobName, byte[] data) throws PrintException;

    /**
     * Raw method. Same as {@link #print(String, String, Object, DocFlavor, String)} with a null last parameter.
     */
    void print(String printQueueName, String jobName, Object data, DocFlavor flavor) throws PrintException;

    /**
     * Raw exposition of the Java Print API. This method is mainly used internally by the API. See
     * {@link #print(String, String, InputStream)} and {@link #print(String, String, byte[])} for easier to use methods.
     * 
     * @param printQueueName
     * @param data
     * @param flavor
     * @param jobName
     * @param endUserName
     * 
     * @throws IllegalArgumentException
     *             if the printer queue cannot be found, if the queue name is null, if data is null, or if data is of the wrong flavor.
     * @throws PrintException
     *             if the job parameters are incorrect
     */
    void print(String printQueueName, String jobName, Object data, DocFlavor flavor, String endUserName) throws PrintException;
}
