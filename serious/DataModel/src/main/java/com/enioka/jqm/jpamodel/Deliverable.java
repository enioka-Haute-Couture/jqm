package com.enioka.jqm.jpamodel;

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
    protected String filePath;
	@Column(length=1000)
    protected String fileName;
	@Column(length=1000)
    protected String fileFamily;
	@Column
	private Integer jobId;
	private String HashPath;



    public String getFileFamily() {
        return fileFamily;
    }

	/**
	 * @param fileFamily the fileFamily to set
	 */
	public void setFileFamily(String fileFamily)
	{
		this.fileFamily = fileFamily;
	}


    public Integer getJobId() {

    	return jobId;
    }


    public void setJobId(Integer jobId) {

    	this.jobId = jobId;
    }


    public String getFilePath() {

    	return filePath;
    }


    public void setFilePath(String filePath) {

    	this.filePath = filePath;
    }


    public String getHashPath() {

    	return HashPath;
    }


    public void setHashPath(String hashPath) {

    	HashPath = hashPath;
    }


    public String getFileName() {

    	return fileName;
    }


    public void setFileName(String fileName) {

    	this.fileName = fileName;
    }

}
