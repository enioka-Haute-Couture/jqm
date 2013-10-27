/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Pierre COPPEE (pierre.coppee@enioka.com)
 * Contributors : Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	public void setFileFamily(final String fileFamily)
	{
		this.fileFamily = fileFamily;
	}


	public Integer getJobId() {

		return jobId;
	}


	public void setJobId(final Integer jobId) {

		this.jobId = jobId;
	}


	public String getFilePath() {

		return filePath;
	}


	public void setFilePath(final String filePath) {

		this.filePath = filePath;
	}


	public String getHashPath() {

		return HashPath;
	}


	public void setHashPath(final String hashPath) {

		HashPath = hashPath;
	}


	public String getFileName() {

		return fileName;
	}


	public void setFileName(final String fileName) {

		this.fileName = fileName;
	}

}
