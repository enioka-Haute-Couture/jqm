package com.enioka.jqm.api;

import java.util.Calendar;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.RRole;
import com.enioka.jqm.jpamodel.RUser;

/**
 * Helper class helping dealing with internal security when calling the simple REST API (the API dealing with file transfers)<br>
 * This API may be secured - therefore, we must create a temporary internal user with the necessary permissions.
 * 
 */
final class SimpleApiSecurity
{
    private static Logger jqmlogger = LoggerFactory.getLogger(SimpleApiSecurity.class);

    private static RUser user;
    private static String secret;
    private static Duet logindata;
    private static Boolean useAuth = null;

    static class Duet
    {
        String usr;
        String pass;
    }

    private SimpleApiSecurity()
    {
        // Helper class
    }

    /**
     * Will create (or recreate) if necessary the temporary login data.<br>
     * Will create its own transaction - therefore the given em must not have any active transaction.
     */
    static Duet getId(EntityManager em)
    {
        if (logindata == null && useAuth == null)
        {
            try
            {
                GlobalParameter gp = em.createQuery("SELECT gp from GlobalParameter gp WHERE gp.key = 'useAuth'", GlobalParameter.class)
                        .getSingleResult();
                useAuth = Boolean.parseBoolean(gp.getValue());
            }
            catch (NoResultException e)
            {
                useAuth = true;
            }

            if (!useAuth)
            {
                jqmlogger.debug("The client API will not use any authentication to download files");
                logindata = new Duet();
                logindata.pass = null;
                logindata.usr = null;
            }
            else
            {
                jqmlogger.debug("The client API will use authentication to download files");
            }
        }

        if (!useAuth)
        {
            return logindata;
        }

        if (user == null || user.getExpirationDate().before(Calendar.getInstance()))
        {
            synchronized (SimpleApiSecurity.class)
            {
                if (user == null || user.getExpirationDate().before(Calendar.getInstance()))
                {
                    jqmlogger.debug("The client API will create an internal secret to access the simple API for file downloading");
                    em.getTransaction().begin();

                    // Create new
                    user = new RUser();
                    secret = UUID.randomUUID().toString();
                    Calendar expiration = Calendar.getInstance();
                    expiration.add(Calendar.DAY_OF_YEAR, 1);
                    user.setExpirationDate(expiration);
                    user.setInternal(true);
                    user.setLocked(false);
                    user.setLogin(UUID.randomUUID().toString());

                    ByteSource salt = new SecureRandomNumberGenerator().nextBytes();
                    user.setPassword(new Sha512Hash(secret, salt, 100000).toHex());
                    user.setHashSalt(salt.toHex());

                    logindata = new Duet();
                    logindata.pass = secret;
                    logindata.usr = user.getLogin();

                    RRole r = em.createQuery("SELECT r from RRole r where r.name = 'administrator'", RRole.class).getSingleResult();
                    user.getRoles().add(r);
                    r.getUsers().add(user);

                    em.persist(user);

                    // Purge all old internal accounts
                    em.createQuery("DELETE FROM RUser u WHERE u.expirationDate < :n").setParameter("n", Calendar.getInstance())
                            .executeUpdate();

                    em.getTransaction().commit();
                }
            }
        }

        return logindata;
    }

    static void dispose()
    {
        user = null;
        logindata = null;
        secret = null;
        useAuth = null;
    }
}
