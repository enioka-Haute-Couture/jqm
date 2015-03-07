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

package com.enioka.jqm.jpamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity
public class RUser implements Serializable
{
    private static final long serialVersionUID = 1234354709423603792L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(length = 100, name = "login", unique = true)
    private String login;

    @Column(length = 254, name = "password")
    private String password;

    @Column(length = 254, name = "hashSalt")
    private String hashSalt;

    private Boolean locked = false;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar expirationDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Calendar creationDate = Calendar.getInstance();

    @Column(length = 254, nullable = true)
    private String email;

    @Column(length = 254, nullable = true)
    private String freeText;

    @ManyToMany(mappedBy = "users")
    private List<RRole> roles = new ArrayList<RRole>();

    @Version
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastModified;

    private Boolean internal = false;

    public Integer getId()
    {
        return id;
    }

    void setId(Integer id)
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

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public List<RRole> getRoles()
    {
        return roles;
    }

    void setRoles(List<RRole> roles)
    {
        this.roles = roles;
    }

    public String getHashSalt()
    {
        return hashSalt;
    }

    public void setHashSalt(String hashSalt)
    {
        this.hashSalt = hashSalt;
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

    void setCreationDate(Calendar creationDate)
    {
        this.creationDate = creationDate;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getFreeText()
    {
        return freeText;
    }

    public void setFreeText(String freeText)
    {
        this.freeText = freeText;
    }

    public Boolean getInternal()
    {
        return internal;
    }

    public void setInternal(Boolean internal)
    {
        this.internal = internal;
    }

    /**
     * When the object was last modified. Read only.
     */
    public Calendar getLastModified()
    {
        return lastModified;
    }

    /**
     * See {@link #getLastModified()}
     */
    protected void setLastModified(Calendar lastModified)
    {
        this.lastModified = lastModified;
    }
}
