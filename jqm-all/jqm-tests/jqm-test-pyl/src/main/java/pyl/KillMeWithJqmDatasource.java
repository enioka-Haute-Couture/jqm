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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * A job that connects to the JQM database and holds a connection to it forever, trying a query every second. Used to test job crashing due
 * to a database failure. The engine should still be able to collect and persist the results of the job instance properly.
 */
public class KillMeWithJqmDatasource implements Runnable
{
    @Override
    public void run()
    {
        DataSource ds = null;
        Connection cnx = null;
        String dbName = System.getenv("DB");
        dbName = (dbName == null ? "hsqldb" : dbName);
        try
        {
            ds = (DataSource) InitialContext.doLookup("jdbc/" + dbName);
            cnx = ds.getConnection();

            while (true)
            {
                PreparedStatement ps1 = cnx.prepareStatement("SELECT * FROM HISTORY");
                ResultSet rs1 = ps1.executeQuery();
                rs1.close();
                ps1.close();
                Thread.sleep(1000);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            if (Thread.interrupted())
            {
                System.err.println("KillMeWithJqmDatasource was interrupted and will quit");
                return;
            }
        }
        catch (Exception e1)
        {
            throw new RuntimeException(e1);
        }
        finally
        {
            if (cnx != null)
            {
                try
                {
                    cnx.close();
                }
                catch (SQLException e)
                {
                    // We do not care in a test.
                }
            }
        }
    }
}
