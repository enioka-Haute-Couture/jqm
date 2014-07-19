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
package com.enioka.jqm.jpamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PKI implements Serializable
{
    private static final long serialVersionUID = -1830546620049033739L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(length = 100, nullable = false, unique = true)
    private String prettyName;

    @Column(length = 4000, nullable = false)
    private String pemPK;

    @Column(length = 4000, nullable = false)
    private String pemCert;

    public Integer getId()
    {
        return id;
    }

    void setId(Integer id)
    {
        this.id = id;
    }

    public String getPrettyName()
    {
        return prettyName;
    }

    public void setPrettyName(String prettyName)
    {
        this.prettyName = prettyName;
    }

    public String getPemPK()
    {
        return pemPK;
    }

    public void setPemPK(String pemPK)
    {
        this.pemPK = pemPK;
    }

    public String getPemCert()
    {
        return pemCert;
    }

    public void setPemCert(String pemCert)
    {
        this.pemCert = pemCert;
    }
}
