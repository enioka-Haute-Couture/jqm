package com.enioka.api.admin;

import java.io.Serializable;

public class VersionDto implements Serializable
{
    private String mavenVersion;

    public String getMavenVersion()
    {
        return mavenVersion;
    }

    public void setMavenVersion(String version)
    {
        this.mavenVersion = version;
    }

}
