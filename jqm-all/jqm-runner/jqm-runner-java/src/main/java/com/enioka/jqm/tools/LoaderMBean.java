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

import java.util.Calendar;

/**
 * Monitoring interface for a running job instance.
 */
public interface LoaderMBean
{
    /**
     * tries to kill the job instance
     * 
     */
    void kill();

    /**
     * The name of the Job Definition that was used for this launch.
     */
    String getApplicationName();

    /**
     * Time a request was made. (request time, no startup time)
     */
    Calendar getEnqueueDate();

    /**
     * An optional tag that can be defined at enqueue time.
     */
    String getKeyword1();

    /**
     * An optional tag that can be defined at enqueue time.
     */
    String getKeyword2();

    /**
     * An optional tag that can be defined at enqueue time.
     */
    String getKeyword3();

    /**
     * An optional tag that can be defined at enqueue time.
     */
    String getModule();

    /**
     * An optional tag that can be defined at enqueue time.
     */
    String getUser();

    /**
     * An optional tag that can be defined at enqueue time.
     */
    String getSessionId();

    /**
     * The unique ID of the launch. It holds no special meaning.
     */
    Integer getId();

    /**
     * Number of seconds the job instance has been running.
     */
    Long getRunTimeSeconds();
}
