package com.enioka.jqm.tools;

public class CheckFilePath
{
	public CheckFilePath()
	{

	}

	public boolean IsValidFilePath(String fp)
	{
		return (fp.endsWith("/"));

	}

	public String FixFilePath(String fp)
	{
		if (IsValidFilePath(fp))
			return fp;
		else
			return fp + "/";
	}
}
