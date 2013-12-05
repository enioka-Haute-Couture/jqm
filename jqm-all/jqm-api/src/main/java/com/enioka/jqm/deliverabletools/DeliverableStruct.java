/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
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

package com.enioka.jqm.deliverabletools;

public class DeliverableStruct {

	public String filePath;
	public String hashPath;
	public String fileFamily;
	public String fileName;

	public DeliverableStruct(final String fp, final String fileName, final String hp, final String ff) {

		filePath = fp;
		hashPath = hp;
		fileFamily = ff;
		this.fileName = fileName;
	}

	public String getFilePath() {

		return filePath;
	}

	public void setFilePath(final String filePath) {

		this.filePath = filePath;
	}

	public String getHashPath() {

		return hashPath;
	}

	public void setHashPath(final String hashPath) {

		this.hashPath = hashPath;
	}

	public String getFileFamily() {

		return fileFamily;
	}

	public void setFileFamily(final String fileFamily) {

		this.fileFamily = fileFamily;
	}

	public String getFileName() {

		return fileName;
	}

	public void setFileName(final String fileName) {

		this.fileName = fileName;
	}

}
