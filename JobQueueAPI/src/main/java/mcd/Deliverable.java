package mcd;


/**
 *
 * @author pierre.coppee
 */
public class Deliverable {

    protected String fileName;
    protected String fileFamily;


    public String getFileName() {
        return fileName;
    }

    public String getFileFamily() {
        return fileFamily;
    }

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	/**
	 * @param fileFamily the fileFamily to set
	 */
	public void setFileFamily(String fileFamily)
	{
		this.fileFamily = fileFamily;
	}

}
