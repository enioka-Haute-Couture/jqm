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
package com.enioka.jqm.tools;

import org.hibernate.dialect.HSQLDialect;

/**
 * A dialect for Hibernate + HSQLDB 2.x+ It exists only because of bug HHH-7479 which is solved but was not backported to any JPA 2.0
 * version of Hibernate.
 */
public class HSQLDialect7479 extends HSQLDialect
{
    @Override
    public String getForUpdateString()
    {
        return " for update";
    }
}
