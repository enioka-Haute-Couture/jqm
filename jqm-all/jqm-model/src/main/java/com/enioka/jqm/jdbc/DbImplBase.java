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
        queries.put("node_insert", "INSERT INTO NODE(REPO_DELIVERABLE, DNS, ENABLED, JMX_REGISTRY_PORT, JMX_SERVER_PORT, "
                + "LOAD_API_ADMIN, LOAD_API_CLIENT, LOAD_API_SIMPLE, NAME, PORT, REPO_JOB_DEF, ROOT_LOG_LEVEL, STOP, REPO_TMP) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        queries.put("node_delete_all", "DELETE FROM NODE");
        queries.put("node_delete_by_id", "DELETE NODE WHERE ID=?");
        queries.put("node_update_all_enable_ws", "UPDATE NODE SET LOAD_API_SIMPLE=1, LOAD_API_CLIENT=1, LOAD_API_ADMIN=1, DNS='0.0.0.0'");
        queries.put("node_update_all_disable_ws", "UPDATE NODE SET LOAD_API_CLIENT=0, LOAD_API_ADMIN=0");
        queries.put("node_update_enabled_by_id", "UPDATE NODE SET ENABLED=? WHERE ID=?");
        queries.put("node_update_port_by_id", "UPDATE NODE SET PORT=? WHERE ID=?");
        queries.put("node_update_jmx_by_id", "UPDATE NODE SET JMX_REGISTRY_PORT=?, JMX_SERVER_PORT=? WHERE ID=?");
        queries.put("node_update_alive_by_id", "UPDATE NODE SET LAST_SEEN_ALIVE=CURRENT_TIMESTAMP WHERE ID=?");
        queries.put("node_update_has_stopped_by_id", "UPDATE NODE SET LAST_SEEN_ALIVE=NULL, STOP=0 WHERE ID=?");
        queries.put("node_update_stop_by_id", "UPDATE NODE SET STOP=1 WHERE ID=?");
        queries.put("node_update_all_log_level", "UPDATE NODE SET ROOT_LOG_LEVEL=?");
        queries.put("node_update_changed_by_id", "UPDATE NODE SET REPO_DELIVERABLE=?, DNS=?, ENABLED=?, JMX_REGISTRY_PORT=?, JMX_SERVER_PORT=?, "
                + "LOAD_API_ADMIN=?, LOAD_API_CLIENT=?, LOAD_API_SIMPLE=?, NAME=?, PORT=?, REPO_JOB_DEF=?, ROOT_LOG_LEVEL=?, STOP=?, REPO_TMP=? "
                + "WHERE ID=? AND NOT (REPO_DELIVERABLE=? AND DNS=? AND ENABLED=? AND JMX_REGISTRY_PORT=? AND JMX_SERVER_PORT=? AND "
                + "LOAD_API_ADMIN=? AND LOAD_API_CLIENT=? AND LOAD_API_SIMPLE=? AND NAME=? AND PORT=? AND REPO_JOB_DEF=? AND ROOT_LOG_LEVEL=? AND STOP=? AND REPO_TMP=?)");
        queries.put("node_select_all", "SELECT ID, REPO_DELIVERABLE, DNS, ENABLED, JMX_REGISTRY_PORT, JMX_SERVER_PORT, "
                + "LOAD_API_ADMIN, LOAD_API_CLIENT, LOAD_API_SIMPLE, NAME, PORT, REPO_JOB_DEF, ROOT_LOG_LEVEL, STOP, REPO_TMP, LAST_SEEN_ALIVE "
                + "FROM NODE");
        queries.put("node_select_by_key", queries.get("node_select_all") + " WHERE NAME=?");
        queries.put("node_select_by_id", queries.get("node_select_all") + " WHERE ID=?");
        queries.put("node_select_connectdata_by_key", "SELECT DNS, PORT FROM NODE WHERE NAME=?");
        
        // QUEUE
        queries.put("q_insert", "INSERT INTO QUEUE(DEFAULT_QUEUE, DESCRIPTION, NAME) VALUES(?, ?, ?)");
        queries.put("q_delete_all", "DELETE FROM QUEUE");
        queries.put("q_delete_by_id", "DELETE FROM QUEUE WHERE ID=?");
        queries.put("q_update_default_none", "UPDATE QUEUE SET DEFAULT_QUEUE=0");
        queries.put("q_update_default_by_id", "UPDATE QUEUE SET DEFAULT_QUEUE=1 WHERE ID=?");
        queries.put("q_update_all_fields_by_id", "UPDATE QUEUE SET DEFAULT_QUEUE=?, DESCRIPTION=?, NAME=? WHERE ID=?");
        queries.put("q_update_changed_by_id", "UPDATE QUEUE SET DEFAULT_QUEUE=?, DESCRIPTION=?, NAME=? WHERE ID=? AND NOT (DEFAULT_QUEUE=? AND DESCRIPTION=? AND NAME=?)");
        queries.put("q_select_count_all", "SELECT COUNT(1) FROM QUEUE");
        queries.put("q_select_all", "SELECT ID, DEFAULT_QUEUE, DESCRIPTION, NAME FROM QUEUE");
        queries.put("q_select_default", "SELECT ID, DEFAULT_QUEUE, DESCRIPTION, NAME FROM QUEUE WHERE DEFAULT_QUEUE=1");
        queries.put("q_select_by_key", "SELECT ID, DEFAULT_QUEUE, DESCRIPTION, NAME FROM QUEUE WHERE NAME=?");
        queries.put("q_select_by_id", "SELECT ID, DEFAULT_QUEUE, DESCRIPTION, NAME FROM QUEUE WHERE ID=?");
        
        // DEPLOYMENT
        queries.put("dp_insert", "INSERT INTO QUEUE_NODE_MAPPING(ENABLED, LAST_MODIFIED, MAX_THREAD, POLLING_INTERVAL, NODE, QUEUE) VALUES(?, CURRENT_TIMESTAMP, ?, ?, ?, ?)");
        queries.put("dp_delete_all", "DELETE FROM QUEUE_NODE_MAPPING");
        queries.put("dp_delete_for_node", "DELETE FROM QUEUE_NODE_MAPPING WHERE NODE=?");
        queries.put("dp_delete_for_queue", "DELETE FROM QUEUE_NODE_MAPPING WHERE QUEUE=?");
        queries.put("dp_delete_by_id", "DELETE FROM QUEUE_NODE_MAPPING WHERE ID=?");
        queries.put("dp_update_interval_by_id", "UPDATE QUEUE_NODE_MAPPING SET POLLING_INTERVAL=? WHERE ID=?");
        queries.put("dp_update_threads_by_id", "UPDATE QUEUE_NODE_MAPPING SET MAX_THREAD=? WHERE ID=?");
        queries.put("dp_update_changed_by_id", "UPDATE QUEUE_NODE_MAPPING SET ENABLED=?, LAST_MODIFIED=CURRENT_TIMESTAMP, MAX_THREAD=?, POLLING_INTERVAL=?, NODE=?, QUEUE=? WHERE ID=? AND NOT "
                + "(ENABLED=? AND MAX_THREAD=? AND POLLING_INTERVAL=? AND NODE=? AND QUEUE=?)");
        queries.put("dp_select_for_node", "SELECT ID, ENABLED, LAST_MODIFIED, MAX_THREAD, POLLING_INTERVAL, NODE, QUEUE FROM QUEUE_NODE_MAPPING WHERE NODE=?");
        queries.put("dp_select_count_for_node", "SELECT COUNT(1) FROM QUEUE_NODE_MAPPING WHERE NODE=?");
        queries.put("dp_select_all_with_names", "SELECT dp.ID, dp.ENABLED, dp.LAST_MODIFIED, dp.MAX_THREAD, dp.POLLING_INTERVAL, dp.NODE, dp.QUEUE, n.NAME, q.NAME FROM QUEUE_NODE_MAPPING dp LEFT JOIN NODE n ON n.ID=dp.NODE LEFT JOIN QUEUE q ON q.ID=dp.QUEUE ");
        queries.put("dp_select_with_names_by_id", queries.get("dp_select_all_with_names") + " WHERE ID=?");
        
        // JOB DEF
        queries.put("jd_insert", "INSERT INTO JOB_DEFINITION(APPLICATION, JD_KEY, CL_CHILD_FIRST, "
                + "CL_TRACING, DESCRIPTION, ENABLED, EXTERNAL, CL_HIDDEN_CLASSES, HIGHLANDER, "
                + "PATH, CLASS_NAME, JAVA_OPTS, KEYWORD1, KEYWORD2, KEYWORD3, ALERT_AFTER_SECONDS, "
                + "MODULE, PATH_TYPE, CL_KEY, QUEUE) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        queries.put("jd_delete_all", "DELETE FROM JOB_DEFINITION");
        queries.put("jd_delete_by_id", "DELETE FROM JOB_DEFINITION WHERE ID=?");
        queries.put("jd_update_all_fields_by_id", "UPDATE JOB_DEFINITION SET APPLICATION=?, JD_KEY=?, CL_CHILD_FIRST=?, "
                + "CL_TRACING=?, DESCRIPTION=?, ENABLED=?, EXTERNAL=?, CL_HIDDEN_CLASSES=?, HIGHLANDER=?, "
                + "PATH=?, CLASS_NAME=?, JAVA_OPTS=?, KEYWORD1=?, KEYWORD2=?, KEYWORD3=?, ALERT_AFTER_SECONDS=?, "
                + "MODULE=?, PATH_TYPE=?, CL_KEY=?, QUEUE=? "
                + "WHERE ID=?");
        queries.put("jd_update_set_external_by_id", "UPDATE JOB_DEFINITION SET EXTERNAL=1 WHERE ID=?");
        queries.put("jd_update_set_enabled_by_id", "UPDATE JOB_DEFINITION SET ENABLED=? WHERE ID=?");
        queries.put("jd_update_set_queue_by_key", "UPDATE JOB_DEFINITION SET QUEUE=? WHERE JD_KEY=?");
        queries.put("jd_select_all", "SELECT ID, APPLICATION, JD_KEY, CL_CHILD_FIRST, "
                + "CL_TRACING, DESCRIPTION, ENABLED, EXTERNAL, CL_HIDDEN_CLASSES, HIGHLANDER, "
                + "PATH, CLASS_NAME, JAVA_OPTS, KEYWORD1, KEYWORD2, KEYWORD3, ALERT_AFTER_SECONDS, "
                + "MODULE, PATH_TYPE, CL_KEY, QUEUE FROM JOB_DEFINITION");
        queries.put("jd_select_by_id", queries.get("jd_select_all") + " WHERE ID=?");
        queries.put("jd_select_by_key", queries.get("jd_select_all") + " WHERE JD_KEY=?");
        queries.put("jd_select_by_queue", queries.get("jd_select_all") + " WHERE QUEUE=?");
        
        // JOB DEF PRM
        queries.put("jdprm_insert", "INSERT INTO JOB_DEFINITION_PARAMETER(KEYNAME, VALUE, JOBDEF) VALUES(?, ?, ?)");
        queries.put("jdprm_delete_all", "DELETE FROM JOB_DEFINITION_PARAMETER");
        queries.put("jdprm_delete_all_for_jd", "DELETE FROM JOB_DEFINITION_PARAMETER WHERE JOBDEF=?");
        queries.put("jdprm_select_all_for_jd", "SELECT ID, KEYNAME, VALUE, JOBDEF FROM JOB_DEFINITION_PARAMETER WHERE JOBDEF=?");
        queries.put("jdprm_select_all_for_jd_list", "SELECT ID, KEYNAME, VALUE, JOBDEF FROM JOB_DEFINITION_PARAMETER WHERE JOBDEF IN(UNNEST(?))");
        queries.put("jdprm_select_all", "SELECT ID, KEYNAME, VALUE, JOBDEF FROM JOB_DEFINITION_PARAMETER ORDER BY JOBDEF");
        
        // JOB INSTANCE
        queries.put("ji_insert_enqueue", "INSERT INTO JOB_INSTANCE (DATE_ENQUEUE, EMAIL, APPLICATION, "
                + "KEYWORD1, KEYWORD2, KEYWORD3, MODULE, INTERNAL_POSITION, PARENT, PROGRESS, SESSION, "
                + "STATUS, USERNAME, JOBDEF, QUEUE, HIGHLANDER) "
                + "VALUES(CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, UNIX_MILLIS(), ?, 0, ?, 'SUBMITTED', ?, ?, ?, ?)");
        queries.put("ji_delete_all", "DELETE FROM JOB_INSTANCE");
        queries.put("ji_delete_by_id", "DELETE FROM JOB_INSTANCE WHERE ID = ?");
        queries.put("jj_update_cancel_by_id", "UPDATE JOB_INSTANCE SET STATUS='CANCELLED' WHERE ID=? AND STATUS='SUBMITTED'");
        queries.put("jj_update_kill_by_id", "UPDATE JOB_INSTANCE SET STATUS='KILLED' WHERE ID=?");
        queries.put("jj_update_pause_by_id", "UPDATE JOB_INSTANCE SET STATUS='HOLDED' WHERE ID=? AND STATUS='SUBMITTED'");
        queries.put("jj_update_resume_by_id", "UPDATE JOB_INSTANCE SET STATUS='SUBMITTED' WHERE ID=? AND STATUS='HOLDED'");
        queries.put("jj_update_queue_by_id", "UPDATE JOB_INSTANCE SET QUEUE=? WHERE ID=? AND STATUS IN ('SUBMITTED', 'HOLDED')");
        queries.put("jj_update_rank_by_id", "UPDATE JOB_INSTANCE SET INTERNAL_POSITION=? WHERE ID=? AND STATUS='SUBMITTED'");
        queries.put("jj_update_progress_by_id", "UPDATE JOB_INSTANCE SET PROGRESS=? WHERE ID=?");
        queries.put("jj_update_run_by_id", "UPDATE JOB_INSTANCE SET DATE_START=CURRENT_TIMESTAMP, STATUS='RUNNING' WHERE ID=? AND STATUS='ATTRIBUTED'");
        queries.put("debug_jj_update_node_by_id", "UPDATE JOB_INSTANCE SET NODE=? WHERE ID=?");
        queries.put("debug_jj_update_status_by_id", "UPDATE JOB_INSTANCE SET STATUS=? WHERE ID=?");
        queries.put("ji_select_current_pos", "SELECT COUNT(ji) FROM %1$sJOB_INSTANCE ji WHERE ji.INTERNAL_POSITION < ? AND ji.status = 'SUBMITTED' AND QUEUE=?");
        queries.put("ji_select_count_all", "SELECT COUNT(1) FROM JOB_INSTANCE");
        queries.put("ji_select_count_running", "SELECT COUNT(1) FROM JOB_INSTANCE WHERE STATUS='RUNNING'");
        queries.put("ji_select_count_by_jd", "SELECT COUNT(1) FROM JOB_INSTANCE WHERE JOBDEF=?");
        queries.put("ji_select_count_by_node", "SELECT COUNT(1) FROM JOB_INSTANCE WHERE NODE=?");
        queries.put("ji_select_count_by_queue", "SELECT COUNT(1) FROM JOB_INSTANCE WHERE QUEUE=?");
        queries.put("ji_select_all", "SELECT ID, DATE_ATTRIBUTION, DATE_ENQUEUE, EMAIL, DATE_START, APPLICATION, KEYWORD1, KEYWORD2, "
                + "KEYWORD3, MODULE, INTERNAL_POSITION, PARENT, PROGRESS, SESSION, STATUS, USERNAME, JOBDEF, NODE, QUEUE ,HIGHLANDER, "
                + "q.ID, q.DEFAULT_QUEUE, q.DESCRIPTION, q.NAME, "
                + "jd.ID, jd.APPLICATION, jd.JD_KEY, jd.CL_CHILD_FIRST, "
                + "jd.CL_TRACING, jd.DESCRIPTION, jd.ENABLED, jd.EXTERNAL, jd.CL_HIDDEN_CLASSES, jd.HIGHLANDER, "
                + "jd.PATH, jd.CLASS_NAME, jd.JAVA_OPTS, jd.KEYWORD1, jd.KEYWORD2, jd.KEYWORD3, jd.ALERT_AFTER_SECONDS, "
                + "jd.MODULE, jd.PATH_TYPE, jd.CL_KEY, jd.QUEUE, "
                + "n.ID, n.REPO_DELIVERABLE, n.DNS, n.ENABLED, n.JMX_REGISTRY_PORT, n.JMX_SERVER_PORT, "
                + "n.LOAD_API_ADMIN, n.LOAD_API_CLIENT, n.LOAD_API_SIMPLE, n.NAME, n.PORT, n.REPO_JOB_DEF, n.ROOT_LOG_LEVEL, n.STOP, n.REPO_TMP, n.LAST_SEEN_ALIVE "
                + "FROM JOB_INSTANCE ji LEFT JOIN QUEUE q ON ji.QUEUE=q.ID LEFT JOIN JOB_DEFINITION jd ON ji.JOBDEF=jd.ID LEFT JOIN NODE n ON ji.NODE=n.ID ");
        queries.put("ji_select_by_id", queries.get("ji_select_all") + " WHERE ID=?");
        queries.put("ji_select_by_queue", queries.get("ji_select_all") + " WHERE QUEUE=? ORDER BY INTERNAL_POSITION");
        queries.put("ji_select_by_node", queries.get("ji_select_all") + " WHERE NODE=?");
        queries.put("ji_select_existing_highlander", "SELECT ID FROM JOB_INSTANCE WHERE JOBDEF=? AND STATUS='SUBMITTED'");
        queries.put("ji_select_changequeuepos_by_id", "SELECT QUEUE, INTERNAL_POSITION FROM JOB_INSTANCE WHERE ID=? AND STATUS='SUBMITTED'");
        queries.put("ji_select_state_by_id", "SELECT STATUS FROM JOB_INSTANCE WHERE ID=?");
        queries.put("ji_select_execution_date_by_id", "SELECT DATE_START FROM JOB_INSTANCE WHERE ID=?");
        queries.put("ji_select_cnx_data_by_id", "SELECT DNS||':'||PORT AS HOST FROM JOB_INSTANCE ji LEFT JOIN Node n ON ji.NODE = n.ID WHERE ji.ID=?");
        
        queries.put("ji_update_poll", "UPDATE JOB_INSTANCE j1 SET j1.NODE=?, j1.STATUS='ATTRIBUTED', DATE_ATTRIBUTION=CURRENT_TIMESTAMP WHERE j1.ID IN "
                + "(SELECT j2.ID FROM JOB_INSTANCE j2 WHERE j2.STATUS='SUBMITTED' AND j2.QUEUE=? "
                + "AND (j2.HIGHLANDER=0 OR (j2.HIGHLANDER=1 AND (SELECT COUNT(1) FROM JOB_INSTANCE j3 WHERE j3.STATUS IN ('ATTRIBUTED', 'RUNNING') AND j3.JOBDEF=j2.JOBDEF)=0 )) ORDER BY INTERNAL_POSITION LIMIT ?)");
        queries.put("ji_select_to_run", queries.get("ji_select_all") + " WHERE NODE = ? AND QUEUE = ? AND STATUS='ATTRIBUTED'");
        
        // HISTORY
        queries.put("history_insert_with_end_date", "INSERT INTO HISTORY(ID, JD_APPLICATION, JD_KEY, DATE_ATTRIBUTION, EMAIL, "
                + "DATE_END, DATE_ENQUEUE, DATE_START, HIGHLANDER, INSTANCE_APPLICATION, INSTANCE_KEYWORD1, "
                + "INSTANCE_KEYWORD2, INSTANCE_KEYWORD3, INSTANCE_MODULE, JD_KEYWORD1, JD_KEYWORD2, JD_KEYWORD3, JD_MODULE, "
                + "NODE_NAME, PARENT, PROGRESS, QUEUE_NAME, RETURN_CODE, SESSION, STATUS, USERNAME, JOBDEF, "
                + "NODE, QUEUE) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        queries.put("history_insert", queries.get("history_insert_with_end_date").replace("(?, ?, ?, ?, ?, ?",  "(?, ?, ?, ?, ?, CURRENT_TIMESTAMP"));
        
        queries.put("history_delete_all", "DELETE FROM HISTORY");
        queries.put("history_delete_by_id", "DELETE FROM HISTORY WHERE ID=?");
        queries.put("history_select_count_all", "SELECT COUNT(1) FROM History");
        queries.put("history_select_count_for_poller", "SELECT COUNT(1) FROM History WHERE QUEUE=? AND NODE=?");
        queries.put("history_select_count_last_mn_for_poller", "SELECT COUNT(1)/60 FROM History WHERE QUEUE=? AND NODE=? AND DATE_END > (CURRENT_TIMESTAMP - 1 MINUTE)");
        queries.put("history_select_count_ended", "SELECT COUNT(1) FROM History WHERE STATUS='ENDED'");
        queries.put("history_select_count_notended", "SELECT COUNT(1) FROM History WHERE STATUS<>'ENDED'");
        queries.put("history_select_reenqueue_by_id", "SELECT JD_APPLICATION, JD_KEY, EMAIL, INSTANCE_KEYWORD1, INSTANCE_KEYWORD2, INSTANCE_KEYWORD3, INSTANCE_MODULE, PARENT, SESSION, USERNAME, STATUS FROM HISTORY WHERE ID=?");
        queries.put("history_select_cnx_data_by_id", "SELECT DNS||':'||PORT AS HOST FROM History h LEFT JOIN Node n ON h.NODE = n.ID WHERE h.ID=?");
        queries.put("history_select_state_by_id", "SELECT STATUS FROM HISTORY WHERE ID=?");
        
        // DELIVERABLE
        queries.put("deliverable_insert",  "INSERT INTO DELIVERABLE(FILE_FAMILY, PATH, JOB_INSTANCE, ORIGINAL_FILE_NAME, RANDOM_ID) VALUES(?, ?, ?, ?, ?)");
        queries.put("deliverable_delete_all", "DELETE FROM DELIVERABLE");
        queries.put("deliverable_select_all",  "SELECT ID, FILE_FAMILY, PATH, JOB_INSTANCE, ORIGINAL_FILE_NAME, RANDOM_ID FROM DELIVERABLE");
        queries.put("deliverable_select_by_id", queries.get("deliverable_select_all") +  " WHERE ID=?");
        queries.put("deliverable_select_by_randomid", queries.get("deliverable_select_all") +  " WHERE RANDOM_ID=?");
        queries.put("deliverable_select_all_for_ji", queries.get("deliverable_select_all") +  " WHERE JOB_INSTANCE=?");
        
        // RUNTIME PRM
        queries.put("jiprm_insert", "INSERT INTO JOB_INSTANCE_PARAMETER(JOB_INSTANCE, KEYNAME, VALUE) VALUES(?, ?, ?)");
        queries.put("jiprm_delete_all", "DELETE FROM JOB_INSTANCE_PARAMETER ");
        queries.put("jiprm_delete_by_ji",queries.get("jiprm_delete_all") + " WHERE JOB_INSTANCE=?");
        queries.put("jiprm_select_by_ji", "SELECT ID, JOB_INSTANCE, KEYNAME, VALUE FROM JOB_INSTANCE_PARAMETER WHERE JOB_INSTANCE=?");
        queries.put("jiprm_select_by_ji_list", "SELECT ID, JOB_INSTANCE, KEYNAME, VALUE FROM JOB_INSTANCE_PARAMETER WHERE JOB_INSTANCE IN (UNNEST(?))");
        
        // MESSAGE
        queries.put("message_insert",  "INSERT INTO MESSAGE(JOB_INSTANCE, TEXT_MESSAGE) VALUES(?, ?)");
        queries.put("message_delete_all", "DELETE FROM MESSAGE");
        queries.put("message_delete_by_ji",queries.get("message_delete_all") + " WHERE JOB_INSTANCE=?");
        queries.put("message_select_all", "SELECT ID, JOB_INSTANCE, TEXT_MESSAGE FROM MESSAGE");
        queries.put("message_select_by_ji_list", queries.get("message_select_all") + " WHERE JOB_INSTANCE IN (UNNEST(?))");
        queries.put("message_select_count_all", "SELECT COUNT(1) FROM MESSAGE");
        
        // JNDI
        queries.put("jndi_insert", "INSERT INTO JNDI_OBJECT_RESOURCE(AUTH, DESCRIPTION, FACTORY, LAST_MODIFIED, NAME, SINGLETON, TEMPLATE, TYPE) VALUES('CONTAINER', ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)");
        queries.put("jndi_delete_all", "DELETE FROM JNDI_OBJECT_RESOURCE");
        queries.put("jndi_update_changed_by_id", "UPDATE JNDI_OBJECT_RESOURCE SET AUTH=?, DESCRIPTION=?, FACTORY=?, LAST_MODIFIED=CURRENT_TIMESTAMP, NAME=?, SINGLETON=?, TEMPLATE=?, TYPE=? WHERE ID=? AND NOT (AUTH=? AND DESCRIPTION=? AND FACTORY=? AND NAME=? AND SINGLETON=? AND TEMPLATE=? AND TYPE=?)");
        queries.put("jndi_select_count_for_key", "SELECT COUNT(1) FROM JNDI_OBJECT_RESOURCE WHERE NAME=?");
        queries.put("jndi_select_count_changed", "SELECT COUNT(1) FROM JNDI_OBJECT_RESOURCE r RIGHT JOIN JNDI_OR_PARAMETER p ON p.RESOURCE = r.ID WHERE r.LAST_MODIFIED > ? OR p.LAST_MODIFIED > ?");
        queries.put("jndi_select_all", "SELECT ID, NAME, AUTH, TYPE, FACTORY, DESCRIPTION, TEMPLATE, SINGLETON, LAST_MODIFIED FROM JNDI_OBJECT_RESOURCE");
        queries.put("jndi_select_by_key", queries.get("jndi_select_all") + " WHERE NAME=?");
        
        // JNDI PRM
        queries.put("jndiprm_insert", "INSERT INTO JNDI_OR_PARAMETER(KEYNAME, LAST_MODIFIED, VALUE, RESOURCE) VALUES(?, CURRENT_TIMESTAMP, ?, ?)");
        queries.put("jndiprm_delete_all", "DELETE FROM JNDI_OR_PARAMETER");
        queries.put("jndiprm_delete_for_resource", "DELETE FROM JNDI_OR_PARAMETER WHERE RESOURCE=?");
        queries.put("jndiprm_delete_by_id", "DELETE FROM JNDI_OR_PARAMETER WHERE ID=?");
        queries.put("jndiprm_update_value_by_key", "UPDATE JNDI_OR_PARAMETER SET VALUE=?, LAST_MODIFIED=CURRENT_TIMESTAMP WHERE KEYNAME=?");
        queries.put("jndiprm_update_changed_by_id", "UPDATE JNDI_OR_PARAMETER SET VALUE=?, LAST_MODIFIED=CURRENT_TIMESTAMP WHERE KEYNAME=? AND NOT VALUE=?");
        queries.put("jndiprm_select_all_in_jndisrc", "SELECT ID, KEYNAME, LAST_MODIFIED, VALUE FROM JNDI_OR_PARAMETER WHERE RESOURCE=?");
        queries.put("jndiprm_select_all_in_jndisrc_list", "SELECT ID, KEYNAME, LAST_MODIFIED, VALUE, RESOURCE FROM JNDI_OR_PARAMETER WHERE RESOURCE IN (UNNEST(?))");
        
        // PKI
        queries.put("pki_insert", "INSERT INTO PKI(PEM_CERT, PEM_PK, PRETTY_NAME) VALUES(?, ?, ?)");
        queries.put("pki_delete_all", "DELETE FROM PKI");
        queries.put("pki_select_by_key", "SELECT ID, PEM_CERT, PEM_PK, PRETTY_NAME FROM PKI WHERE PRETTY_NAME=?");
        
        // R-ROLE
        queries.put("role_insert", "INSERT INTO RROLE(DESCRIPTION, NAME) VALUES(?, ?)");
        queries.put("role_delete_all", "DELETE FROM RROLE");
        queries.put("role_delete_by_id", "DELETE FROM RROLE WHERE ID=?");
        queries.put("role_update_all_by_id", "UPDATE RROLE SET NAME=?, DESCRIPTION=? WHERE ID=? ");
        queries.put("role_select_all", "SELECT r.ID, r.NAME, r.DESCRIPTION FROM RROLE r ");
        queries.put("role_select_all_for_user", "SELECT r.ID, r.NAME, r.DESCRIPTION FROM RROLE r RIGHT JOIN RROLE_RUSER a ON a.ROLE = r.ID WHERE a.ACCOUNT=?");
        queries.put("role_select_id_for_user_list", "SELECT a.ROLE, a.USER FROM RROLE_RUSER a WHERE a.USER IN(UNNEST(?))");
        queries.put("role_select_by_key", "SELECT ID, NAME, DESCRIPTION FROM RROLE r WHERE NAME=?");
        
        // R-PERMISSION
        queries.put("perm_insert", "INSERT INTO RPERMISSION(NAME, ROLE) VALUES(?, ?)");
        queries.put("perm_delete_all", "DELETE FROM RPERMISSION");
        queries.put("perm_delete_for_role", "DELETE FROM RPERMISSION WHERE ROLE=?");
        queries.put("perm_select_all_in_role", "SELECT ID, NAME, ROLE FROM RPERMISSION WHERE ROLE=?");
        queries.put("perm_select_all_in_role_list", "SELECT ID, NAME, ROLE FROM RPERMISSION WHERE ROLE IN(UNNEST(?))");
        
        // R-USER
        queries.put("user_insert", "INSERT INTO RUSER(CREATION_DATE, EMAIL, EXPIRATION_DATE, FREETEXT, HASHSALT, INTERNAL, LAST_MODIFIED, LOCKED, LOGIN, PASSWORD) VALUES(CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?)");
        queries.put("user_delete_all", "DELETE FROM RUSER");
        queries.put("user_delete_by_id", "DELETE FROM RUSER WHERE ID=?");
        queries.put("user_delete_expired_internal", "DELETE FROM RUSER WHERE internal=1 AND EXPIRATION_DATE < CURRENT_TIMESTAMP");
        queries.put("user_add_role_by_name", "INSERT INTO RROLE_RUSER(ACCOUNT, ROLE) VALUES(?, (SELECT r.ID FROM RROLE r WHERE r.NAME=?))");
        queries.put("user_add_role_by_id", "INSERT INTO RROLE_RUSER(ACCOUNT, ROLE) VALUES(?, ?)");
        queries.put("user_remove_all_roles_by_id", "DELETE FROM RROLE_RUSER WHERE ACCOUNT=?");
        queries.put("user_remove_role", "DELETE FROM RROLE_RUSER WHERE ROLE=? AND ACCOUNT=?");
        queries.put("user_update_enable_by_id", "UPDATE RUSER SET LOCKED=0 WHERE ID=?");
        queries.put("user_update_password_by_id", "UPDATE RUSER SET PASSWORD=?, HASHSALT=? WHERE ID=?");
        queries.put("user_update_changed", "UPDATE RUSER SET LOGIN=?, LOCKED=?, EXPIRATION_DATE=?, LAST_MODIFIED=CURRENT_TIMESTAMP, EMAIL=?, FREETEXT=? WHERE ID=? AND NOT (LOGIN=? AND LOCKED=? AND EXPIRATION_DATE=? AND EMAIL=? AND FREETEXT=? )");
        queries.put("user_select_all_in_role", "SELECT ID, LOGIN, PASSWORD, HASHSALT, LOCKED, EXPIRATION_DATE, CREATION_DATE, LAST_MODIFIED, EMAIL, FREETEXT, INTERNAL FROM RUSER u RIGHT JOIN RROLE_RUSER a ON a.USER = u.ID WHERE a.ROLE=?");
        queries.put("user_select_all",         "SELECT ID, LOGIN, PASSWORD, HASHSALT, LOCKED, EXPIRATION_DATE, CREATION_DATE, LAST_MODIFIED, EMAIL, FREETEXT, INTERNAL FROM RUSER ");
        queries.put("user_select_by_key",      queries.get("user_select_all") +  " WHERE LOGIN=?");
        queries.put("user_select_by_id",       queries.get("user_select_all") +  " WHERE ID=?");
        queries.put("user_select_count_by_key", "SELECT COUNT(1) FROM RUSER WHERE LOGIN=?");
        queries.put("user_select_id_by_key", "SELECT ID FROM RUSER WHERE LOGIN=?");
        queries.put("user_select_count_using_role", "SELECT COUNT(1) FROM RROLE_RUSER WHERE ROLE=?");
        
        // GLOBAL PRM
        queries.put("globalprm_insert", "INSERT INTO GLOBAL_PARAMETER(KEYNAME, VALUE, LAST_MODIFIED) VALUES(?, ?, CURRENT_TIMESTAMP)");
        queries.put("globalprm_update_value_by_key", "UPDATE GLOBAL_PARAMETER SET VALUE=? WHERE KEYNAME=?");
        queries.put("globalprm_delete_all", "DELETE FROM GLOBAL_PARAMETER");
        queries.put("globalprm_delete_by_id", "DELETE FROM GLOBAL_PARAMETER WHERE ID=?");
        queries.put("globalprm_select_all", "SELECT ID, KEYNAME, VALUE, LAST_MODIFIED FROM GLOBAL_PARAMETER");
        queries.put("globalprm_select_by_key", queries.get("globalprm_select_all") + " WHERE KEYNAME=?");
        queries.put("globalprm_select_by_id", queries.get("globalprm_select_all") + " WHERE ID=?");
        queries.put("globalprm_select_count_modified_jetty", "SELECT COUNT(1) FROM GLOBAL_PARAMETER WHERE LAST_MODIFIED > ? AND KEYNAME IN ('disableWsApi', 'enableWsApiSsl', 'enableInternalPki', 'pfxPassword', 'enableWsApiAuth')");
    }
   
}

