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
