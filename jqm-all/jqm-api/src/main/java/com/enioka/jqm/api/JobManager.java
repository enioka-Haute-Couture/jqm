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
package com.enioka.jqm.api;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * This interface gives access to JQM engine variables and methods. It allows to retrieve the characteristics of the currently running job
 * instance, as well as creating new instances and other useful methods.<br>
 * <br>
 * It should never be instantiated but injected by the JQM engine. For the injection to take place, the payload main class should have a
 * field of type JobManager (directly or through inheritance, as well as public or private).
 * 
 */
public interface JobManager
{
    /**
     * The unique ID of the application/Job definition (a <code>JobInstance<code> is one run of a <code>JobDefinition</code>)<br>
     * <br>
     * <strong>This is not the instance ID but the definition ID</strong>. See {@link #jobInstanceID()} for the instance (and not the
     * definition) ID
     * 
     * @return The unique ID of the application/Job definition (a <code>JobInstance<code> is one run of a <code>JobDefinition</code>)
     */
    Integer jobApplicationId();

    /**
     * @return The instance ID of the JobInstance that launched this JobInstance (if any, null otherwise)
     */
    Integer parentID();

    /**
     * The unique ID of the currently running job instance.<br>
     * <br>
     * <strong>This is the instance, not the application/jobdefinition ID</strong>. Use {@link #jobApplicationId()} for the job definition
     * (and not instance) ID.
     * 
     * @return The unique ID of the currently running job instance.
     */
    Integer jobInstanceID();

    /**
     * @return true if this type of jobs (the <code>JobDefinition</code> is allowed to restart after a failure.
     */
    Boolean canBeRestarted();

    /**
     * @return the name of the running application/JobDefinition.
     */
    String applicationName();

    /**
     * Optional arbitrary user classification
     * 
     * @return the sessionID that was given at enqueue time, null if none.
     */
    String sessionID();

    /**
     * Optional arbitrary user classification
     * 
     * @return the application name that was given at enqueue time, null if none.
     */
    String application();

    /**
     * Optional arbitrary user classification
     * 
     * @return the applicative module name that was given at enqueue time, null if none.
     */
    String module();

    /**
     * Optional arbitrary user classification
     * 
     * @return the first keyword that was given at enqueue time, null if none.
     */
    String keyword1();

    /**
     * Optional arbitrary user classification
     * 
     * @return the second keyword that was given at enqueue time, null if none.
     */
    String keyword2();

    /**
     * Optional arbitrary user classification
     * 
     * @return the third keyword that was given at enqueue time, null if none.
     */
    String keyword3();
    
    /**
     * Optional arbitrary user classification
     * 
     * @return the first keyword that was given inside the job definition, null if none.
     */
    String definitionKeyword1();

    /**
     * Optional arbitrary user classification
     * 
     * @return the second keyword that was given inside the job definition, null if none.
     */
    String definitionKeyword2();

    /**
     * Optional arbitrary user classification
     * 
     * @return the third keyword that was given inside the job definition, null if none.
     */
	String definitionKeyword3();

    /**
     * Optional arbitrary user classification
     * 
     * @return the user name that was given at enqueue time, null if none.
     */
    String userName();

    /**
     * Parameters are from the Job Definition (i.e. default parameters) as well as values given at enqueue time. This is the privileged way
     * of giving parameters to a job instance.
     * 
     * @return a <code>Map<String,String></code> of all parameters (random order) with the Map key being the name of the parameter and the
     *         Map value the value of the parameter.
     */
    Map<String, String> parameters();

    /**
     * @return the JNDI alias of the default JDBC connection
     */
    String defaultConnect();

    /*
     * *************************************************************************************
     * METHODS
     */

    /**
     * Enqueues a new execution request and returns as soon as the request is posted. <br>
     * <strong>All parameters are nullable but applicationName</strong>
     * 
     * @param applicationName
     * @param user
     *            Arbitrary user classification (not used by the JQM engine, only in reporting queries and potentially in jobs themselves)
     * @param mail
     *            Give an e-mail address to send a mail at the end of run. Leave null if no mail is needed.
     * @param sessionId
     *            Arbitrary user classification (not used by the JQM engine, only in reporting queries and potentially in jobs themselves)
     * @param application
     *            Arbitrary user classification (not used by the JQM engine, only in reporting queries and potentially in jobs themselves)
     * @param module
     *            Arbitrary user classification (not used by the JQM engine, only in reporting queries and potentially in jobs themselves)
     * @param keyword1
     *            Arbitrary user classification (not used by the JQM engine, only in reporting queries and potentially in jobs themselves)
     * @param keyword2
     *            Arbitrary user classification (not used by the JQM engine, only in reporting queries and potentially in jobs themselves)
     * @param keyword3
     *            Arbitrary user classification (not used by the JQM engine, only in reporting queries and potentially in jobs themselves)
     * @param parameters
     *            <strong>nullable</strong>
     * @return the ID of the new request
     * @see #enqueueSync sync enqueue for a synchronous variant
     */
    Integer enqueue(String applicationName, String user, String mail, String sessionId, String application, String module, String keyword1,
            String keyword2, String keyword3, Map<String, String> parameters);

