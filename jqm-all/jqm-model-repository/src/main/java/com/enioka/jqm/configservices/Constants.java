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
package com.enioka.jqm.configservices;

// TODO: kill.
/**
 * Some strings and other values are reused throughout the whole engine and are centralized here to avoid multiple redefinitions.
 */
public class Constants
{
    private Constants()
    {
        // Helper class.
    }

    static final String GP_JQM_CONNECTION_ALIAS = "jdbc/jqm";
    static final String GP_MAVEN_REPO_KEY = "mavenRepo";
    static final String GP_DEFAULT_CONNECTION_KEY = "defaultConnection";

    static final String API_INTERFACE = "com.enioka.jqm.api.JobManager";
    static final String API_OLD_IMPL = "com.enioka.jqm.api.JobBase";
}
