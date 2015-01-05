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

package pyl;

import com.enioka.jqm.api.JobBase;

/**
 * Simple call to getParameters and check value is as expected.
 */
public class JobBaseGetParam extends JobBase
{
    @Override
    public void start()
    {
        if ((!this.getParameters().containsValue("argument1")) || (!this.getParameters().containsValue("argument2")))
        {
            throw new RuntimeException("arguments did not contain expected values");
        }
    }
}
