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
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.JobParameter;
import com.enioka.jqm.jpamodel.State;

class JobManagerHandler implements InvocationHandler
{
    private Logger jqmlogger = Logger.getLogger(JobManagerHandler.class);

    private JobInstance ji;
    private EntityManager em;

    JobManagerHandler(JobInstance ji, EntityManager em)
    {
        this.ji = ji;
        this.em = em;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        String methodName = method.getName();
        Class<?>[] classes = method.getParameterTypes();
        jqmlogger.trace("An engine API method was called: " + methodName + " with nb arguments: " + classes.length);
        shouldKill();

        if (classes.length == 0)
        {
            if ("jobApplicationId".equals(methodName))
            {
                return ji.getJd().getId();
            }
            else if ("parentID".equals(methodName))
            {
                return ji.getParentId();
            }
            else if ("jobInstanceID".equals(methodName))
            {
                return ji.getId();
            }
            else if ("canBeRestarted".equals(methodName))
            {
                return ji.getJd().isCanBeRestarted();
            }
            else if ("applicationName".equals(methodName))
            {
                return ji.getJd().getApplicationName();
            }
            else if ("sessionID".equals(methodName))
            {
                return ji.getSessionID();
            }
            else if ("application".equals(methodName))
            {
                return ji.getJd().getApplication();
            }
            else if ("module".equals(methodName))
            {
                return ji.getJd().getModule();
            }
            else if ("keyword1".equals(methodName))
            {
                return ji.getJd().getKeyword1();
            }
            else if ("keyword2".equals(methodName))
            {
                return ji.getJd().getKeyword2();
            }
            else if ("keyword3".equals(methodName))
            {
                return ji.getJd().getKeyword3();
            }
            else if ("parameters".equals(methodName))
            {
                Map<String, String> res = new HashMap<String, String>();
                for (JobParameter p : ji.getParameters())
                {
                    res.put(p.getKey(), p.getValue());
                }
                return res;
            }
            else if ("defaultConnect".equals(methodName))
            {
                return this.getDefaultConnectionAlias();
            }
            else if ("getDefaultConnection".equals(methodName))
            {
                return this.getDefaultConnection();
            }
            else if ("getWorkDir".equals(methodName))
            {
                return getWorkDir();
            }
            else if ("yield".equals(methodName))
            {
                return null;
            }
        }
        else if ("sendMsg".equals(methodName) && classes.length == 1 && classes[0] == String.class)
        {
            sendMsg((String) args[0]);
            return null;
        }
        else if ("sendProgress".equals(methodName) && classes.length == 1 && classes[0] == Integer.class)
        {
            sendProgress((Integer) args[0]);
            return null;
        }
        else if ("enqueue".equals(methodName) && classes.length == 10 && classes[0] == String.class)
        {
            return enqueue((String) args[0], (String) args[1], (String) args[2], (String) args[3], (String) args[4], (String) args[5],
                    (String) args[6], (String) args[7], (String) args[8], (Map<String, String>) args[9]);
        }
        else if ("enqueueSync".equals(methodName) && classes.length == 10 && classes[0] == String.class)
        {
            return enqueueSync((String) args[0], (String) args[1], (String) args[2], (String) args[3], (String) args[4], (String) args[5],
                    (String) args[6], (String) args[7], (String) args[8], (Map<String, String>) args[9]);
        }
        else if ("addDeliverable".equals(methodName) && classes.length == 2 && classes[0] == String.class && classes[1] == String.class)
        {
            return addDeliverable((String) args[0], (String) args[1]);
        }

        throw new NoSuchMethodException(methodName);
    }

    private void shouldKill()
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
    private void sendMsg(String msg)
    {
        em.getTransaction().begin();
        Helpers.createMessage(msg, ji, em);
        em.getTransaction().commit();
    }

    /**
     * Update the {@link com.enioka.jqm.jpamodel.History} with the given progress data.
     * 
     * @param msg
     * @throws JqmKillException
     */
    private void sendProgress(Integer msg)
    {
        em.getTransaction().begin();
        em.refresh(ji, LockModeType.PESSIMISTIC_WRITE);
        ji.setProgress(msg);
        em.getTransaction().commit();
        jqmlogger.debug("Current progression: " + msg);
    }

    private Integer enqueue(String applicationName, String user, String mail, String sessionId, String application, String module,
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

        return Dispatcher.enQueue(jd);
    }

    private int enqueueSync(String applicationName, String user, String mail, String sessionId, String application, String module,
            String keyword1, String keyword2, String keyword3, Map<String, String> parameters)
    {
        int i = enqueue(applicationName, user, mail, sessionId, application, module, keyword1, keyword2, keyword3, parameters);
        JobInstance child = em.createQuery("SELECT h FROM JobInstance h WHERE h.id = :id", JobInstance.class).setParameter("id", i)
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
            try
            {
                em.refresh(child);
            }
            catch (EntityNotFoundException e)
            {
                break;
            }
        }

        return i;
    }

    private Integer addDeliverable(String path, String fileLabel) throws IOException
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

    private File getWorkDir()
    {
        File f = new File(FilenameUtils.concat(this.ji.getNode().getDlRepo(), "" + this.ji.getId()));
        if (!f.isDirectory() && !f.mkdir())
        {
            throw new JqmRuntimeException("Could not create work directory");
        }
        return f;
    }

    private String getDefaultConnectionAlias()
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

    private DataSource getDefaultConnection() throws NamingException
    {
        Object p = NamingManager.getInitialContext(null).lookup(this.getDefaultConnectionAlias());
        DataSource q = (DataSource) p;

        return q;
    }
}
