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
package com.enioka.api.admin;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Global parameters are shared between all nodes in the JQM cluster and control various aspects of its behaviour: to use SSL or not, which
 * Maven repositories are allowed, etc.<br>
 * Allowed keys and values are found inside the documentation.
 */
@XmlRootElement
public class GlobalParameterDto implements Serializable
{
    private static final long serialVersionUID = -4836332110468430277L;

    private Integer id;
    private String key;
    private String value;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
