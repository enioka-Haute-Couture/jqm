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

package com.enioka.jqm.client.api;

import java.util.Calendar;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Represents a progress message emitted by a running job instance.
 *
 * @see #com.enioka.jqm.model.Message for details
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Message
{
    private String textMessage;
    private Calendar creationDate;

    public Message()
    {}

    public Message(String textMessage, Calendar creationDate)
    {
        this.textMessage = textMessage;
        this.creationDate = creationDate;
    }

    public String getTextMessage()
    {
        return textMessage;
    }

    public void setTextMessage(String textMessage)
    {
        this.textMessage = textMessage;
    }

    public Calendar getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate)
    {
        this.creationDate = creationDate;
    }
}
