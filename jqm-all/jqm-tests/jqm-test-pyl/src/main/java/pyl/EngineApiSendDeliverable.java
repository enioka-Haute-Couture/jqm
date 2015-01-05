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

import java.io.FileWriter;
import java.io.PrintWriter;

import com.enioka.jqm.api.JobManager;

public class EngineApiSendDeliverable implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        String file = jm.parameters().get("filepath");
        String fileName = jm.parameters().get("fileName");
        System.out.println("FILENAME: " + fileName);
        try
        {
            PrintWriter out = new PrintWriter(new FileWriter(file + fileName));
            out.println("Hello World!");
            out.close();

            jm.addDeliverable(file + fileName, "JobGenADeliverableFamily");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