    /**
     * Enqueues a new execution request and waits for the execution to end.
     * 
     * @return the ID of the new request
     * @see #enqueue the synchronous variant for the description of parameters
     */
    Integer enqueueSync(String applicationName, String user, String mail, String sessionId, String application, String module,
            String keyword1, String keyword2, String keyword3, Map<String, String> parameters);

    /**
     * Messages are strings that can be retrieved during run by other applications, so that interactive human users may have a measure of
     * job progress. (typical messages highlight the job's internal steps)
     * 
     * @param message
     *            the message to send. At most 1000 characters.
     * 
     * @see #sendProgress sendProgress for sending a progress percentage (or other numeric advancement) instead of a string
     */
    void sendMsg(String message);

    /**
     * Progress is an integer that can be retrieved during run by other applications, so that interactive human users may have a measure of
     * job progress. (typically used for percents of completion)
     * 
     * @param progress
     *            the advancement
     * 
     * @see #sendProgress sendProgressfor sending a progress percentage (or other numeric advancement) instead of a string
     */
    void sendProgress(Integer progress);

    /**
     * Helper. One JDBC connection is set as the default one. This function does the JNDI request to retrieve it.
     * 
     * @return the <code>DataSource</code> to the default database.
     * @throws NamingException
     */
    DataSource getDefaultConnection() throws NamingException;

    /**
     * When a file is created and should be retrievable from the client API, the file must be referenced with this function. <br>
     * <strong>The file is moved by this function!</strong> Only call when you don't need the file any more. <br>
     * It is strongly advised to use {@link #getWorkDir()} to get a directory where to create your files.
     * 
     * @param path
     *            absolute path to the file to make available to clients
     * @param fileLabel
     *            an optional classification (only used for user queries, not the engine itself)
     * 
     * @return the deliverable unique ID
     */
    Integer addDeliverable(String path, String fileLabel) throws IOException;

    /**
     * If temp files are necessary, use this directory. The directory already exists. It is used by a single instance. It is purged at the
     * end of the run.
     * 
     * @return a File object describing a temp directory.
     */
    File getWorkDir();

    /**
     * Be a good citizen: call this function regularly. It does nothing but check if your job should be paused, killed & such. Java makes it
     * impossible to kill a thread properly, so calling this function is the only way to allow it. <br>
     * Note: this function is also called by the other functions of the API.
     */
    void yield();

    /**
     * This method will only return after the job request of given ID is completed (be it in error or correctly). It is not guaranteed to
     * return as soon as the request is over - simply sometimes after it is over.
     * 
     * @param requestId
     *            the ID as returned by {@link #enqueue(String, String, String, String, String, String, String, String, String, Map)}
     */
    void waitChild(int requestId);

    /**
     * This method will only return after all job requests that were explicitly designated as children of the current one are completed (be
     * it in error or correctly). Job requests created through
     * {@link #enqueue(String, String, String, String, String, String, String, String, String, Map)} always create the child/parent
     * relationship so this method will wait for at least the requests created through the JobManager APIs.
     */
    void waitChildren();

    /**
     * This methods checks if a job request was processed by an engine. It returns true if it has (be it with a gracious exit or a failure) <br>
     * Also see {@link #hasSucceeded} and {@link #hasFailed}: these methods also allow to check for end with the added value of getting the
     * status.
     * 
     * @param requestId
     *            the ID as returned by {@link #enqueue} and variants.
     * @return true if ended, false otherwise
     */
    boolean hasEnded(int requestId);

    /**
     * This method checks if a job request was processed by an engine and ended in success. If the job is still running, null is returned.
     * 
     * @param requestId
     *            the ID as returned by {@link #enqueue} and variants.
     * @return true if ended correctly, false if crashed, null if still waiting in queue or running.
     */
    Boolean hasSucceeded(int requestId);

    /**
     * This method checks if a job request was processed by an engine and ended in failure. If the job is still running, null is returned.
     * 
     * @param requestId
     *            the ID as returned by {@link #enqueue} and variants.
     * @return true if ended with an exception being raised (CRASHED status) or if was killed, false if ended correctly, null if still
     *         waiting in queue or running.
     */
    Boolean hasFailed(int requestId);
}
