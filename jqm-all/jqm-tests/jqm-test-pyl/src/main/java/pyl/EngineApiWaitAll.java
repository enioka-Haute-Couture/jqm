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

package pyl;

import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.api.JobManager;

public class EngineApiWaitAll implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        String child = jm.parameters().get("child");

        if (child != null)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                // Do nothing. Just a test.
            }
        }
        else
        {
            Map<String, String> p = new HashMap<String, String>();
            p.put("child", "yep");
            jm.enqueue(jm.applicationName(), jm.userName(), null, jm.sessionID(), jm.application(), jm.module(), jm.keyword1(),
                    jm.keyword2(), jm.keyword3(), p);
            jm.enqueue(jm.applicationName(), jm.userName(), null, jm.sessionID(), jm.application(), jm.module(), jm.keyword1(),
                    jm.keyword2(), jm.keyword3(), p);
            jm.enqueue(jm.applicationName(), jm.userName(), null, jm.sessionID(), jm.application(), jm.module(), jm.keyword1(),
                    jm.keyword2(), jm.keyword3(), p);
            jm.enqueue(jm.applicationName(), jm.userName(), null, jm.sessionID(), jm.application(), jm.module(), jm.keyword1(),
                    jm.keyword2(), jm.keyword3(), p);
            jm.enqueue(jm.applicationName(), jm.userName(), null, jm.sessionID(), jm.application(), jm.module(), jm.keyword1(),
                    jm.keyword2(), jm.keyword3(), p);
            jm.waitChildren();
        }
    }
}
