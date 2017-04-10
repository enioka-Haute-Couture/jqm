package com.enioka.admin;

import java.io.Closeable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.api.admin.GlobalParameterDto;
import com.enioka.api.admin.JndiObjectResourceDto;
import com.enioka.api.admin.NodeDto;
import com.enioka.api.admin.QueueDto;
import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.jdbc.NonUniqueResultException;
import com.enioka.jqm.jdbc.QueryResult;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.JndiObjectResource;
import com.enioka.jqm.jpamodel.JndiObjectResourceParameter;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;

/**
 * Set of methods to handle metadata.
 */
public class MetaService
{
    private static Logger jqmlogger = LoggerFactory.getLogger(MetaService.class);

    private static void closeQuietly(Closeable em)
    {
        try
        {
            if (em != null)
            {
                em.close();
            }
        }
        catch (Exception e)
        {
            // fail silently
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // GLOBAL DELETE
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Empty the database. <br>
     * No commit performed.
     * 
     * @param cnx
     *            database session to use. Not commited.
     * @param force
     *            set to true if you want to delete metadata even if there is still transactional data depending on it.
     */
    public static void deleteAllMeta(DbConn cnx, boolean force)
    {
        if (force)
        {
            deleteAllTransac(cnx);
        }

        cnx.runUpdate("globalprm_delete_all");
        cnx.runUpdate("dp_delete_all");
        cnx.runUpdate("jdprm_delete_all");
        cnx.runUpdate("node_delete_all");
        cnx.runUpdate("jd_delete_all");
        cnx.runUpdate("q_delete_all");
        cnx.runUpdate("jndiprm_delete_all");
        cnx.runUpdate("jndi_delete_all");
        cnx.runUpdate("pki_delete_all"); // No corresponding DTO.
        cnx.runUpdate("perm_delete_all");
        cnx.runUpdate("role_delete_all");
        cnx.runUpdate("user_delete_all");
    }

    /**
     * This method is an exception - it does not deal with metadata but transactional data. It is included here to allow easier purge of
     * metadata.<br>
     * No commit performed.
     * 
     * @param cnx
     */
    public static void deleteAllTransac(DbConn cnx)
    {
        cnx.runUpdate("deliverable_delete_all");
        cnx.runUpdate("message_delete_all");
        cnx.runUpdate("history_delete_all");
        cnx.runUpdate("jiprm_delete_all");
        cnx.runUpdate("ji_delete_all");
    }

    ///////////////////////////////////////////////////////////////////////////
    // GLOBAL PARAMETER
    ///////////////////////////////////////////////////////////////////////////

    public static void deleteGlobalParameter(DbConn cnx, int id)
    {
        QueryResult qr = cnx.runUpdate("globalprm_delete_by_id", id);
        if (qr.nbUpdated != 1)
        {
            cnx.setRollbackOnly();
            throw new JqmAdminApiUserException("no item with ID " + id);
        }
    }

    public static void upsertGlobalParameter(DbConn cnx, GlobalParameterDto dto)
    {
        if (dto == null || dto.getKey() == null || dto.getKey().isEmpty() || dto.getValue() == null || dto.getValue().isEmpty())
        {
            throw new IllegalArgumentException("invalid dto object");
        }
        GlobalParameter.setParameter(cnx, dto.getKey(), dto.getValue());
    }

    private static GlobalParameterDto mapGlobalParameter(ResultSet rs) throws SQLException
    {
        GlobalParameterDto res = new GlobalParameterDto();
        res.setId(rs.getInt(1));
        res.setKey(rs.getString(2));
        res.setValue(rs.getString(3));
        return res;
    }

    private static GlobalParameterDto getGlobalParameter(DbConn cnx, String query_key, Object key)
    {
        ResultSet rs = cnx.runSelect(query_key, key);
        try
        {
            if (!rs.next())
            {
                throw new NoResultException("The query returned zero rows when one was expected.");
            }
            GlobalParameterDto res = mapGlobalParameter(rs);
            if (rs.next())
            {
                throw new NonUniqueResultException("The query returned more than one row when one was expected");
            }

            rs.close();
            return res;
        }
        catch (SQLException e)
        {
            throw new JqmAdminApiInternalException("An error occurred while querying the database", e);
        }
    }

    public static GlobalParameterDto getGlobalParameter(DbConn cnx, String key)
    {
        return getGlobalParameter(cnx, "globalprm_select_by_key", key);
    }

    public static GlobalParameterDto getGlobalParameter(DbConn cnx, Integer key)
    {
        return getGlobalParameter(cnx, "globalprm_select_by_id", key);
    }

    public static List<GlobalParameterDto> getGlobalParameter(DbConn cnx)
    {
        ResultSet rs = cnx.runSelect("globalprm_select_all");
        List<GlobalParameterDto> res = new ArrayList<GlobalParameterDto>();
        try
        {
            while (rs.next())
            {
                res.add(mapGlobalParameter(rs));
            }
            rs.close();
        }
        catch (SQLException e)
        {
            throw new JqmAdminApiInternalException("An error occurred while querying the database", e);
        }
        return res;
    }

    ///////////////////////////////////////////////////////////////////////////
    // JNDI
    ///////////////////////////////////////////////////////////////////////////

    public static void deleteJndiObjectResource(DbConn cnx, int id)
    {
        cnx.runUpdate("jndiprm_delete_for_resource", id);
        QueryResult qr = cnx.runUpdate("globalprm_delete_by_id", id);
        if (qr.nbUpdated != 1)
        {
            cnx.setRollbackOnly();
            throw new JqmAdminApiUserException("no item with ID " + id);
        }
    }

    /**
     * Update or insert a resource. Convention is that if dto.id is null, we always insert. If non null, always update. (so we use the
     * technical key, not the functional key - because the functional key can itself be updated!<br>
     * <br>
     * This method only updates (and sets the timestamp for last update) if there are actual modifications done. Modifications are detected
     * by value comparison on all fields (except the ID, but including parameters).
     * 
     * @param cnx
     * @param dto
     */
    public static void upsertJndiObjectResource(DbConn cnx, JndiObjectResourceDto dto)
    {
        if (dto.getId() != null)
        {
            QueryResult qr = cnx.runUpdate("jndi_update_changed_by_id", dto.getAuth(), dto.getDescription(), dto.getFactory(),
                    dto.getName(), dto.getSingleton(), dto.getTemplate(), dto.getType(), dto.getId(), dto.getAuth(), dto.getDescription(),
                    dto.getFactory(), dto.getName(), dto.getSingleton(), dto.getTemplate(), dto.getType());
            if (qr.nbUpdated != 1)
            {
                jqmlogger.debug("No update was done as object either does not exist or no modifications were done");
            }

            // Sync parameters too
            for (Map.Entry<String, String> e : dto.getParameters().entrySet())
            {
                QueryResult qr2 = cnx.runUpdate("jndiprm_update_changed_by_id", e.getValue(), e.getKey(), e.getValue());
                if (qr2.nbUpdated == 0)
                {
                    cnx.runUpdate("jndiprm_insert", e.getKey(), e.getValue(), dto.getId());
                }
            }
            ResultSet rs = cnx.runSelect("jndiprm_select_all_in_jndisrc", dto.getId());
            try
            {
                while (rs.next())
                {
                    String key = rs.getString(2);
                    int id = rs.getInt(1);

                    if (!dto.getParameters().containsKey(key))
                    {
                        cnx.runUpdate("jndiprm_delete_by_id", id);
                    }
                }
                rs.close();
            }
            catch (Exception e)
            {
                throw new JqmAdminApiInternalException(e);
            }
        }
        else
        {
            JndiObjectResource.create(cnx, dto.getName(), dto.getType(), dto.getFactory(), dto.getDescription(), dto.getSingleton(),
                    dto.getParameters());
        }
    }

    private static JndiObjectResourceDto mapJndiObjectResource(ResultSet rs) throws SQLException
    {
        JndiObjectResourceDto res = new JndiObjectResourceDto();
        res.setId(rs.getInt(1));
        res.setName(rs.getString(2));
        res.setAuth(rs.getString(3));
        res.setType(rs.getString(4));
        res.setFactory(rs.getString(5));
        res.setDescription(rs.getString(6));
        res.setTemplate(rs.getString(7));
        res.setSingleton(rs.getBoolean(8));

        return res;
    }

    private static JndiObjectResourceDto getJndiObjectResource(DbConn cnx, String query_key, Object key)
    {
        ResultSet rs = cnx.runSelect(query_key, key);
        try
        {
            if (!rs.next())
            {
                throw new NoResultException("The query returned zero rows when one was expected.");
            }
            JndiObjectResourceDto res = mapJndiObjectResource(rs);
            if (rs.next())
            {
                throw new NonUniqueResultException("The query returned more than one row when one was expected");
            }

            rs.close();
            return res;
        }
        catch (SQLException e)
        {
            throw new JqmAdminApiInternalException("An error occurred while querying the database", e);
        }
    }

    public static JndiObjectResourceDto getJndiObjectResource(DbConn cnx, String key)
    {
        return getJndiObjectResource(cnx, "globalprm_select_by_key", key);
    }

    public static JndiObjectResourceDto getJndiObjectResource(DbConn cnx, Integer key)
    {
        return getJndiObjectResource(cnx, "globalprm_select_by_id", key);
    }

    public static List<JndiObjectResourceDto> getJndiObjectResource(DbConn cnx)
    {
        ResultSet rs = cnx.runSelect("jndi_select_all");
        List<JndiObjectResourceDto> res = new ArrayList<JndiObjectResourceDto>();
        try
        {
            while (rs.next())
            {
                res.add(mapJndiObjectResource(rs));
            }
            rs.close();
        }
        catch (SQLException e)
        {
            throw new JqmAdminApiInternalException("An error occurred while querying the database", e);
        }
        return res;
    }

    ///////////////////////////////////////////////////////////////////////////
    // JOB DEF
    ///////////////////////////////////////////////////////////////////////////

    public static void deleteJobDef(DbConn cnx, int id)
    {
        int countRunning = cnx.runSelectSingle("ji_select_count_by_jd", Integer.class, id);
        if (countRunning > 0)
        {
            throw new JqmAdminApiUserException(
                    "cannot delete a job definition with running instances. Disable it, wait for the end of all running instances, then retry.");
        }

        cnx.runUpdate("jdprm_delete_all_for_jd", id);
        QueryResult qr = cnx.runUpdate("jd_delete_by_id", id);
        if (qr.nbUpdated != 1)
        {
            cnx.setRollbackOnly();
            throw new JqmAdminApiUserException("no item with ID " + id);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // NODE
    ///////////////////////////////////////////////////////////////////////////

    public static void deleteNode(DbConn cnx, int id)
    {
        int countRunning = cnx.runSelectSingle("ji_select_count_by_node", Integer.class, id);
        if (countRunning > 0)
        {
            cnx.setRollbackOnly();
            throw new JqmAdminApiUserException(
                    "cannot delete a node with running instances. Disable it, wait for the end of all running instances, then retry.");
        }

        try
        {
            Node n = Node.select_single(cnx, "node_select_by_id", id);
            Calendar limit = Calendar.getInstance();
            limit.add(Calendar.MINUTE, -10);
            if (n.getLastSeenAlive() != null && n.getLastSeenAlive().after(limit))
            {
                cnx.setRollbackOnly();
                throw new JqmAdminApiUserException(
                        "Can only remove a node either properly shut down or that has crashed more than 10 minutes ago.");
            }
        }
        catch (NoResultException e)
        {
            throw new JqmAdminApiUserException("no item with ID " + id);
        }

        cnx.runUpdate("dp_delete_for_node", id);
        QueryResult qr = cnx.runUpdate("node_delete_by_id", id);
        if (qr.nbUpdated != 1)
        {
            cnx.setRollbackOnly();
            throw new JqmAdminApiUserException("no item with ID " + id);
        }
    }

    static NodeDto mapNode(ResultSet rs, int colShift)
    {
        try
        {
            NodeDto tmp = new NodeDto();

            tmp.setId(rs.getInt(1 + colShift));

            tmp.setOutputDirectory(rs.getString(2 + colShift));
            tmp.setDns(rs.getString(3 + colShift));
            tmp.setEnabled(rs.getBoolean(4 + colShift));
            tmp.setJmxRegistryPort(rs.getInt(6 + colShift));
            tmp.setJmxServerPort(rs.getInt(7 + colShift));
            tmp.setLoadApiAdmin(rs.getBoolean(8 + colShift));
            tmp.setLoapApiSimple(rs.getBoolean(9 + colShift));
            tmp.setLoapApiSimple(rs.getBoolean(10 + colShift));
            tmp.setName(rs.getString(11 + colShift));
            tmp.setPort(rs.getInt(12 + colShift));
            tmp.setJobRepoDirectory(rs.getString(13 + colShift));
            tmp.setRootLogLevel(rs.getString(14 + colShift));
            tmp.setStop(rs.getBoolean(15 + colShift));
            tmp.setTmpDirectory(rs.getString(16 + colShift));

            Calendar c = null;
            if (rs.getTimestamp(17 + colShift) != null)
            {
                c = Calendar.getInstance();
                c.setTimeInMillis(rs.getTimestamp(17 + colShift).getTime());
            }
            tmp.setLastSeenAlive(c);

            return tmp;
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
    }

    public static List<NodeDto> getNodes(DbConn cnx)
    {
        List<NodeDto> res = new ArrayList<NodeDto>();
        try
        {
            ResultSet rs = cnx.runSelect("node_select_all");
            while (rs.next())
            {
                res.add(mapNode(rs, 0));
            }
            rs.close();
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static NodeDto getNode(DbConn cnx, int id)
    {
        ResultSet rs = null;
        try
        {
            rs = cnx.runSelect("node_select_by_id");
            if (!rs.next())
            {
                throw new JqmAdminApiUserException("no result");
            }

            return mapNode(rs, 0);
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    public static void upsertNode(DbConn cnx, NodeDto dto)
    {

    }

    ///////////////////////////////////////////////////////////////////////////
    // QUEUE
    ///////////////////////////////////////////////////////////////////////////

    public static void deleteQueue(DbConn cnx, int id)
    {
        int countRunning = cnx.runSelectSingle("ji_select_count_by_queue", Integer.class, id);
        if (countRunning > 0)
        {
            cnx.setRollbackOnly();
            throw new JqmAdminApiUserException(
                    "cannot delete a queue with running instances. Disable it, wait for the end of all running instances, then retry.");
        }

        cnx.runUpdate("dp_delete_for_queue", id);
        QueryResult qr = cnx.runUpdate("q_delete_by_id", id);
        if (qr.nbUpdated != 1)
        {
            cnx.setRollbackOnly();
            throw new JqmAdminApiUserException("no item with ID " + id);
        }
    }

    private static QueueDto mapQueue(ResultSet rs, int colShift)
    {
        try
        {
            QueueDto tmp = new QueueDto();

            tmp.setId(rs.getInt(1 + colShift));
            tmp.setDefaultQueue(rs.getBoolean(2 + colShift));
            tmp.setDescription(rs.getString(3 + colShift));
            tmp.setName(rs.getString(4 + colShift));

            return tmp;
        }
        catch (SQLException e)
        {
            throw new JqmAdminApiInternalException(e);
        }
    }

    public static List<QueueDto> getQueues(DbConn cnx)
    {
        List<QueueDto> res = new ArrayList<QueueDto>();
        try
        {
            ResultSet rs = cnx.runSelect("q_select_all");
            while (rs.next())
            {
                res.add(mapQueue(rs, 0));
            }
            rs.close();
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static QueueDto getQueue(DbConn cnx, int id)
    {
        ResultSet rs = null;
        try
        {
            rs = cnx.runSelect("q_select_by_id");
            if (!rs.next())
            {
                throw new JqmAdminApiUserException("no result");
            }

            return mapQueue(rs, 0);
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    public static void upsertQueue(DbConn cnx, QueueDto dto)
    {
        if (dto.getId() != null)
        {
            cnx.runUpdate("q_update_changed_by_id", dto.isDefaultQueue(), dto.getDescription(), dto.getName(), dto.getId(),
                    dto.isDefaultQueue(), dto.getDescription(), dto.getName());
        }
        else
        {
            Queue.create(cnx, dto.getName(), dto.getDescription(), dto.isDefaultQueue());
        }
    }

    public static void syncQueues(DbConn cnx, List<QueueDto> dtos)
    {
        for (QueueDto existing : getQueues(cnx))
        {
            boolean foundInNewSet = false;
            for (QueueDto newdto : dtos)
            {
                if (newdto.getId() != null && newdto.getId().equals(existing.getId()))
                {
                    foundInNewSet = true;
                    break;
                }
            }

            if (!foundInNewSet)
            {
                deleteQueue(cnx, existing.getId());
            }
        }

        for (QueueDto dto : dtos)
        {
            upsertQueue(cnx, dto);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // DEPLOYMENT PARAMETER
    ///////////////////////////////////////////////////////////////////////////

    public static void deleteQueueMapping(DbConn cnx, int id)
    {
        QueryResult qr = cnx.runUpdate("dp_delete_by_id", id);
        if (qr.nbUpdated != 1)
        {
            cnx.setRollbackOnly();
            throw new JqmAdminApiUserException("no item with ID " + id);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // ROLE
    ///////////////////////////////////////////////////////////////////////////

    public static void deleteRole(DbConn cnx, int id, boolean force)
    {
        if (force)
        {
            cnx.runUpdate("user_remove_role", id);
        }
        else
        {
            int userUsingRole = cnx.runSelectSingle("user_select_count_using_role", Integer.class, id);
            if (userUsingRole > 0)
            {
                cnx.setRollbackOnly();
                throw new JqmAdminApiUserException(
                        "cannot delete a role currently attributed to a user. Remove role attribution of use force parameter.");
            }
        }

        QueryResult qr = cnx.runUpdate("role_delete_by_id", id);
        if (qr.nbUpdated != 1)
        {
            cnx.setRollbackOnly();
            throw new JqmAdminApiUserException("no item with ID " + id);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // USER
    ///////////////////////////////////////////////////////////////////////////

    public static void deleteUser(DbConn cnx, int id)
    {
        QueryResult qr = cnx.runUpdate("user_delete_by_id", id);
        if (qr.nbUpdated != 1)
        {
            cnx.setRollbackOnly();
            throw new JqmAdminApiUserException("no item with ID " + id);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // UPSERT
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // SELECT
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////
    // Global parameter

    ///////////////////////////////////
    // JNDI

}
