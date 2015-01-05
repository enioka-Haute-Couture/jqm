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

import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.api.JobBase;

public class StressFibo extends JobBase
{
    @Override
    public void start()
    {
        System.out.println("PARAMETRE FIBO 2: " + this.getParameters().get("p2"));

        Map<String, String> p = new HashMap<String, String>();

        p.put("p1", this.getParameters().get("p2"));
        p.put("p2", (Integer.parseInt(this.getParameters().get("p1")) + Integer.parseInt(this.getParameters().get("p2")) + ""));
        System.out.println("BEFORE ENQUEUE");

        if (Integer.parseInt(this.getParameters().get("p1")) <= 100)
        {
            enQueue(jm.applicationName(), null, null, null, null, null, null, null, null, p);
        }
        System.out.println("QUIT FIBO");
    }
}
