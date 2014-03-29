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
    private String fileFamily;
    private Integer id;
    private String originalName;

    /**
     * Construction. This will not create a new file on the file system!
     * 
     * @param filePath
     * @param fileName
     */
    Deliverable(String filePath, String fileFamily, Integer ID, String originalName)
    {
        this.filePath = filePath;
        this.fileFamily = fileFamily;
        this.id = ID;
        this.originalName = originalName;
    }

    /**
     * File Path on the JQM server. This is purely informational.
     */
    public String getFilePath()
    {
        return filePath;
    }

    /**
     * Optional file tag.
     * 
     * @return the tag
     */
    public String getFileFamily()
    {
        return fileFamily;
    }

    /**
     * Unique ID of the file. This is purely technical and has no meaning to end users.
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * The name of the file as it was when created by the payload.
     */
    public String getOriginalName()
    {
        return originalName;
    }
}
