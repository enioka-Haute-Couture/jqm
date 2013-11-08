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

package com.enioka.jqm.api;

/**
 * A job instance queue. Job request (i.e. job instances) are put inside queues. There are different queues for different uses depending on
 * the environment.
 * 
 */
public class Queue
{
	private int id;
	private String name;
	private String description;

	/**
	 * Each queue has a unique ID
	 * @return
	 */
	public int getId()
	{
		return id;
	}

	void setId(int id)
	{
		this.id = id;
	}

	/**
	 * Name of the queue, usually descriptive.
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Queue description.
	 * @return
	 */
	public String getDescription()
	{
		return description;
	}

	void setDescription(String description)
	{
		this.description = description;
	}
}
