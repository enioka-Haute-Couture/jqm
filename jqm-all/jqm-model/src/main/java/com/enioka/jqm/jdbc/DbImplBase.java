package com.enioka.jqm.jdbc;

import java.util.HashMap;
import java.util.Map;

public class DbImplBase
{
    /**
     *  Key convention is: entity_verb_descr<br>
     *  Placeholders are:
     *  <ul>
     *      <li>1: table name prefix (can contain schema information)</li>
     *  </ul>
     */
    static Map<String, String> queries = new HashMap<String, String>();
    
    static {
        // NODE
        queries.put("node_insert", "INSERT INTO NODE(DLREPO, DNS, ENABLED, EXPORTREPO, JMXREGISTRYPORT, JMXSERVERPORT, "
                + "LOADAPIADMIN, LOADAPICLIENT, LOAPAPISIMPLE, NODENAME, PORT, REPO, ROOTLOGLEVEL, STOP, TMPDIRECTORY) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        queries.put("node_update_all_enable_ws", "UPDATE NODE SET LOADAPISIMPLE=1, LOADAPICLIENT=1, LOADAPIADMIN=1, DNS='0.0.0.0'");
        queries.put("node_update_all_disable_ws", "UPDATE NODE SET LOADAPICLIENT=0, LOADAPIADMIN=0");
        queries.put("node_update_enabled_by_id", "UPDATE NODE SET ENABLED=? WHERE ID=?");
        queries.put("node_update_port_by_id", "UPDATE NODE SET PORT=? WHERE ID=?");
        queries.put("node_update_alive_by_id", "UPDATE NODE SET LASTSEENALIVE=CURRENT_TIMESTAMP WHERE ID=?");
        queries.put("node_update_has_stopped_by_id", "UPDATE NODE SET LASTSEENALIVE=NULL, STOP=0 WHERE ID=?");
        queries.put("node_update_stop_by_id", "UPDATE NODE SET STOP=1 WHERE ID=?");
        queries.put("node_delete_all", "DELETE FROM NODE");
        queries.put("node_update_all_log_level", "UPDATE NODE SET ROOTLOGLEVEL=?");
        queries.put("node_select_all", "SELECT ID, DLREPO, DNS, ENABLED, EXPORTREPO, JMXREGISTRYPORT, JMXSERVERPORT, "
                + "LOADAPIADMIN, LOADAPICLIENT, LOAPAPISIMPLE, NODENAME, PORT, REPO, ROOTLOGLEVEL, STOP, TMPDIRECTORY, LASTSEENALIVE "
                + "FROM NODE");
        queries.put("node_select_by_key", queries.get("node_select_all") + " WHERE NODENAME=?");
        queries.put("node_select_by_id", queries.get("node_select_all") + " WHERE ID=?");
        queries.put("node_select_connectdata_by_key", "SELECT DNS, PORT FROM NODE WHERE NODENAME=?");
        
        // QUEUE
        queries.put("q_insert", "INSERT INTO QUEUE(DEFAULTQUEUE, DESCRIPTION, NAME) VALUES(?, ?, ?)");
        queries.put("q_update_default_none", "UPDATE QUEUE SET DEFAULTQUEUE=0");
        queries.put("q_update_default_by_id", "UPDATE QUEUE SET DEFAULTQUEUE=1 WHERE ID=?");
        queries.put("q_update_all_fields_by_id", "UPDATE QUEUE SET DEFAULTQUEUE=?, DESCRIPTION=?, NAME=? WHERE ID=?");
        queries.put("q_delete_all", "DELETE FROM QUEUE");
        queries.put("q_select_count_all", "SELECT COUNT(1) FROM QUEUE");
        queries.put("q_select_all", "SELECT ID, DEFAULTQUEUE, DESCRIPTION, NAME FROM QUEUE");
        queries.put("q_select_default", "SELECT ID, DEFAULTQUEUE, DESCRIPTION, NAME FROM QUEUE WHERE DEFAULTQUEUE=1");
        queries.put("q_select_by_key", "SELECT ID, DEFAULTQUEUE, DESCRIPTION, NAME FROM QUEUE WHERE NAME=?");
        queries.put("q_select_by_id", "SELECT ID, DEFAULTQUEUE, DESCRIPTION, NAME FROM QUEUE WHERE ID=?");
        
        // DEPLOYMENT
        queries.put("dp_insert", "INSERT INTO DEPLOYMENTPARAMETER(ENABLED, LASTMODIFIED, NBTHREAD, POLLINGINTERVAL, NODE, QUEUE) VALUES(?, CURRENT_TIMESTAMP, ?, ?, ?, ?)");
        queries.put("dp_delete_all", "DELETE FROM DEPLOYMENTPARAMETER");
        queries.put("dp_select_for_node", "SELECT ID, ENABLED, LASTMODIFIED, NBTHREAD, POLLINGINTERVAL, NODE, QUEUE FROM DEPLOYMENTPARAMETER WHERE NODE=?");
        queries.put("dp_select_count_for_node", "SELECT COUNT(1) FROM DEPLOYMENTPARAMETER WHERE NODE=?");
        
        // JOB DEF
        queries.put("jd_insert", "INSERT INTO JOBDEF(APPLICATION, APPLICATIONNAME, CHILDFIRSTCLASSLOADER, "
                + "CLASSLOADERTRACING, DESCRIPTION, ENABLED, EXTERNAL, HIDDENJAVACLASSES, HIGHLANDER, "
                + "JARPATH, JAVACLASSNAME, JAVA_OPTS, KEYWORD1, KEYWORD2, KEYWORD3, MAXTIMERUNNING, "
                + "MODULE, PATHTYPE, SPECIFICISOLATIONCONTEXT, QUEUE_ID) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        queries.put("jd_delete_all", "DELETE FROM JOBDEF");
        queries.put("jd_update_all_fields_by_id", "UPDATE JOBDEF SET APPLICATION=?, APPLICATIONNAME=?, CHILDFIRSTCLASSLOADER=?, "
                + "CLASSLOADERTRACING=?, DESCRIPTION=?, ENABLED=?, EXTERNAL=?, HIDDENJAVACLASSES=?, HIGHLANDER=?, "
                + "JARPATH=?, JAVACLASSNAME=?, JAVA_OPTS=?, KEYWORD1=?, KEYWORD2=?, KEYWORD3=?, MAXTIMERUNNING=?, "
                + "MODULE=?, PATHTYPE=?, SPECIFICISOLATIONCONTEXT=?, QUEUE_ID=?) "
                + "WHERE ID=?");
        queries.put("jd_update_set_external_by_id", "UPDATE JOBDEF SET EXTERNAL=1 WHERE ID=?");
        queries.put("jd_update_set_queue_by_key", "UPDATE JOBDEF SET QUEUE_ID=? WHERE APPLICATIONNAME=?");
        queries.put("jd_select_all", "SELECT ID, APPLICATION, APPLICATIONNAME, CHILDFIRSTCLASSLOADER, "
                + "CLASSLOADERTRACING, DESCRIPTION, ENABLED, EXTERNAL, HIDDENJAVACLASSES, HIGHLANDER, "
                + "JARPATH, JAVACLASSNAME, JAVA_OPTS, KEYWORD1, KEYWORD2, KEYWORD3, MAXTIMERUNNING, "
                + "MODULE, PATHTYPE, SPECIFICISOLATIONCONTEXT, QUEUE_ID FROM JobDef");
        queries.put("jd_select_by_id", queries.get("jd_select_all") + " WHERE ID=?");
        queries.put("jd_select_by_key", queries.get("jd_select_all") + " WHERE APPLICATIONNAME=?");
        queries.put("jd_select_by_queue", queries.get("jd_select_all") + " WHERE QUEUE_ID=?");
        
        // JOB DEF PRM
        queries.put("jdprm_insert", "INSERT INTO JOBDEFPARAMETER(KEYNAME, VALUE, JOBDEF_ID) VALUES(?, ?, ?)");
        queries.put("jdprm_delete_all", "DELETE FROM JOBDEFPARAMETER");
        queries.put("jdprm_select_all_for_jd", "SELECT ID, KEYNAME, VALUE, JOBDEF_ID FROM JOBDEFPARAMETER WHERE JOBDEF_ID=?");
        queries.put("jdprm_select_all", "SELECT ID, KEYNAME, VALUE, JOBDEF_ID FROM JOBDEFPARAMETER ORDER BY JOBDEF_ID");
        
        // JOB INSTANCE
        queries.put("ji_insert_enqueue", "INSERT INTO JOBINSTANCE (CREATIONDATE, SENDEMAIL, APPLICATION, "
                + "KEYWORD1, KEYWORD2, KEYWORD3, MODULE, INTERNALPOSITION, PARENTID, PROGRESS, SESSIONID, "
                + "STATE, USERNAME, JD_ID, QUEUE_ID) "
                + "VALUES(CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, UNIX_MILLIS(), ?, 0, ?, 'SUBMITTED', ?, ?, ?)");
        queries.put("ji_delete_all", "DELETE FROM JOBINSTANCE");
        queries.put("ji_delete_by_id", "DELETE FROM JOBINSTANCE WHERE ID = ?");
        queries.put("jj_update_cancel_by_id", "UPDATE JOBINSTANCE SET STATE='CANCELLED' WHERE ID=? AND STATE='SUBMITTED'");
        queries.put("jj_update_kill_by_id", "UPDATE JOBINSTANCE SET STATE='KILLED' WHERE ID=?");
        queries.put("jj_update_pause_by_id", "UPDATE JOBINSTANCE SET STATE='HOLDED' WHERE ID=? AND STATE='SUBMITTED'");
        queries.put("jj_update_resume_by_id", "UPDATE JOBINSTANCE SET STATE='SUBMITTED' WHERE ID=? AND STATE='HOLDED'");
        queries.put("jj_update_queue_by_id", "UPDATE JOBINSTANCE SET QUEUE_ID=? WHERE ID=? AND STATE IN ('SUBMITTED', 'HOLDED')");
        queries.put("jj_update_rank_by_id", "UPDATE JOBINSTANCE SET INTERNALPOSITION=? WHERE ID=? AND STATE='SUBMITTED'");
        queries.put("jj_update_progress_by_id", "UPDATE JOBINSTANCE SET PROGRESS=? WHERE ID=?");
        queries.put("jj_update_run_by_id", "UPDATE JOBINSTANCE SET EXECUTIONDATE=CURRENT_TIMESTAMP, STATE='RUNNING' WHERE ID=? AND STATE='ATTRIBUTED'");
        queries.put("debug_jj_update_node_by_id", "UPDATE JOBINSTANCE SET NODE_ID=? WHERE ID=?");
        queries.put("debug_jj_update_status_by_id", "UPDATE JOBINSTANCE SET STATE=? WHERE ID=?");
        queries.put("ji_select_current_pos", "SELECT COUNT(ji) FROM %1$sJobInstance ji WHERE ji.internalPosition < ? AND ji.state = 'SUBMITTED' AND QUEUE_ID=?");
        queries.put("ji_select_count_all", "SELECT COUNT(1) FROM JobInstance");
        queries.put("ji_select_count_running", "SELECT COUNT(1) FROM JobInstance WHERE STATE='RUNNING'");
        queries.put("ji_select_all", "SELECT ID, ATTRIBUTIONDATE, CREATIONDATE, SENDEMAIL, EXECUTIONDATE, APPLICATION, KEYWORD1, KEYWORD2, "
                + "KEYWORD3, MODULE, INTERNALPOSITION, PARENTID, PROGRESS, SESSIONID, STATE, USERNAME, JD_ID, NODE_ID, QUEUE_ID , HIGHLANDER, "
                + "q.ID, q.DEFAULTQUEUE, q.DESCRIPTION, q.NAME, "
                + "jd.ID, jd.APPLICATION, jd.APPLICATIONNAME, jd.CHILDFIRSTCLASSLOADER, "
                + "jd.CLASSLOADERTRACING, jd.DESCRIPTION, jd.ENABLED, jd.EXTERNAL, jd.HIDDENJAVACLASSES, jd.HIGHLANDER, "
                + "jd.JARPATH, jd.JAVACLASSNAME, jd.JAVA_OPTS, jd.KEYWORD1, jd.KEYWORD2, jd.KEYWORD3, jd.MAXTIMERUNNING, "
                + "jd.MODULE, jd.PATHTYPE, jd.SPECIFICISOLATIONCONTEXT, jd.QUEUE_ID, "
                + "n.ID, n.DLREPO, n.DNS, n.ENABLED, n.EXPORTREPO, n.JMXREGISTRYPORT, n.JMXSERVERPORT, "
                + "n.LOADAPIADMIN, n.LOADAPICLIENT, n.LOAPAPISIMPLE, n.NODENAME, n.PORT, n.REPO, n.ROOTLOGLEVEL, n.STOP, n.TMPDIRECTORY, n.LASTSEENALIVE "
                + "FROM JOBINSTANCE ji LEFT JOIN QUEUE q ON ji.QUEUE_ID=q.ID LEFT JOIN JOBDEF jd ON ji.JD_ID=jd.ID LEFT JOIN NODE n ON ji.NODE_ID=n.ID ");
        queries.put("ji_select_by_id", queries.get("ji_select_all") + " WHERE ID=?");
        queries.put("ji_select_by_queue", queries.get("ji_select_all") + " WHERE QUEUE_ID=? ORDER BY INTERNALPOSITION");
        queries.put("ji_select_by_node", queries.get("ji_select_all") + " WHERE NODE_ID=?");
        queries.put("ji_select_existing_highlander", "SELECT ID FROM JOBINSTANCE WHERE JD_ID=? AND STATE='SUBMITTED'");
        queries.put("ji_select_changequeuepos_by_id", "SELECT QUEUE_ID, INTERNALPOSITION FROM JOBINSTANCE WHERE ID=? AND STATE='SUBMITTED'");
        queries.put("ji_select_state_by_id", "SELECT STATE FROM JOBINSTANCE WHERE ID=?");
        queries.put("ji_select_execution_date_by_id", "SELECT EXECUTION_DATE FROM JOBINSTANCE WHERE ID=?");
        queries.put("ji_select_cnx_data_by_id", "SELECT DNS||':'||PORT AS HOST FROM JobInstance ji LEFT JOIN Node n ON ji.NODE_ID = n.ID WHERE ji.ID=?");
        
        queries.put("ji_update_poll", "UPDATE JOBINSTANCE j1 SET j1.NODE_ID=?, j1.STATE='ATTRIBUTED', ATTRIBUTIONDATE=CURRENT_TIMESTAMP WHERE j1.ID IN "
                + "(SELECT j2.ID FROM JOBINSTANCE j2 WHERE j2.STATE='SUBMITTED' AND j2.QUEUE_ID=? "
                + "AND (j2.HIGHLANDER=0 OR (j2.HIGHLANDER=1 AND (SELECT COUNT(1) FROM JOBINSTANCE j3 WHERE j3.STATE IN ('ATTRIBUTED', 'RUNNING') AND j3.JD_ID=j2.JD_ID)=0 )) ORDER BY INTERNALPOSITION LIMIT ?)");
        queries.put("ji_update_poll", "UPDATE JOBINSTANCE j1 SET j1.NODE_ID=?, j1.STATE='ATTRIBUTED', ATTRIBUTIONDATE=CURRENT_TIMESTAMP WHERE j1.ID IN "
                + "(SELECT j2.ID FROM JOBINSTANCE j2 WHERE j2.STATE='SUBMITTED' AND j2.QUEUE_ID=? "
                + " ORDER BY INTERNALPOSITION LIMIT ?)");
        queries.put("ji_select_to_run", queries.get("ji_select_all") + " WHERE NODE_ID = ? AND QUEUE_ID = ? AND STATE='ATTRIBUTED'");
        
        // HISTORY
        queries.put("history_insert_with_end_date", "INSERT INTO HISTORY(ID, APPLICATION, APPLICATIONNAME, ATTRIBUTIONDATE, EMAIL, "
                + "END_DATE, ENQUEUE_DATE, EXECUTION_DATE, HIGHLANDER, INSTANCE_APPLICATION, INSTANCE_KEYWORD1, "
                + "INSTANCE_KEYWORD2, INSTANCE_KEYWORD3, INSTANCE_MODULE, KEYWORD1, KEYWORD2, KEYWORD3, MODULE, "
                + "NODENAME, PARENT_JOB_ID, PROGRESS, QUEUE_NAME, RETURN_CODE, SESSION_ID, STATUS, USERNAME, JOBDEF_ID, "
                + "NODE_ID, QUEUE_ID) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        queries.put("history_insert", queries.get("history_insert_with_end_date").replace("(?, ?, ?, ?, ?, ?",  "(?, ?, ?, ?, ?, CURRENT_TIMESTAMP"));
        
        queries.put("history_delete_all", "DELETE FROM HISTORY");
        queries.put("history_delete_by_id", "DELETE FROM HISTORY WHERE ID=?");
        queries.put("history_select_count_all", "SELECT COUNT(1) FROM History");
        queries.put("history_select_count_for_poller", "SELECT COUNT(1) FROM History WHERE QUEUE_ID=? AND NODE_ID=?");
        queries.put("history_select_count_last_mn_for_poller", "SELECT COUNT(1)/60 FROM History WHERE QUEUE_ID=? AND NODE_ID=? AND ENDDATE > CURRENT_TIMESTAMP - INTERVAL OF 1 MINUTE");
        queries.put("history_select_count_ended", "SELECT COUNT(1) FROM History WHERE STATUS='ENDED'");
        queries.put("history_select_count_notended", "SELECT COUNT(1) FROM History WHERE STATUS<>'ENDED'");
        queries.put("history_select_reenqueue_by_id", "SELECT APPLICATION, APPLICATIONNAME, EMAIL, INSTANCE_KEYWORD1, INSTANCE_KEYWORD2, INSTANCE_KEYWORD3, INSTANCE_MODULE, PARENT_JOB_ID, SESSION_ID, USERNAME, STATUS FROM HISTORY WHERE ID=?");
        queries.put("history_select_cnx_data_by_id", "SELECT DNS||':'||PORT AS HOST FROM History h LEFT JOIN Node n ON h.NODE_ID = n.ID WHERE h.ID=?");
        queries.put("history_select_state_by_id", "SELECT STATUS FROM HISTORY WHERE ID=?");
        
        // DELIVERABLE
        queries.put("deliverable_insert",  "INSERT INTO DELIVERABLE(FILE_FAMILLY, FILEPATH, JOBID, ORIGINALFILENAME, RANDOMID) VALUES(?, ?, ?, ?, ?)");
        queries.put("deliverable_delete_all", "DELETE FROM DELIVERABLE");
        queries.put("deliverable_select_all",  "SELECT ID, FILE_FAMILLY, FILEPATH, JOBID, ORIGINALFILENAME, RANDOMID FROM DELIVERABLE");
        queries.put("deliverable_select_by_id", queries.get("deliverable_select_all") +  " WHERE ID=?");
        queries.put("deliverable_select_all_for_ji", queries.get("deliverable_select_all") +  " WHERE JOBID=?");
        
        // RUNTIME PRM
        queries.put("jiprm_insert", "INSERT INTO RUNTIMEPARAMETER(JI_ID, KEYNAME, VALUE) VALUES(?, ?, ?)");
        queries.put("jiprm_delete_all", "DELETE FROM RUNTIMEPARAMETER ");
        queries.put("jiprm_delete_by_ji",queries.get("jiprm_delete_all") + " WHERE JI_ID=?");
        queries.put("jiprm_select_by_ji", "SELECT ID, JI_ID, KEYNAME, VALUE FROM RUNTIMEPARAMETER WHERE JI_ID=?");
        queries.put("jiprm_select_by_ji_list", "SELECT ID, JI_ID, KEYNAME, VALUE FROM RUNTIMEPARAMETER WHERE JI_ID IN (UNNEST(?))");
        
        // MESSAGE
        queries.put("message_insert",  "INSERT INTO MESSAGE(JI, TEXT_MESSAGE) VALUES(?, ?)");
        queries.put("message_delete_all", "DELETE FROM MESSAGE");
        queries.put("message_delete_by_ji",queries.get("message_delete_all") + " WHERE JI=?");
        queries.put("message_select_by_ji_list","SELECT ID, JI, TEXT_MESSAGE FROM MESSAGE WHERE JI IN (UNNEST(?))");
        
        // JNDI
        queries.put("jndi_insert", "INSERT INTO JNDIOBJECTRESOURCE(AUTH, DESCRIPTION, FACTORY, LASTMODIFIED, NAME, SINGLETON, TEMPLATE, TYPE) VALUES('CONTAINER', ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)");
        queries.put("jndi_delete_all", "DELETE FROM JNDIOBJECTRESOURCE");
        queries.put("jndi_select_count_for_key", "SELECT COUNT(1) FROM JNDIOBJECTRESOURCE WHERE NAME=?");
        queries.put("jndi_select_count_changed", "SELECT COUNT(1) FROM JNDIOBJECTRESOURCE r RIGHT JOIN JNDIOBJECTRESOURCEPARAMETER p ON p.RESOURCE_ID = r.ID WHERE r.LASTMODIFIED > ? OR p.LASTMODIFIED > ?");
        queries.put("jndi_select_all", "SELECT ID, NAME, AUTH, TYPE, FACTORY, DESCRIPTION, TEMPLATE, SINGLETON, LASTMODIFIED FROM JNDIOBJECTRESOURCE");
        queries.put("jndi_select_by_key", queries.get("jndi_select_all") + " WHERE NAME=?");
        
        // JNDI PRM
        queries.put("jndiprm_insert", "INSERT INTO JNDIOBJECTRESOURCEPARAMETER(KEYNAME, LASTMODIFIED, VALUE, RESOURCE_ID) VALUES(?, CURRENT_TIMESTAMP, ?, ?)");
        queries.put("jndiprm_delete_all", "DELETE FROM JNDIOBJECTRESOURCEPARAMETER");
        queries.put("jndiprm_select_all_in_jndisrc", "SELECT ID, KEYNAME, LASTMODIFIED, VALUE FROM JNDIOBJECTRESOURCEPARAMETER WHERE RESOURCE_ID=?");
        
        // PKI
        queries.put("pki_insert", "INSERT INTO PKI(PEMCERT, PEMPK, PRETTYNAME) VALUES(?, ?, ?)");
        queries.put("pki_delete_all", "DELETE FROM PKI");
        queries.put("pki_select_by_key", "SELECT ID, PEMCERT, PEMPK, PRETTYNAME FROM PKI WHERE PRETTYNAME=?");
        
        // R-ROLE
        queries.put("role_insert", "INSERT INTO RROLE(DESCRIPTION, NAME) VALUES(?, ?)");
        queries.put("role_delete_all", "DELETE FROM RROLE");
        queries.put("role_select_all_for_user", "SELECT ID, NAME, DESCRIPTION FROM RROLE r RIGHT JOIN RROLE_RUSER a ON a.ROLE_ID = r.ID WHERE a.USER_ID=?");
        queries.put("role_select_by_key", "SELECT ID, NAME, DESCRIPTION FROM RROLE r WHERE NAME=?");
        
        // R-PERMISSION
        queries.put("perm_insert", "INSERT INTO RPERMISSION(NAME, ROLE_ID) VALUES(?, ?)");
        queries.put("perm_delete_all", "DELETE FROM RPERMISSION");
        queries.put("perm_select_all_in_role", "SELECT ID, NAME, ROLE FROM RROLE WHERE ROLE=?");
        
        // R-USER
        queries.put("user_insert", "INSERT INTO RUSER(CREATION_DATE, EMAIL, EXPIRATION_DATE, FREETEXT, HASHSALT, INTERNAL, LAST_MODIFIED, LOCKED, LOGIN, PASSWORD) VALUES(CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?)");
        queries.put("user_delete_all", "DELETE FROM RUSER");
        queries.put("user_delete_expired_internal", "DELETE FROM RUSER WHERE internal=1 AND EXPIRATION_DATE < CURRENT_TIMESTAMP");
        queries.put("user_add_role_by_name", "INSERT INTO RROLE_RUSER(USER_ID, ROLE_ID) VALUES(?, (SELECT r.ID FROM RROLE r WHERE r.NAME=?))");
        queries.put("user_remove_all_roles_by_key", "DELETE FROM RROLE_RUSER WHERE USER_ID=?");
        queries.put("user_update_enable_by_id", "UPDATE RUSER SET LOCKED=0 WHERE ID=?");
        queries.put("user_select_all_in_role", "SELECT ID, LOGIN, PASSWORD, HASHSALT, LOCKED, EXPIRATION_DATE, CREATION_DATE, LAST_MODIFIED, EMAIL, FREETEXT, INTERNAL FROM RUSER u RIGHT JOIN RROLE_RUSER a ON a.USER_ID = u.ID WHERE a.ROLE_ID=?");
        queries.put("user_select_by_key",      "SELECT ID, LOGIN, PASSWORD, HASHSALT, LOCKED, EXPIRATION_DATE, CREATION_DATE, LAST_MODIFIED, EMAIL, FREETEXT, INTERNAL FROM RUSER WHERE LOGIN=?");
        queries.put("user_select_count_by_key", "SELECT COUNT(1) FROM RUSER WHERE LOGIN=?");
        queries.put("user_select_id_by_key", "SELECT ID FROM RUSER WHERE LOGIN=?");
        
        // GLOBAL PRM
        queries.put("globalprm_insert", "INSERT INTO GLOBALPARAMETER(KEYNAME, VALUE, LASTMODIFIED) VALUES(?, ?, CURRENT_TIMESTAMP)");
        queries.put("globalprm_update_value_by_key", "UPDATE GLOBALPARAMETER SET VALUE=? WHERE KEYNAME=?");
        queries.put("globalprm_delete_all", "DELETE FROM GLOBALPARAMETER");
        queries.put("globalprm_select_all", "SELECT ID, KEYNAME, VALUE, LASTMODIFIED FROM GLOBALPARAMETER");
        queries.put("globalprm_select_by_key", queries.get("globalprm_select_all") + " WHERE KEYNAME=?");
        queries.put("globalprm_select_count_modified_jetty", "SELECT COUNT(1) FROM GLOBALPARAMETER WHERE LASTMODIFIED > ? AND KEYNAME IN ('disableWsApi', 'enableWsApiSsl', 'enableInternalPki', 'pfxPassword', 'enableWsApiAuth')");
    }
   
}

