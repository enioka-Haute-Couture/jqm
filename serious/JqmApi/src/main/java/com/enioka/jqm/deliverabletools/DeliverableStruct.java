
package com.enioka.jqm.deliverabletools;

public class DeliverableStruct {

	public String filePath;
	public String hashPath;
	public String fileFamily;
	public String fileName;

	public DeliverableStruct(String fp, String fileName, String hp, String ff) {

		filePath = fp;
		hashPath = hp;
		fileFamily = ff;
		this.fileName = fileName;
	}

	public String getFilePath() {

		return filePath;
	}

	public void setFilePath(String filePath) {

		this.filePath = filePath;
	}

	public String getHashPath() {

		return hashPath;
	}

	public void setHashPath(String hashPath) {

		this.hashPath = hashPath;
	}

	public String getFileFamily() {

		return fileFamily;
	}

	public void setFileFamily(String fileFamily) {

		this.fileFamily = fileFamily;
	}

	public String getFileName() {

		return fileName;
	}

	public void setFileName(String fileName) {

		this.fileName = fileName;
	}

}
