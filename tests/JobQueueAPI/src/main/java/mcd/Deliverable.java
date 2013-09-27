package mcd;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


/**
 *
 * @author pierre.coppee
 */
@Entity
public class Deliverable {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	protected Integer id;
	@Column(length=1000)
    protected String fileName;
	@Column(length=1000)
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
