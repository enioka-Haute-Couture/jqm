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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * JPA persistence class for storing the default parameters of a {@link JobDef}, i.e. key/value pairs that should be present for all
 * instances created from a JobDef (and may be overloaded).<br>
 * When a {@link JobDef} is instantiated, {@link RuntimeParameter}s are created from {@link JobDefParameter}s as well as parameters
 * specified inside the execution request a,d associated to the {@link JobInstance}. Therefore, this table is purely metadata and is never
 * used in TP processing.
 */
@Entity
@Table(name = "JobDefParameter")
public class JobDefParameter implements Serializable
{
    private static final long serialVersionUID = -5308516206913425230L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false, length = 50, name = "KEYNAME")
    private String key;

    @Column(nullable = false, length = 1000, name = "VALUE")
    private String value;

    /**
     * The name of the parameter.<br>
     * Max length is 50.
     */
    public String getKey()
    {
        return key;
    }

    /**
     * See {@link #getKey()}
     */
    public void setKey(final String key)
    {
        this.key = key;
    }

    /**
     * Value of the parameter.<br>
     * Max length is 1000.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * See {@link #getValue()}
     */
    public void setValue(final String value)
    {
        this.value = value;
    }

    /**
     * A technical ID without special meaning.
     */
    public Integer getId()
    {
        return id;
    }
}
