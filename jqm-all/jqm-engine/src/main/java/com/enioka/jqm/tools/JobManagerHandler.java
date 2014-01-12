package com.enioka.jqm.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.JobParameter;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.State;

public class JobManagerHandler implements InvocationHandler
{
    private Logger jqmlogger = Logger.getLogger(JobManagerHandler.class);

    private JobInstance ji;
    private EntityManager em;
    private History h;

    JobManagerHandler(JobInstance ji, EntityManager em)
    {
        this.ji = ji;
        this.em = em;
        h = em.createQuery("SELECT h FROM History h WHERE h.jobInstanceId = :i", History.class).setParameter("i", this.ji.getId())
                .getSingleResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        String method_name = method.getName();
        Class<?>[] classes = method.getParameterTypes();
        jqmlogger.trace("An engine API method was called: " + method_name + " with nb arguments: " + classes.length);
        shouldKill();

        if (classes.length == 0)
        {
            if (method_name.equals("jobApplicationId"))
            {
                return ji.getJd().getId();
            }
            else if (method_name.equals("parentID"))
            {
                return ji.getParentId();
            }
            else if (method_name.equals("jobInstanceID"))
            {
                return ji.getId();
            }
            else if (method_name.equals("canBeRestarted"))
            {
                return ji.getJd().isCanBeRestarted();
            }
            else if (method_name.equals("applicationName"))
            {
                return ji.getJd().getApplicationName();
            }
            else if (method_name.equals("sessionID"))
            {
                return ji.getSessionID();
            }
            else if (method_name.equals("application"))
            {
                return ji.getJd().getApplication();
            }
            else if (method_name.equals("module"))
            {
                return ji.getJd().getModule();
            }
            else if (method_name.equals("keyword1"))
            {
                return ji.getJd().getKeyword1();
            }
            else if (method_name.equals("keyword2"))
            {
                return ji.getJd().getKeyword2();
            }
            else if (method_name.equals("keyword3"))
            {
                return ji.getJd().getKeyword3();
            }
            else if (method_name.equals("parameters"))
            {
                Map<String, String> res = new HashMap<String, String>();
                for (JobParameter p : ji.getParameters())
                {
                    res.put(p.getKey(), p.getValue());
                }
                return res;
            }
            else if (method_name.equals("defaultConnect"))
            {
                return this.getDefaultConnectionAlias();
            }
            else if (method_name.equals("getDefaultConnection"))
            {
                return this.getDefaultConnection();
            }
            else if (method_name.equals("getWorkDir"))
            {
                return getWorkDir();
            }
            else if (method_name.equals("yield"))
            {
                return null;
            }
        }
        else if (method_name.equals("sendMsg"))
        {
            if (classes.length == 1 && classes[0] == String.class)
            {
                sendMsg((String) args[0]);
                return null;
            }
        }
        else if (method_name.equals("sendProgress"))
        {
            if (classes.length == 1 && classes[0] == Integer.class)
            {
                sendProgress((Integer) args[0]);
                return null;
            }
        }
        else if (method_name.equals("enqueue"))
        {
            if (classes.length == 10 && classes[0] == String.class)
            {
                return enqueue((String) args[0], (String) args[1], (String) args[2], (String) args[3], (String) args[4], (String) args[5],
                        (String) args[6], (String) args[7], (String) args[8], (Map<String, String>) args[9]);
            }
        }
        else if (method_name.equals("enqueueSync"))
        {
            if (classes.length == 10 && classes[0] == String.class)
            {
                return enqueueSync((String) args[0], (String) args[1], (String) args[2], (String) args[3], (String) args[4],
                        (String) args[5], (String) args[6], (String) args[7], (String) args[8], (Map<String, String>) args[9]);
            }
        }
        else if (method_name.equals("addDeliverable"))
        {
            if (classes.length == 2 && classes[0] == String.class && classes[1] == String.class)
            {
                return addDeliverable((String) args[0], (String) args[1]);
            }
        }

        throw new NoSuchMethodException(method_name);
    }

    private void shouldKill() throws JqmKillException
    {
        em.refresh(ji);
        jqmlogger.debug("Analysis: should JI " + ji.getId() + " get killed? Status is " + ji.getState());
        if (ji.getState().equals(State.KILLED))
        {
            jqmlogger.debug("Link: Job will be KILLED");
            Thread.currentThread().interrupt();
            throw new JqmKillException("This job" + "(ID: " + ji.getId() + ")" + " has been killed by a user");
        }
    }

