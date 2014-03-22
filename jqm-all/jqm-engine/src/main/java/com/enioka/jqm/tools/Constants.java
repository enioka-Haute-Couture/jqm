package com.enioka.jqm.tools;

/**
 * Some strings and other values are reused throughout the whole engine and are centralized here to avoid multiple redefinitions.
 */
class Constants
{
    Constants()
    {

    }

    static final String GP_JQM_CONNECTION_ALIAS = "jdbc/jqm";
    static final String GP_MAVEN_REPO_KEY = "mavenRepo";
    static final String GP_DEFAULT_CONNECTION_KEY = "defaultConnection";

    static final String API_INTERFACE = "com.enioka.jqm.api.JobManager";
    static final String API_OLD_IMPL = "com.enioka.jqm.api.JobBase";
}
