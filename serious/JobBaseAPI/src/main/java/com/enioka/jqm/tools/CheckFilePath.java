package com.enioka.jqm.tools;

public class CheckFilePath
{
	public CheckFilePath()
	{

	}

	public boolean IsValidFilePath(String fp)
	{
		Integer length = fp.length();
		return (fp.charAt(length - 1) == '/');

	}

	public String FixFilePath(String fp)
	{
		if (IsValidFilePath(fp))
			return fp;
		else
			return fp + "/";
	}
}