    /**
     * Create a {@link com.enioka.jqm.jpamodel.Message} with the given message. The {@link com.enioka.jqm.jpamodel.History} to link to is
     * deduced from the context.
     * 
     * @param msg
     * @throws JqmKillException
     */
    public void sendMsg(String msg) throws JqmKillException
    {
        em.getTransaction().begin();
        Message mssg = new Message();
        mssg.setHistory(h);
        mssg.setTextMessage(msg);
        em.persist(mssg);
        em.getTransaction().commit();
    }

    /**
     * Update the {@link com.enioka.jqm.jpamodel.History} with the given progress data.
     * 
     * @param msg
     * @throws JqmKillException
     */
    public void sendProgress(Integer msg) throws JqmKillException
    {
        em.getTransaction().begin();
        em.refresh(ji, LockModeType.PESSIMISTIC_WRITE);
        ji.setProgress(msg);
        h.setProgress(msg);
        em.getTransaction().commit();

        jqmlogger.debug("Current progression: " + h.getProgress());
    }

    public Integer enqueue(String applicationName, String user, String mail, String sessionId, String application, String module,
            String keyword1, String keyword2, String keyword3, Map<String, String> parameters)
    {
        JobDefinition jd = new JobDefinition(applicationName, user, mail);
        jd.setApplicationName(applicationName);
        jd.setUser(user == null ? ji.getUserName() : user);
        jd.setEmail(mail);
        jd.setSessionID(sessionId == null ? ji.getSessionID() : sessionId);
        jd.setApplication(application == null ? ji.getJd().getApplication() : application);
        jd.setModule(module == null ? ji.getJd().getModule() : module);
        jd.setKeyword1(keyword1);
        jd.setKeyword2(keyword2);
        jd.setKeyword3(keyword3);
        jd.setParentID(this.ji.getId());
        jd.setParameters(parameters);

        Integer id = Dispatcher.enQueue(jd);
        return id;
    }

    public int enqueueSync(String applicationName, String user, String mail, String sessionId, String application, String module,
            String keyword1, String keyword2, String keyword3, Map<String, String> parameters)
    {
        int i = enqueue(applicationName, user, mail, sessionId, application, module, keyword1, keyword2, keyword3, parameters);
        History child = em.createQuery("SELECT h FROM History h WHERE h.jobInstanceId = :id", History.class).setParameter("id", i)
                .getSingleResult();

        while (true)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                jqmlogger.debug(e);
            }
            em.refresh(child);

            if (child.getState().equals(State.ENDED) || child.getState().equals(State.CRASHED))
            {
                break;
            }
        }

        return i;
    }

    public Integer addDeliverable(String path, String fileLabel) throws IOException
    {
        String outputRoot = this.ji.getNode().getDlRepo();
        String ext = FilenameUtils.getExtension(path);
        String destPath = FilenameUtils.concat(outputRoot,
                "" + ji.getJd().getApplicationName() + "/" + ji.getId() + "/" + UUID.randomUUID() + "." + ext);
        String fileName = FilenameUtils.getName(path);
        FileUtils.moveFile(new File(path), new File(destPath));
        jqmlogger.info("A deliverable is added. Stored as " + destPath + ". Initial name: " + fileName);

        em.getTransaction().begin();
        Deliverable d = Helpers.createDeliverable(destPath, fileName, fileLabel, this.ji.getId(), em);
        em.getTransaction().commit();

        return d.getId();
    }

    File getWorkDir()
    {
        File f = new File(FilenameUtils.concat(this.ji.getNode().getDlRepo(), "" + this.ji.getId()));
        f.mkdir();
        return f;
    }

    String getDefaultConnectionAlias()
    {
        try
        {
            return em.createQuery("SELECT gp.value FROM GlobalParameter gp WHERE gp.key = 'defaultConnection'", String.class)
                    .getSingleResult();
        }
        catch (NoResultException ex)
        {
            return null;
        }
    }

    DataSource getDefaultConnection() throws NamingException
    {
        Object p = NamingManager.getInitialContext(null).lookup(this.getDefaultConnectionAlias());
        DataSource q = (DataSource) p;

        return q;
    }
}
