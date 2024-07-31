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
package com.enioka.jqm.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Instruction;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.shared.threads.BaseSimplePoller;

/**
 * The internal poller is responsible for doing all the repetitive tasks of an engine (excluding polling queues). Namely: check if
 * {@link Node#isStop()} has become true (stop order) and update {@link Node#setLastSeenAlive(java.util.Calendar)} to make visible to the
 * whole cluster that the engine is still alive and that no other engine should start with the same node name.
 */
class InternalPoller extends BaseSimplePoller
{
    private static Logger jqmlogger = LoggerFactory.getLogger(InternalPoller.class);
    private JqmEngine engine = null;
    private Node node = null;

    InternalPoller(JqmEngine e)
    {
        this.engine = e;

        // Get configuration data
        this.node = this.engine.getNode();
    }

    @Override
    protected long getPeriod()
    {
        try (var cnx = DbManager.getDb().getConn())
        {
            return Long.parseLong(GlobalParameter.getParameter(cnx, "internalPollingPeriodMs", "60000"));
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not retrieve poller period: " + e.getMessage());
            throw new RuntimeException("Could not retrieve poller period", e);
        }
    }

    @Override
    public void pollingLoopWork()
    {
        try (DbConn cnx = Helpers.getNewDbSession())
        {
            // Check if stop order
            try
            {
                node = Node.select_single(cnx, "node_select_by_id", node.getId());
            }
            catch (NoResultException e)
            {
                node = null;
            }
            if (node == null || node.isStop())
            {
                jqmlogger.info("Node has received a stop order from the database or was removed from the database");
                jqmlogger.trace("At stop order time, there are " + this.engine.getCurrentlyRunningJobCount() + " jobs running in the node");
                this.run = false;
                this.engine.stop();
                return;
            }

            // Engine handler is allowed to do changes on configuration changes.
            if (this.engine.getHandler() != null)
            {
                this.engine.getHandler().onConfigurationChanged(node);
            }

            // I am alive
            cnx.runUpdate("node_update_alive_by_id", node.getId());
            cnx.commit();

            // Have queue bindings changed, or is engine disabled?
            this.engine.syncPollers(cnx, node);

            // Should job instances be killed or changed priorities?
            try
            {
                ResultSet rs = cnx.runSelect("ji_select_instructions_by_node", node.getId());
                while (rs.next())
                {
                    Integer jiid = rs.getInt(1);
                    String instr = rs.getString(2);
                    Instruction instruction;
                    try
                    {
                        instruction = Instruction.valueOf(instr);
                    }
                    catch (IllegalArgumentException ex2)
                    {
                        jqmlogger.warn("An unknown instruction was found and is ignored: " + instr);
                        continue;
                    }

                    this.engine.getRunningJobInstanceManager().handleInstruction(jiid, instruction);
                }
            }
            catch (SQLException e)
            {
                throw new DatabaseException(e);
            }

            // All engine pollings done!
        }
        catch (RuntimeException e)
        {
            if (Helpers.testDbFailure(e))
            {
                jqmlogger.error("connection to database lost - stopping internal poller");
                jqmlogger.trace("connection error was:", e.getCause());
                run = false;
                this.engine.startDbRestarter();
                return;
            }
            else
            {
                throw e;
            }
        }

        jqmlogger.info("End of the internal poller");
    }
}
