package com.enioka.jqm.api;

/**
 * Represents a file created by a job instance
 * 
 */
public class Deliverable
{
	private String filePath;
	private String fileName;

	/**
	 * Construction. This will not create a new file on the file system!
	 * 
	 * @param filePath
	 * @param fileName
	 */
	public Deliverable(String filePath, String fileName)
	{
		this.filePath = filePath;
		this.fileName = fileName;
	}

	/**
	 * File Path
	 * 
	 * @return
	 */
	public String getFilePath()
	{
		return filePath;
	}

	/**
	 * The family name
	 * 
	 * @return
	 */
	public String getFileName()
	{
		return fileName;
	}
}
