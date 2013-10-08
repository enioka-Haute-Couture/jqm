
package com.enioka.jqm.temp;

public class DeliverableStruct {

	public String filePath;
	public String hashPath;
	public String fileFamily;

	public DeliverableStruct(String fp, String hp, String ff) {

		filePath = fp;
		hashPath = hp;
		fileFamily = ff;
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

}
