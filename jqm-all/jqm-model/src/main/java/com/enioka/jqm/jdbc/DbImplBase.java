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
        queries.put("node_delete_all", "DELETE FROM NODE");
        queries.put("node_update_all_log_level", "UPDATE NODE SET ROOTLOGLEVEL=?");
        
        // QUEUE
        queries.put("q_insert", "INSERT INTO QUEUE(DEFAULTQUEUE, DESCRIPTION, NAME) VALUES(?, ?, ?)");
        queries.put("q_delete_all", "DELETE FROM QUEUE");
        
        // DEPLOYMENT
        queries.put("dp_insert", "INSERT INTO DEPLOYMENTPARAMETER(ENABLED, LASTMODIFIED, NBTHREAD, POLLINGINTERVAL, NODE, QUEUE) VALUES(?, CURRENT_TIMESTAMP, ?, ?, ?, ?)");
        queries.put("dp_delete_all", "DELETE FROM DEPLOYMENTPARAMETER");
        
        // JOB DEF
        queries.put("jd_insert", "INSERT INTO JOBDEF(APPLICATION, APPLICATIONNAME, CHILDFIRSTCLASSLOADER, "
                + "CLASSLOADERTRACING, DESCRIPTION, ENABLED, EXTERNAL, HIDDENJAVACLASSES, HIGHLANDER, "
                + "JARPATH, JAVACLASSNAME, JAVA_OPTS, KEYWORD1, KEYWORD2, KEYWORD3, MAXTIMERUNNING, "
                + "MODULE, PATHTYPE, SPECIFICISOLATIONCONTEXT, QUEUE_ID) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
        queries.put("jd_delete_all", "DELETE FROM JOBDEF");
        queries.put("js_select_all", "SELECT ID, APPLICATION, APPLICATIONNAME, CHILDFIRSTCLASSLOADER, "
                + "CLASSLOADERTRACING, DESCRIPTION, ENABLED, EXTERNAL, HIDDENJAVACLASSES, HIGHLANDER, "
                + "JARPATH, JAVACLASSNAME, JAVA_OPTS, KEYWORD1, KEYWORD2, KEYWORD3, MAXTIMERUNNING, "
                + "MODULE, PATHTYPE, SPECIFICISOLATIONCONTEXT, QUEUE_ID FROM JobDef");
        
        // JOB DEF PRM
        queries.put("jdprm_insert", "INSERT INTO JOBDEFPARAMETER(KEYNAME, VALUE, JOBDEF_ID) VALUES(?, ?, ?)");
        queries.put("jdprm_delete_all", "DELETE FROM JOBDEFPARAMETER");
        queries.put("jdprm_select_all_for_jd", "SELECT ID, KEYNAME, VALUE, JOBDEF_ID FROM JOBDEFPARAMETER WHERE JOBDEF_ID=?");
        queries.put("jdprm_select_all", "SELECT ID, KEYNAME, VALUE, JOBDEF_ID FROM JOBDEFPARAMETER ORDER BY JOBDEF_ID");
        
        // JOB INSTANCE
        queries.put("ji_delete_all", "DELETE FROM JOBINSTANCE");
        queries.put("ji_select_current_pos", "SELECT COUNT(ji) FROM %1$sJobInstance ji WHERE ji.internalPosition < ? AND ji.state = 'SUBMITTED'");
        queries.put("ji_select_count_all", "SELECT COUNT(1) FROM JobInstance");
        queries.put("ji_select_count_running", "SELECT COUNT(1) FROM JobInstance WHERE STATE='RUNNING'");
        
        // HISTORY
        queries.put("history_delete_all", "DELETE FROM HISTORY");
        queries.put("history_select_count_all", "SELECT COUNT(1) FROM History");
        queries.put("history_select_count_ended", "SELECT COUNT(1) FROM History WHERE STATUS='ENDED'");
        queries.put("history_select_count_notended", "SELECT COUNT(1) FROM History WHERE STATUS!='ENDED'");
        
        // DELIVERABLE
        queries.put("deliverable_delete_all", "DELETE FROM DELIVERABLE");
        
        // RUNTIME PRM
        queries.put("jiprm_delete_all", "DELETE FROM RUNTIMEPARAMETER");
        
        // MESSAGE
        queries.put("message_delete_all", "DELETE FROM MESSAGE");
        
        // JNDI
        queries.put("jndi_insert", "INSERT INTO JNDIOBJECTRESOURCE(AUTH, DESCRIPTION, FACTORY, LASTMODIFIED, NAME, SINGLETON, TEMPLATE, TYPE) VALUES('CONTAINER', ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)");
        queries.put("jndi_delete_all", "DELETE FROM JNDIOBJECTRESOURCE");
        
        // JNDI PRM
        queries.put("jndiprm_insert", "INSERT INTO JNDIOBJECTRESOURCEPARAMETER(KEYNAME, LASTMODIFIED, VALUE, RESOURCE_ID) VALUES(?, CURRENT_TIMESTAMP, ?, ?)");
        queries.put("jndiprm_delete_all", "DELETE FROM JNDIOBJECTRESOURCEPARAMETER");
        queries.put("jndiprm_select_all_in_jndisrc", "SELECT ID, KEYNAME, LASTMODIFIED, VALUE FROM JNDIOBJECTRESOURCEPARAMETER WHERE RESOURCE_ID=?");
        
        // PKI
        queries.put("pki_delete_all", "DELETE FROM PKI");
        
        // R-ROLE
        queries.put("role_insert", "INSERT INTO RROLE(DESCRIPTION, NAME) VALUES(?, ?)");
        queries.put("role_delete_all", "DELETE FROM RROLE");
        queries.put("role_select_all_for_user", "SELECT ID, NAME, DESCRIPTION FROM RROLE r RIGHT JOIN RROLE_RUSER a ON a.ROLES_ID = r.ID WHERE a.USERS_ID=?");
        
        // R-PERMISSION
        queries.put("perm_insert", "INSERT INTO RPERMISSION(NAME, ROLE_ID) VALUES(?, ?)");
        queries.put("perm_delete_all", "DELETE FROM RPERMISSION");
        queries.put("perm_select_all_in_role", "SELECT ID, NAME, ROLE FROM RROLE WHERE ROLE=?");
        
        // R-USER
        queries.put("user_insert", "INSERT INTO RUSER(CREATIONDATE, EMAIL, EXPIRATIONDATE, FREETEXT, HASHSALT, INTERNAL, LASTMODIFIED, LOCKED, LOGIN, PASSWORD) VALUES(CURRENT_TIMESTAMP, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?)");
        queries.put("user_delete_all", "DELETE FROM RUSER");
        queries.put("user_add_role_by_name", "INSERT INTO RROLE_RUSER(ROLE_ID, USER_ID) VALUES(?, (SELECT ID FROM RROLE WHERE NAME=?))");
        queries.put("user_select_all_in_role", "SELECT ID, LOGIN, PASSWORD, HASHSALT, LOCKED, EXPIRATION_DATE, CREATION_DATE, LAST_MODIFIED, EMAIL, FREETEXT, INTERNAL FROM RUSER u RIGHT JOIN RROLE_RUSER a ON a.USER_ID = u.ID WHERE a.ROLE_ID=?");
        
        // GLOBAL PRM
        queries.put("globalprm_insert", "INSERT INTO GLOBALPARAMETER(KEYNAME, VALUE, LASTMODIFIED) VALUES(?, ?, CURRENT_TIMESTAMP)");
        queries.put("globalprm_delete_all", "DELETE FROM GLOBALPARAMETER");
    }
   
}

