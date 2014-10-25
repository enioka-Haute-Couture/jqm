/**
 * Copyright © 2013 enioka. All rights reserved
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
package com.enioka.jqm.webui.admin.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RUserDto implements Serializable
{
    private static final long serialVersionUID = -3226768067729024400L;

    private Integer id;
    private String login;
    private String newPassword;
    private String certificateThumbprint;
    private Boolean locked, internal;
    private Calendar expirationDate;
    private Calendar creationDate = Calendar.getInstance();
    private String freeText;
    private String email;

    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "role", type = Integer.class)
    private List<Integer> roles = new ArrayList<Integer>();

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getNewPassword()
    {
        return newPassword;
    }

    public void setNewPassword(String newPassword)
    {
        this.newPassword = newPassword;
    }

    public String getCertificateThumbprint()
    {
        return certificateThumbprint;
    }

    public void setCertificateThumbprint(String certificateThumbprint)
    {
        this.certificateThumbprint = certificateThumbprint;
    }

    public Boolean getLocked()
    {
        return locked;
    }

    public void setLocked(Boolean locked)
    {
        this.locked = locked;
    }

    public Calendar getExpirationDate()
    {
        return expirationDate;
    }

    public void setExpirationDate(Calendar expirationDate)
    {
        this.expirationDate = expirationDate;
    }

    public Calendar getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate)
    {
        this.creationDate = creationDate;
    }

    public String getFreeText()
    {
        return freeText;
    }

    public void setFreeText(String freeText)
    {
        this.freeText = freeText;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public List<Integer> getRoles()
    {
        return roles;
    }

    void setRoles(List<Integer> roles)
    {
        this.roles = roles;
    }

    public Boolean getInternal()
    {
        return internal;
    }

    public void setInternal(Boolean internal)
    {
        this.internal = internal;
    }
}
