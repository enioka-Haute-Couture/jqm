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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

@Entity
public class RRole implements Serializable
{
    private static final long serialVersionUID = 1234354709423603792L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(length = 100, name = "name", nullable = false, unique = true)
    private String name;

    @Column(length = 254, name = "description", nullable = false)
    private String description;

    @ManyToMany()
    private List<RUser> users = new ArrayList<RUser>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "role")
    private List<RPermission> permissions = new ArrayList<RPermission>();

    public void addPermission(String perm)
    {
        String np = perm.toLowerCase();
        for (RPermission p : this.getPermissions())
        {
            if (p.getName().equals(np))
            {
                return;
            }
        }
        RPermission nrp = new RPermission();
        nrp.setName(np);
        nrp.setRole(this);
        this.getPermissions().add(nrp);
    }

    public Integer getId()
    {
        return id;
    }

    void setId(Integer id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<RUser> getUsers()
    {
        return users;
    }

    void setUsers(List<RUser> users)
    {
        this.users = users;
    }

    public List<RPermission> getPermissions()
    {
        return permissions;
    }

    void setPermissions(List<RPermission> permissions)
    {
        this.permissions = permissions;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
